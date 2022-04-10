package org.apache.catalina.startup;

import org.apache.catalina.Container;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Server;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.juli.ClassLoaderLogManager;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.digester.Digester;
import org.apache.tomcat.util.digester.Rule;
import org.apache.tomcat.util.digester.RuleSet;
import org.apache.tomcat.util.file.ConfigFileLoader;
import org.apache.tomcat.util.file.ConfigurationSource;
import org.apache.tomcat.util.log.SystemLogHandler;
import org.apache.tomcat.util.res.StringManager;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;

public class Catalina {

    /**
     * what does StringManager mean, TODO, String manager for this package?
     */
    protected static final StringManager sm =
            StringManager.getManager(Constants.Package);

    private static final String SERVER_XML = "conf/server.xml";
    /**
     * The shared extensions class loader for this server
     *
     */
    private ClassLoader parentClassLoader = Catalina.class.getClassLoader();

    private static final Log log = LogFactory.getLog(Catalina.class);

    /**
     * understand it's role of server TODO，
     * here the modifier is protected, see why TODO
     */
    protected Server server = null;
    private String configFile = SERVER_XML;
    /**
     * Is naming enabled ?
     */
    protected boolean useNaming = true;
    /**
     * Use await.
     */
    protected boolean await = false;


    /**
     * Value of the argument, TODO, what does it mean?
     */
    protected String generatedCodeLocationParameter = null;
    private boolean loaded = false;
    /**
     * what does this field mean? TODO
     */
    private boolean useGeneratedCode;

    /**
     * Generate Tomcat embedded core from configuration files, TODO
     */
    private boolean generateCode = false;

    /**
     * Use shutdown hook flag.
     */
    protected boolean useShutdownHook = true;


    /**
     * Shutdown hook.
     */
    protected Thread shutdownHook = null;



    /**
     * Rethrow exceptions on init failure.
     */
    protected boolean throwOnInitFailure =
            Boolean.getBoolean("org.apache.catalina.startup.EXIT_ON_INIT_FAILURE");


    private String generatedCodePackage = "catalinaembedded";
    private File generatedCodeLocation = null;

    public void setParentClassLoader(ClassLoader parentClassLoader) {
        this.parentClassLoader = parentClassLoader;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public void load(String args[]) {
        arguments(args);
    }

    /**
     * Stop an existing server instance.
     */
    public void stop() {

        try {
            // Remove the ShutdownHook first so that server.stop()
            // doesn't get invoked twice
            if (useShutdownHook) {
                Runtime.getRuntime().removeShutdownHook(shutdownHook);

                // If JULI is being used, re-enable JULI's shutdown to ensure
                // log messages are not lost
                LogManager logManager = LogManager.getLogManager();
                if (logManager instanceof ClassLoaderLogManager) {
                    ((ClassLoaderLogManager) logManager).setUseShutdownHook(
                            true);
                }
            }
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            // This will fail on JDK 1.2. Ignoring, as Tomcat can run
            // fine without the shutdown hook.
        }

        // Shut down the server
        try {
            Server s = getServer();
            LifecycleState state = s.getState();
            if (LifecycleState.STOPPING_PREP.compareTo(state) <= 0
                    && LifecycleState.DESTROYED.compareTo(state) >= 0) {
                // Nothing to do. stop() was already called
            } else {
                s.stop();
                s.destroy();
            }
        } catch (LifecycleException e) {
            log.error(sm.getString("catalina.stopError"), e);
        }

    }


    /**
     * TODO why is here
     * @param args
     */
    private boolean arguments(String[] args) {
        boolean isConfig = false;
        boolean isGenerateCode = false; //What does it mean here?

        if (args.length < 1) {
            usage();
            return false;
        }

        for (String arg : args) {
            if (isConfig) {
                configFile = arg;
                isConfig = false;
            } else if (arg.equals("-config")) {
                isConfig = true;
            } else if(arg.equals("-generateCode")) {
                setGenerateCode(true);
                isGenerateCode = true;
            }else if (arg.equals("-useGenerateCode")) {//what does here mean? TODO
                setUseGenerateCode(true);
                isGenerateCode = false;
            } else if (arg.equals("-nonaming")) {
                setUseNaming(false);
                isGenerateCode = false;
            } else if (arg.equals("-help")) {
                usage();
                return false;
            } else if (arg.equals("start")) {
                isGenerateCode = false;
            } else if (arg.equals("configtest")) {//TODO what does here mean?
                isGenerateCode = false;
            } else if(arg.equals("stop")) {
                isGenerateCode = false;
            } else if (isGenerateCode) {
                generatedCodeLocationParameter = arg;
                isGenerateCode = false;
            } else {
                usage();
                return false;
            }
        }

        return true;
    }

    private void setUseNaming(boolean useNaming) {

    }

    private void setUseGenerateCode(boolean useGenerateCode) {

    }

    private void setGenerateCode(boolean generateCode) {

    }

    /**
     * describe the usage TODO, see the details
     */
    private void usage() {

    }

    /**
     * the real one load method being used, TODO
     */
    public void load() {
        if (loaded) {
            return;
        }
        loaded = true;
        long t1 = System.nanoTime();

        // what is naming?
        initNaming();

        // Parse main server.xml
        parseServerXml(true);

        Server s = getServer();
        if (s == null) {
            return;
        }

        getServer().setCatalina(this);
        getServer().setCatalinaHome(Bootstrap.getCatalinaHomeFile());
        getServer().setCatalinaBase(Bootstrap.getCatalinaBaseFile());

        // Stream redirection
        initStreams();

        // Start the new server
        try {
            getServer().init();
        } catch (LifecycleException e) {
            if (throwOnInitFailure) {
                throw new java.lang.Error(e);
            } else {
                log.error(sm.getString("catalina.initError"), e);
            }
        }

        if(log.isInfoEnabled()) {
            log.info(sm.getString("catalina.init", Long.toString(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t1))));
        }
    }


    /**
     * Start a new server instance.
     */
    public void start() {

        if (getServer() == null) {
            load();
        }

        if (getServer() == null) {
            log.fatal(sm.getString("catalina.noServer"));
            return;
        }

        long t1 = System.nanoTime();

        // Start the new server
        try {
            getServer().start();
        } catch (LifecycleException e) {
            log.fatal(sm.getString("catalina.serverStartFail"), e);
            try {
                getServer().destroy();
            } catch (LifecycleException e1) {
                log.debug("destroy() failed for failed Server ", e1);
            }
            return;
        }

        if (log.isInfoEnabled()) {
            log.info(sm.getString("catalina.startup", Long.toString(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t1))));
        }

        if (generateCode) {
            // Generate loader which will load all generated classes
            generateLoader();
        }

        // Register shutdown hook
        if (useShutdownHook) {
            if (shutdownHook == null) {
                shutdownHook = new CatalinaShutdownHook();
            }
            Runtime.getRuntime().addShutdownHook(shutdownHook);

            // If JULI is being used, disable JULI's shutdown hook since
            // shutdown hooks run in parallel and log messages may be lost
            // if JULI's hook completes before the CatalinaShutdownHook()
            LogManager logManager = LogManager.getLogManager();
            if (logManager instanceof ClassLoaderLogManager) {
                ((ClassLoaderLogManager) logManager).setUseShutdownHook(
                        false);
            }
        }

        if (await) {
            await();
            stop();
        }
    }

    /**
     * Await and shutdown.
     */
    public void await() {

        getServer().await();

    }

    protected void generateLoader() {
        String loaderClassName = "DigesterGeneratedCodeLoader";
        StringBuilder code = new StringBuilder();
        code.append("package ").append(generatedCodePackage).append(";").append(System.lineSeparator());
        code.append("public class ").append(loaderClassName);
        code.append(" implements org.apache.tomcat.util.digester.Digester.GeneratedCodeLoader {").append(System.lineSeparator());
        code.append("public Object loadGeneratedCode(String className) {").append(System.lineSeparator());
        code.append("switch (className) {").append(System.lineSeparator());
        for (String generatedClassName : Digester.getGeneratedClasses()) {
            code.append("case \"").append(generatedClassName).append("\" : return new ").append(generatedClassName);
            code.append("();").append(System.lineSeparator());
        }
        code.append("default: return null; }").append(System.lineSeparator());
        code.append("}}").append(System.lineSeparator());
        File loaderLocation = new File(generatedCodeLocation, generatedCodePackage);
        try (FileWriter writer = new FileWriter(new File(loaderLocation, loaderClassName + ".java"))) {
            writer.write(code.toString());
        } catch (IOException e) {
            // Should not happen
            log.debug("Error writing code loader", e);
        }
    }




    protected void initStreams() {
        // Replace System.out and System.err with a custom PrintStream
        System.setOut(new SystemLogHandler(System.out));
        System.setErr(new SystemLogHandler(System.err));
    }

    public boolean getUseGeneratedCode() {
        return this.useGeneratedCode;
    }
    public boolean getGenerateCode() {
        return this.generateCode;
    }

    public File getGeneratedCodeLocation() {
        return this.generatedCodeLocation;
    }


    public void setGeneratedCodeLocation(File generatedCodeLocation) {
        this.generatedCodeLocation = generatedCodeLocation;
    }

    public String getGeneratedCodePackage() {
        return this.generatedCodePackage;
    }

    private void parseServerXml(boolean start) {
        ConfigFileLoader.setSource(new CatalinaBaseConfigurationSource(Bootstrap.getCatalinaBaseFile(), getConfigFile()));
        File file = configFile();

        //TODO, unable to understand here
        if (useGeneratedCode && !Digester.isGeneratedCodeLoaderSet()) {
            String loaderClassName = generatedCodePackage + ".DigesterGeneratedCodeLoader";
            try {
               Digester.GeneratedCodeLoader loader = (Digester.GeneratedCodeLoader)
                       Catalina.class.getClassLoader().loadClass(loaderClassName).getDeclaredConstructor().newInstance();
               //TODO, generatedCodeLoader, what does it mean?
               Digester.setGeneratedCodeLoader(loader);
            } catch (Exception e){
                if (log.isDebugEnabled()) {
                    log.info(sm.getString("catalina.noLoader", loaderClassName), e);
                } else {
                    log.info(sm.getString("catalina.noLoader", loaderClassName));
                }
                useGeneratedCode = false;
            }
        }

        File serverXmlLocation = null;
        String xmlClassName = null;

        //TODO
        if (generateCode || useGeneratedCode) {
            xmlClassName = start? generatedCodePackage + ".ServerXml" : generatedCodePackage + ".ServerXmlStop";
        }

        if (generateCode) {
            if (generatedCodeLocationParameter != null) {
                generatedCodeLocation = new File(generatedCodeLocationParameter);
                if (!generatedCodeLocation.isAbsolute()) {
                    generatedCodeLocation = new File(Bootstrap.getCatalinaHome(),generatedCodeLocationParameter);
                }
            } else {
                generatedCodeLocation = new File(Bootstrap.getCatalinaHomeFile(), generatedCodeLocationParameter);
            }

            serverXmlLocation = new File(generatedCodeLocation, generatedCodePackage);

            //mkdir 和mkdirs difference TODO
            if (!serverXmlLocation.isDirectory() && !serverXmlLocation.mkdirs()) {
                log.warn(sm.getString("catalina.generatedCodeLocationError", generatedCodeLocation.getAbsolutePath()));
                generateCode = false;
            }
        }

        ServerXml serverXml = null;
        if (useGeneratedCode) {
            serverXml = (ServerXml) Digester.loadGeneratedClass(xmlClassName);
        }

        if (serverXml != null) {
            serverXml.load(this);
        } else {
            try(ConfigurationSource.Resource resource = ConfigFileLoader.getSource().getServerXml()) {
                // Create and execute our Digester
                Digester digester = start? createStartDigester(): createStopDigester();
                InputStream inputStream =  resource.getInputStream();
                InputSource inputSource = new InputSource(resource.getURI().toURL().toString());
                inputSource.setByteStream(inputStream);
                digester.push(this);

                if (generateCode) {
                    digester.startGeneratingCode();
                    generateClassHeader(digester, start);
                }
                digester.parse(inputSource);
                if (generateCode) {
                    generateClassFooter(digester);
                    try (FileWriter writer = new FileWriter(new File(serverXmlLocation,
                            start? "ServerXml.java" : "ServerXmlStop.java"))) {

                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
                log.warn(sm.getString("catalina.configFail", file.getAbsolutePath()), e);
                if (file.exists() && !file.canRead()) {
                    log.warn(sm.getString("catalina.incorrectPermissions"));
                }
            }
        }
    }

    private void generateClassFooter(Digester digester) {

    }

    private void generateClassHeader(Digester digester, boolean start) {

    }

    private Digester createStopDigester() {
        return null;
    }

    /**
     * Create and configure the Digester we will be using for startup.
     * @return the main digester to parse server.xml
     */
    protected Digester createStartDigester() {
        // Initialize the digester
        Digester digester = new Digester();
        digester.setValidating(false);
        digester.setRulesValidation(true);
        Map<Class<?>, List<String>> fakeAttributes = new HashMap<>();
        // Ignore className on all elements
        List<String> objectAttrs = new ArrayList<>();
        objectAttrs.add("className");
        fakeAttributes.put(Object.class, objectAttrs);
        // Ignore attribute added by Eclipse for its internal tracking
        List<String> contextAttrs = new ArrayList<>();
        contextAttrs.add("source");
        fakeAttributes.put(StandardContext.class, contextAttrs);
        // Ignore Connector attribute used internally but set on Server
        List<String> connectorAttrs = new ArrayList<>();
        connectorAttrs.add("portOffset");
        fakeAttributes.put(Connector.class, connectorAttrs);
        digester.setFakeAttributes(fakeAttributes);
        digester.setUseContextClassLoader(true);

        // Configure the actions we will be using
        digester.addObjectCreate("Server",
                "org.apache.catalina.core.StandardServer",
                "className");
        digester.addSetProperties("Server");
        digester.addSetNext("Server",
                "setServer",
                "org.apache.catalina.Server");

        digester.addObjectCreate("Server/GlobalNamingResources",
                "org.apache.catalina.deploy.NamingResourcesImpl");
        digester.addSetProperties("Server/GlobalNamingResources");
        digester.addSetNext("Server/GlobalNamingResources",
                "setGlobalNamingResources",
                "org.apache.catalina.deploy.NamingResourcesImpl");

        digester.addRule("Server/Listener",
                new ListenerCreateRule(null, "className"));
        digester.addSetProperties("Server/Listener");
        digester.addSetNext("Server/Listener",
                "addLifecycleListener",
                "org.apache.catalina.LifecycleListener");

        digester.addObjectCreate("Server/Service",
                "org.apache.catalina.core.StandardService",
                "className");
        digester.addSetProperties("Server/Service");
        digester.addSetNext("Server/Service",
                "addService",
                "org.apache.catalina.Service");

        digester.addObjectCreate("Server/Service/Listener",
                null, // MUST be specified in the element
                "className");
        digester.addSetProperties("Server/Service/Listener");
        digester.addSetNext("Server/Service/Listener",
                "addLifecycleListener",
                "org.apache.catalina.LifecycleListener");

        //Executor
        digester.addObjectCreate("Server/Service/Executor",
                "org.apache.catalina.core.StandardThreadExecutor",
                "className");
        digester.addSetProperties("Server/Service/Executor");

        digester.addSetNext("Server/Service/Executor",
                "addExecutor",
                "org.apache.catalina.Executor");

        digester.addRule("Server/Service/Connector",
                new ConnectorCreateRule());
        digester.addSetProperties("Server/Service/Connector",
                new String[]{"executor", "sslImplementationName", "protocol"});
        digester.addSetNext("Server/Service/Connector",
                "addConnector",
                "org.apache.catalina.connector.Connector");

        digester.addRule("Server/Service/Connector", new AddPortOffsetRule());

        digester.addObjectCreate("Server/Service/Connector/SSLHostConfig",
                "org.apache.tomcat.util.net.SSLHostConfig");
        digester.addSetProperties("Server/Service/Connector/SSLHostConfig");
        digester.addSetNext("Server/Service/Connector/SSLHostConfig",
                "addSslHostConfig",
                "org.apache.tomcat.util.net.SSLHostConfig");

        digester.addRule("Server/Service/Connector/SSLHostConfig/Certificate",
                new CertificateCreateRule());
        digester.addSetProperties("Server/Service/Connector/SSLHostConfig/Certificate", new String[]{"type"});
        digester.addSetNext("Server/Service/Connector/SSLHostConfig/Certificate",
                "addCertificate",
                "org.apache.tomcat.util.net.SSLHostConfigCertificate");

        digester.addObjectCreate("Server/Service/Connector/SSLHostConfig/OpenSSLConf",
                "org.apache.tomcat.util.net.openssl.OpenSSLConf");
        digester.addSetProperties("Server/Service/Connector/SSLHostConfig/OpenSSLConf");
        digester.addSetNext("Server/Service/Connector/SSLHostConfig/OpenSSLConf",
                "setOpenSslConf",
                "org.apache.tomcat.util.net.openssl.OpenSSLConf");

        digester.addObjectCreate("Server/Service/Connector/SSLHostConfig/OpenSSLConf/OpenSSLConfCmd",
                "org.apache.tomcat.util.net.openssl.OpenSSLConfCmd");
        digester.addSetProperties("Server/Service/Connector/SSLHostConfig/OpenSSLConf/OpenSSLConfCmd");
        digester.addSetNext("Server/Service/Connector/SSLHostConfig/OpenSSLConf/OpenSSLConfCmd",
                "addCmd",
                "org.apache.tomcat.util.net.openssl.OpenSSLConfCmd");

        digester.addObjectCreate("Server/Service/Connector/Listener",
                null, // MUST be specified in the element
                "className");
        digester.addSetProperties("Server/Service/Connector/Listener");
        digester.addSetNext("Server/Service/Connector/Listener",
                "addLifecycleListener",
                "org.apache.catalina.LifecycleListener");

        digester.addObjectCreate("Server/Service/Connector/UpgradeProtocol",
                null, // MUST be specified in the element
                "className");
        digester.addSetProperties("Server/Service/Connector/UpgradeProtocol");
        digester.addSetNext("Server/Service/Connector/UpgradeProtocol",
                "addUpgradeProtocol",
                "org.apache.coyote.UpgradeProtocol");

        // Add RuleSets for nested elements
        digester.addRuleSet(new NamingRuleSet("Server/GlobalNamingResources/"));
        digester.addRuleSet(new EngineRuleSet("Server/Service/"));
        digester.addRuleSet(new HostRuleSet("Server/Service/Engine/"));
        digester.addRuleSet(new ContextRuleSet("Server/Service/Engine/Host/"));
        addClusterRuleSet(digester, "Server/Service/Engine/Host/Cluster/");
        digester.addRuleSet(new NamingRuleSet("Server/Service/Engine/Host/Context/"));

        // When the 'engine' is found, set the parentClassLoader.
        digester.addRule("Server/Service/Engine",
                new SetParentClassLoaderRule(parentClassLoader));
        addClusterRuleSet(digester, "Server/Service/Engine/Cluster/");

        return digester;

    }


    /**
     * TODO what does here mean?
     * Cluster support is optional. The JARs may have been removed.
     */
    private void addClusterRuleSet(Digester digester, String prefix) {
        Class<?> clazz = null;
        Constructor<?> constructor = null;
        try {
            clazz = Class.forName("org.apache.catalina.ha.ClusterRuleSet");
            constructor = clazz.getConstructor(String.class);
            RuleSet ruleSet = (RuleSet) constructor.newInstance(prefix);
            digester.addRuleSet(ruleSet);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug(sm.getString("catalina.noCluster",
                        e.getClass().getName() + ": " +  e.getMessage()), e);
            } else if (log.isInfoEnabled()) {
                log.info(sm.getString("catalina.noCluster",
                        e.getClass().getName() + ": " +  e.getMessage()));
            }
        }
    }

    private File configFile() {
        File file = new File(configFile);
        if (!file.isAbsolute()) {
            file = new File(Bootstrap.getCatalinaBase(), configFile);
        }
        return file;
    }

    private String getConfigFile() {
        return configFile;
    }

    protected void initNaming() {
        // Setting additional variables
        if (!useNaming) {
            log.info(sm.getString("catalina.noNaming"));
            System.setProperty("catalina.useNaming", "false");
        } else {
            System.setProperty("catalina.useNaming", "true");
            String value = "org.apache.naming";
            String oldValue =
                    System.getProperty(javax.naming.Context.URL_PKG_PREFIXES);
            if (oldValue != null) {
                value = value + ":" + oldValue;
            }
            System.setProperty(javax.naming.Context.URL_PKG_PREFIXES, value);
            if( log.isDebugEnabled() ) {
                log.debug("Setting naming prefix=" + value);
            }
            value = System.getProperty
                    (javax.naming.Context.INITIAL_CONTEXT_FACTORY);
            if (value == null) {
                System.setProperty
                        (javax.naming.Context.INITIAL_CONTEXT_FACTORY,
                                "org.apache.naming.java.javaURLContextFactory");
            } else {
                log.debug("INITIAL_CONTEXT_FACTORY already set " + value );
            }
        }
    }

    private interface ServerXml {
        void load(Catalina catalina);
    }


    /**
     * what does this mean? ParentClassLoader?
     */
    final class SetParentClassLoaderRule extends Rule {

        public SetParentClassLoaderRule(ClassLoader parentClassLoader) {

            this.parentClassLoader = parentClassLoader;

        }

        ClassLoader parentClassLoader = null;

        @Override
        public void begin(String namespace, String name, Attributes attributes)
                throws Exception {

            if (digester.getLogger().isDebugEnabled()) {
                digester.getLogger().debug("Setting parent class loader");
            }

            Container top = (Container) digester.peek();
            top.setParentClassLoader(parentClassLoader);

            StringBuilder code = digester.getGeneratedCode();
            if (code != null) {
                code.append(digester.toVariableName(top)).append(".setParentClassLoader(");
                code.append(digester.toVariableName(Catalina.this)).append(".getParentClassLoader());");
                code.append(System.lineSeparator());
            }
        }

    }




    // XXX Should be moved to embedded !
    /**
     * Shutdown hook which will perform a clean shutdown of Catalina if needed.
     */
    protected class CatalinaShutdownHook extends Thread {

        @Override
        public void run() {
            try {
                if (getServer() != null) {
                    Catalina.this.stop();
                }
            } catch (Throwable ex) {
                ExceptionUtils.handleThrowable(ex);
                log.error(sm.getString("catalina.shutdownHookFail"), ex);
            } finally {
                // If JULI is used, shut JULI down *after* the server shuts down
                // so log messages aren't lost
                LogManager logManager = LogManager.getLogManager();
                if (logManager instanceof ClassLoaderLogManager) {
                    ((ClassLoaderLogManager) logManager).shutdown();
                }
            }
        }
    }


}
