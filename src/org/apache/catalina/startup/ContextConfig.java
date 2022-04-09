package org.apache.catalina.startup;

import org.apache.catalina.*;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.util.ContextName;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.buf.UriUtil;
import org.apache.tomcat.util.digester.Digester;
import org.apache.tomcat.util.digester.RuleSet;
import org.apache.tomcat.util.file.ConfigFileLoader;
import org.apache.tomcat.util.file.ConfigurationSource;
import org.apache.tomcat.util.res.StringManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class ContextConfig  implements LifecycleListener {

    private static final Log log = LogFactory.getLog(ContextConfig.class);

    /**
     * The string resources for this package.
     */
    protected static final StringManager sm =
            StringManager.getManager(Constants.Package);




    /**
     * Original docBase.
     */
    protected String originalDocBase = null;

    /**
     * The Context we are associated with.
     */
    protected volatile Context context = null;

    /**
     * Track any fatal errors during startup configuration processing.
     */
    protected boolean ok = false;


    /**
     * Anti-locking docBase. It is a path to a copy of the web application
     * in the java.io.tmpdir directory. This path is always an absolute one.
     */
    private File antiLockingDocBase = null;

    /**
     * Deployment count.
     */
    protected static long deploymentCount = 0L;


    /**
     * The default web application's deployment descriptor location.
     */
    protected String defaultWebXml = null;

    // --------------------------------------------------------- Public Methods


    /**
     * Process a "before start" event for this Context.
     */
    protected synchronized void beforeStart() {

        try {
            fixDocBase();
        } catch (IOException e) {
            log.error(sm.getString(
                    "contextConfig.fixDocBase", context.getName()), e);
        }

        antiLocking();
    }

    protected void antiLocking() {

        if ((context instanceof StandardContext)
                && ((StandardContext) context).getAntiResourceLocking()) {

            Host host = (Host) context.getParent();
            String docBase = context.getDocBase();
            if (docBase == null) {
                return;
            }
            originalDocBase = docBase;

            File docBaseFile = new File(docBase);
            if (!docBaseFile.isAbsolute()) {
                docBaseFile = new File(host.getAppBaseFile(), docBase);
            }

            String path = context.getPath();
            if (path == null) {
                return;
            }
            ContextName cn = new ContextName(path, context.getWebappVersion());
            docBase = cn.getBaseName();

            String tmp = System.getProperty("java.io.tmpdir");
            File tmpFile = new File(tmp);
            if (!tmpFile.isDirectory()) {
                log.error(sm.getString("contextConfig.noAntiLocking", tmp, context.getName()));
                return;
            }

            if (originalDocBase.toLowerCase(Locale.ENGLISH).endsWith(".war")) {
                antiLockingDocBase = new File(tmpFile, deploymentCount++ + "-" + docBase + ".war");
            } else {
                antiLockingDocBase = new File(tmpFile, deploymentCount++ + "-" + docBase);
            }
            antiLockingDocBase = antiLockingDocBase.getAbsoluteFile();

            if (log.isDebugEnabled()) {
                log.debug("Anti locking context[" + context.getName()
                        + "] setting docBase to " +
                        antiLockingDocBase.getPath());
            }

            // Cleanup just in case an old deployment is lying around
            ExpandWar.delete(antiLockingDocBase);
            if (ExpandWar.copy(docBaseFile, antiLockingDocBase)) {
                context.setDocBase(antiLockingDocBase.getPath());
            }
        }
    }


    /**
     * Adjust docBase.
     * @throws IOException cannot access the context base path
     */
    protected void fixDocBase() throws IOException {

        Host host = (Host) context.getParent();
        File appBase = host.getAppBaseFile();

        // This could be blank, relative, absolute or canonical
        String docBaseConfigured = context.getDocBase();
        // If there is no explicit docBase, derive it from the path and version
        if (docBaseConfigured == null) {
            // Trying to guess the docBase according to the path
            String path = context.getPath();
            if (path == null) {
                return;
            }
            ContextName cn = new ContextName(path, context.getWebappVersion());
            docBaseConfigured = cn.getBaseName();
        }

        // Obtain the absolute docBase in String and File form
        String docBaseAbsolute;
        File docBaseConfiguredFile = new File(docBaseConfigured);
        if (!docBaseConfiguredFile.isAbsolute()) {
            docBaseAbsolute = (new File(appBase, docBaseConfigured)).getAbsolutePath();
        } else {
            docBaseAbsolute = docBaseConfiguredFile.getAbsolutePath();
        }
        File docBaseAbsoluteFile = new File(docBaseAbsolute);
        String originalDocBase = docBaseAbsolute;

        ContextName cn = new ContextName(context.getPath(), context.getWebappVersion());
        String pathName = cn.getBaseName();

        boolean unpackWARs = true;
        if (host instanceof StandardHost) {
            unpackWARs = ((StandardHost) host).isUnpackWARs();
            if (unpackWARs && context instanceof StandardContext) {
                unpackWARs =  ((StandardContext) context).getUnpackWAR();
            }
        }

        // At this point we need to determine if we have a WAR file in the
        // appBase that needs to be expanded. Therefore we consider the absolute
        // docBase NOT the canonical docBase. This is because some users symlink
        // WAR files into the appBase and we want this to work correctly.
        boolean docBaseAbsoluteInAppBase = docBaseAbsolute.startsWith(appBase.getPath() + File.separatorChar);
        if (docBaseAbsolute.toLowerCase(Locale.ENGLISH).endsWith(".war") && !docBaseAbsoluteFile.isDirectory()) {
            URL war = UriUtil.buildJarUrl(docBaseAbsoluteFile);
            if (unpackWARs) {
                docBaseAbsolute = ExpandWar.expand(host, war, pathName);
                docBaseAbsoluteFile = new File(docBaseAbsolute);
                if (context instanceof StandardContext) {
                    ((StandardContext) context).setOriginalDocBase(originalDocBase);
                }
            } else {
                ExpandWar.validate(host, war, pathName);
            }
        } else {
            File docBaseAbsoluteFileWar = new File(docBaseAbsolute + ".war");
            URL war = null;
            if (docBaseAbsoluteFileWar.exists() && docBaseAbsoluteInAppBase) {
                war = UriUtil.buildJarUrl(docBaseAbsoluteFileWar);
            }
            if (docBaseAbsoluteFile.exists()) {
                if (war != null && unpackWARs) {
                    // Check if WAR needs to be re-expanded (e.g. if it has
                    // changed). Note: HostConfig.deployWar() takes care of
                    // ensuring that the correct XML file is used.
                    // This will be a NO-OP if the WAR is unchanged.
                    ExpandWar.expand(host, war, pathName);
                }
            } else {
                if (war != null) {
                    if (unpackWARs) {
                        docBaseAbsolute = ExpandWar.expand(host, war, pathName);
                        docBaseAbsoluteFile = new File(docBaseAbsolute);
                    } else {
                        docBaseAbsoluteFile = docBaseAbsoluteFileWar;
                        ExpandWar.validate(host, war, pathName);
                    }
                }
                if (context instanceof StandardContext) {
                    ((StandardContext) context).setOriginalDocBase(originalDocBase);
                }
            }
        }

        String docBaseCanonical = docBaseAbsoluteFile.getCanonicalPath();

        // Re-calculate now docBase is a canonical path
        boolean docBaseCanonicalInAppBase =
                docBaseAbsoluteFile.getCanonicalFile().toPath().startsWith(appBase.toPath());
        String docBase;
        if (docBaseCanonicalInAppBase) {
            docBase = docBaseCanonical.substring(appBase.getPath().length());
            docBase = docBase.replace(File.separatorChar, '/');
            if (docBase.startsWith("/")) {
                docBase = docBase.substring(1);
            }
        } else {
            docBase = docBaseCanonical.replace(File.separatorChar, '/');
        }

        context.setDocBase(docBase);
    }


    /**
     * Process events for an associated Context.
     *
     * @param event The lifecycle event that has occurred
     */
    @Override
    public void lifecycleEvent(LifecycleEvent event) {

        // Identify the context we are associated with
        try {
            context = (Context) event.getLifecycle();
        } catch (ClassCastException e) {
            log.error(sm.getString("contextConfig.cce", event.getLifecycle()), e);
            return;
        }

        // Process the event that has occurred
        if (event.getType().equals(Lifecycle.CONFIGURE_START_EVENT)) {
            configureStart();
        } else if (event.getType().equals(Lifecycle.BEFORE_START_EVENT)) {
            beforeStart();
        } else if (event.getType().equals(Lifecycle.AFTER_START_EVENT)) {
            // Restore docBase for management tools
            if (originalDocBase != null) {
                context.setDocBase(originalDocBase);
            }
        } else if (event.getType().equals(Lifecycle.CONFIGURE_STOP_EVENT)) {
            configureStop();
        } else if (event.getType().equals(Lifecycle.AFTER_INIT_EVENT)) {
            init();
        } else if (event.getType().equals(Lifecycle.AFTER_DESTROY_EVENT)) {
            destroy();
        }

    }

    protected boolean getUseGeneratedCode() {
        Catalina catalina = Container.getService(context).getServer().getCatalina();
        if (catalina != null) {
            return catalina.getUseGeneratedCode();
        } else {
            return false;
        }
    }

    /**
     * Create (if necessary) and return a Digester configured to process the
     * context configuration descriptor for an application.
     * @return the digester for context.xml files
     */
    protected Digester createContextDigester() {
        Digester digester = new Digester();
        digester.setValidating(false);
        digester.setRulesValidation(true);
        Map<Class<?>, List<String>> fakeAttributes = new HashMap<>();
        List<String> objectAttrs = new ArrayList<>();
        objectAttrs.add("className");
        fakeAttributes.put(Object.class, objectAttrs);
        // Ignore attribute added by Eclipse for its internal tracking
        List<String> contextAttrs = new ArrayList<>();
        contextAttrs.add("source");
        fakeAttributes.put(StandardContext.class, contextAttrs);
        digester.setFakeAttributes(fakeAttributes);
        RuleSet contextRuleSet = new ContextRuleSet("", false);
        digester.addRuleSet(contextRuleSet);
        RuleSet namingRuleSet = new NamingRuleSet("Context/");
        digester.addRuleSet(namingRuleSet);
        return digester;
    }



    /**
     * Process a "init" event for this Context.
     */
    protected synchronized void init() {
        // Called from StandardContext.init()

        Digester contextDigester = null;
        if (!getUseGeneratedCode()) {
            contextDigester = createContextDigester();
            contextDigester.getParser();
        }

        if (log.isDebugEnabled()) {
            log.debug(sm.getString("contextConfig.init"));
        }
        context.setConfigured(false);
        ok = true;

        contextConfig(contextDigester);
    }

    protected boolean getGenerateCode() {
        Catalina catalina = Container.getService(context).getServer().getCatalina();
        if (catalina != null) {
            return catalina.getGenerateCode();
        } else {
            return false;
        }
    }

    /**
     * Process the default configuration file, if it exists.
     * @param digester The digester that will be used for XML parsing
     */
    protected void contextConfig(Digester digester) {

        String defaultContextXml = null;

        boolean generateCode = getGenerateCode();
        boolean useGeneratedCode = getUseGeneratedCode();

        String contextXmlPackageName = null;
        String contextXmlSimpleClassName = null;
        String contextXmlClassName = null;
        File contextXmlJavaSource = null;

        // Open the default context.xml file, if it exists
        if (context instanceof StandardContext) {
            defaultContextXml = ((StandardContext)context).getDefaultContextXml();
        }
        // set the default if we don't have any overrides
        if (defaultContextXml == null) {
            defaultContextXml = Constants.DefaultContextXml;
        }

        ContextXml contextXml = null;

        if (!context.getOverride()) {

            if (useGeneratedCode || generateCode) {
                contextXmlPackageName = getGeneratedCodePackage();
                contextXmlSimpleClassName = "ContextXmlDefault";
                contextXmlClassName = contextXmlPackageName + "." + contextXmlSimpleClassName;
            }
            if (useGeneratedCode) {
                contextXml = (ContextXml) Digester.loadGeneratedClass(contextXmlClassName);
            }
            if (contextXml != null) {
                contextXml.load(context);
                contextXml = null;
            } else if (!useGeneratedCode) {
                try (ConfigurationSource.Resource contextXmlResource =
                             ConfigFileLoader.getSource().getResource(defaultContextXml)) {
                    if (generateCode) {
                        contextXmlJavaSource = getContextXmlJavaSource(contextXmlPackageName, contextXmlSimpleClassName);
                        digester.startGeneratingCode();
                        generateClassHeader(digester, contextXmlPackageName, contextXmlSimpleClassName);
                    }
                    URL defaultContextUrl = contextXmlResource.getURI().toURL();
                    processContextConfig(digester, defaultContextUrl, contextXmlResource.getInputStream());
                    if (generateCode) {
                        generateClassFooter(digester);
                        try (FileWriter writer = new FileWriter(contextXmlJavaSource)) {
                            writer.write(digester.getGeneratedCode().toString());
                        }
                        digester.endGeneratingCode();
                        Digester.addGeneratedClass(contextXmlClassName);
                    }
                } catch (MalformedURLException e) {
                    log.error(sm.getString("contextConfig.badUrl", defaultContextXml), e);
                } catch (IOException e) {
                    // Not found
                }
            }

            if (useGeneratedCode || generateCode) {
                contextXmlPackageName = getContextXmlPackageName(getGeneratedCodePackage(), context);
                contextXmlSimpleClassName = "ContextXmlDefault";
                contextXmlClassName = contextXmlPackageName + "." + contextXmlSimpleClassName;
            }
            if (useGeneratedCode) {
                contextXml = (ContextXml) Digester.loadGeneratedClass(contextXmlClassName);
            }
            if (contextXml != null) {
                contextXml.load(context);
                contextXml = null;
            } else if (!useGeneratedCode) {
                String hostContextFile = Container.getConfigPath(context, Constants.HostContextXml);
                try (ConfigurationSource.Resource contextXmlResource =
                             ConfigFileLoader.getSource().getResource(hostContextFile)) {
                    if (generateCode) {
                        contextXmlJavaSource = getContextXmlJavaSource(contextXmlPackageName, contextXmlSimpleClassName);
                        digester.startGeneratingCode();
                        generateClassHeader(digester, contextXmlPackageName, contextXmlSimpleClassName);
                    }
                    URL defaultContextUrl = contextXmlResource.getURI().toURL();
                    processContextConfig(digester, defaultContextUrl, contextXmlResource.getInputStream());
                    if (generateCode) {
                        generateClassFooter(digester);
                        try (FileWriter writer = new FileWriter(contextXmlJavaSource)) {
                            writer.write(digester.getGeneratedCode().toString());
                        }
                        digester.endGeneratingCode();
                        Digester.addGeneratedClass(contextXmlClassName);
                    }
                } catch (MalformedURLException e) {
                    log.error(sm.getString("contextConfig.badUrl", hostContextFile), e);
                } catch (IOException e) {
                    // Not found
                }
            }
        }

        if (context.getConfigFile() != null) {
            if (useGeneratedCode || generateCode) {
                contextXmlPackageName = getContextXmlPackageName(getGeneratedCodePackage(), context);
                contextXmlSimpleClassName = "ContextXml_" + context.getName().replace('/', '_').replace("-", "__");
                contextXmlClassName = contextXmlPackageName + "." + contextXmlSimpleClassName;
            }
            if (useGeneratedCode) {
                contextXml = (ContextXml) Digester.loadGeneratedClass(contextXmlClassName);
            }
            if (contextXml != null) {
                contextXml.load(context);
                contextXml = null;
            } else if (!useGeneratedCode) {
                if (generateCode) {
                    contextXmlJavaSource = getContextXmlJavaSource(contextXmlPackageName, contextXmlSimpleClassName);
                    digester.startGeneratingCode();
                    generateClassHeader(digester, contextXmlPackageName, contextXmlSimpleClassName);
                }
                processContextConfig(digester, context.getConfigFile(), null);
                if (generateCode) {
                    generateClassFooter(digester);
                    try (FileWriter writer = new FileWriter(contextXmlJavaSource)) {
                        writer.write(digester.getGeneratedCode().toString());
                    } catch (IOException e) {
                        // Ignore
                    }
                    digester.endGeneratingCode();
                    Digester.addGeneratedClass(contextXmlClassName);
                }
            }
        }

    }


    /**
     * Process a "destroy" event for this Context.
     */
    protected synchronized void destroy() {
        // Called from StandardContext.destroy()
        if (log.isDebugEnabled()) {
            log.debug(sm.getString("contextConfig.destroy"));
        }

        // Skip clearing the work directory if Tomcat is being shutdown
        Server s = getServer();
        if (s != null && !s.getState().isAvailable()) {
            return;
        }

        // Changed to getWorkPath per Bugzilla 35819.
        if (context instanceof StandardContext) {
            String workDir = ((StandardContext) context).getWorkPath();
            if (workDir != null) {
                ExpandWar.delete(new File(workDir));
            }
        }
    }



    /**
     * Process a "stop" event for this Context.
     */
    protected synchronized void configureStop() {

        if (log.isDebugEnabled()) {
            log.debug(sm.getString("contextConfig.stop"));
        }

        int i;

        // Removing children
        Container[] children = context.findChildren();
        for (i = 0; i < children.length; i++) {
            context.removeChild(children[i]);
        }

        // Removing application parameters
        /*
        ApplicationParameter[] applicationParameters =
            context.findApplicationParameters();
        for (i = 0; i < applicationParameters.length; i++) {
            context.removeApplicationParameter
                (applicationParameters[i].getName());
        }
        */

        // Removing security constraints
        SecurityConstraint[] securityConstraints = context.findConstraints();
        for (i = 0; i < securityConstraints.length; i++) {
            context.removeConstraint(securityConstraints[i]);
        }

        // Removing errors pages
        ErrorPage[] errorPages = context.findErrorPages();
        for (i = 0; i < errorPages.length; i++) {
            context.removeErrorPage(errorPages[i]);
        }

        // Removing filter defs
        FilterDef[] filterDefs = context.findFilterDefs();
        for (i = 0; i < filterDefs.length; i++) {
            context.removeFilterDef(filterDefs[i]);
        }

        // Removing filter maps
        FilterMap[] filterMaps = context.findFilterMaps();
        for (i = 0; i < filterMaps.length; i++) {
            context.removeFilterMap(filterMaps[i]);
        }

        // Removing Mime mappings
        String[] mimeMappings = context.findMimeMappings();
        for (i = 0; i < mimeMappings.length; i++) {
            context.removeMimeMapping(mimeMappings[i]);
        }

        // Removing parameters
        String[] parameters = context.findParameters();
        for (i = 0; i < parameters.length; i++) {
            context.removeParameter(parameters[i]);
        }

        // Removing security role
        String[] securityRoles = context.findSecurityRoles();
        for (i = 0; i < securityRoles.length; i++) {
            context.removeSecurityRole(securityRoles[i]);
        }

        // Removing servlet mappings
        String[] servletMappings = context.findServletMappings();
        for (i = 0; i < servletMappings.length; i++) {
            context.removeServletMapping(servletMappings[i]);
        }

        // Removing welcome files
        String[] welcomeFiles = context.findWelcomeFiles();
        for (i = 0; i < welcomeFiles.length; i++) {
            context.removeWelcomeFile(welcomeFiles[i]);
        }

        // Removing wrapper lifecycles
        String[] wrapperLifecycles = context.findWrapperLifecycles();
        for (i = 0; i < wrapperLifecycles.length; i++) {
            context.removeWrapperLifecycle(wrapperLifecycles[i]);
        }

        // Removing wrapper listeners
        String[] wrapperListeners = context.findWrapperListeners();
        for (i = 0; i < wrapperListeners.length; i++) {
            context.removeWrapperListener(wrapperListeners[i]);
        }

        // Remove (partially) folders and files created by antiLocking
        if (antiLockingDocBase != null) {
            // No need to log failure - it is expected in this case
            ExpandWar.delete(antiLockingDocBase, false);
        }

        // Reset ServletContextInitializer scanning
        initializerClassMap.clear();
        typeInitializerMap.clear();

        ok = true;

    }



    /**
     * Process a "contextConfig" event for this Context.
     */
    protected synchronized void configureStart() {
        // Called from StandardContext.start()

        if (log.isDebugEnabled()) {
            log.debug(sm.getString("contextConfig.start"));
        }

        if (log.isDebugEnabled()) {
            log.debug(sm.getString("contextConfig.xmlSettings",
                    context.getName(),
                    Boolean.valueOf(context.getXmlValidation()),
                    Boolean.valueOf(context.getXmlNamespaceAware())));
        }

        webConfig();
        context.addServletContainerInitializer(new JasperInitializer(), null);
        if (!context.getIgnoreAnnotations()) {
            applicationAnnotationsConfig();
        }
        if (ok) {
            validateSecurityRoles();
        }

        // Configure an authenticator if we need one
        if (ok) {
            authenticatorConfig();
        }

        // Dump the contents of this pipeline if requested
        if (log.isDebugEnabled()) {
            log.debug("Pipeline Configuration:");
            Pipeline pipeline = context.getPipeline();
            Valve valves[] = null;
            if (pipeline != null) {
                valves = pipeline.getValves();
            }
            if (valves != null) {
                for (Valve valve : valves) {
                    log.debug("  " + valve.getClass().getName());
                }
            }
            log.debug("======================");
        }

        // Make our application available if no problems were encountered
        if (ok) {
            context.setConfigured(true);
        } else {
            log.error(sm.getString("contextConfig.unavailable"));
            context.setConfigured(false);
        }

    }

}
