package org.apache.catalina.startup;

import org.apache.catalina.Server;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.digester.Digester;
import org.apache.tomcat.util.file.ConfigFileLoader;
import org.apache.tomcat.util.file.ConfigurationSource;
import org.apache.tomcat.util.res.StringManager;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;


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



    private String generatedCodePackage = "catalinaembedded";
    private File generatedCodeLocation = null;

    public void setParentClassLoader(ClassLoader parentClassLoader) {
        this.parentClassLoader = parentClassLoader;
    }

    public Server getServer() {
        return server;
    }

    public void load(String args[]) {
        arguments(args);
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
    }

    private void parseServerXml(boolean start) {
        ConfigFileLoader.setSource(new CatalinaBaseConfigurationSource(BootStrap.getCatalinaBaseFile(), getConfigFile()));
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
                    generatedCodeLocation = new File(BootStrap.getCatalinaHome(),generatedCodeLocationParameter);
                }
            } else {
                generatedCodeLocation = new File(BootStrap.getCatalinaHomeFile(), generatedCodeLocationParameter);
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

    private Digester createStartDigester() {
        return null;
    }

    private File configFile() {
        File file = new File(configFile);
        if (!file.isAbsolute()) {
            file = new File(BootStrap.getCatalinaBase(), configFile);
        }
        return file;
    }

    private String getConfigFile() {
        return configFile;
    }

    private void initNaming() {

    }

    private interface ServerXml {
        void load(Catalina catalina);
    }
}
