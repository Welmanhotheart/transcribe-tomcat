package org.apache.catalina.core;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.ServletSecurityElement;
import jakarta.servlet.descriptor.JspConfigDescriptor;
import org.apache.catalina.*;
import org.apache.catalina.deploy.NamingResourcesImpl;
import org.apache.catalina.org.apache.tomcat.util.descriptor.web.LoginConfig;
import org.apache.catalina.util.URLEncoder;
import org.apache.jasper.servlet.jakarta.servlet.ServletContainerInitializer;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.JarScanner;
import org.apache.tomcat.util.compat.JreCompat;
import org.apache.tomcat.util.descriptor.XmlIdentifiers;
import org.apache.tomcat.util.descriptor.web.*;

import javax.management.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class StandardContext extends ContainerBase
        implements Context, NotificationEmitter {


    private static final Log log = LogFactory.getLog(StandardContext.class);

    private Set<String> resourceOnlyServlets = new HashSet<>();

    /**
     * The broadcaster that sends j2ee notifications.
     */
    private NotificationBroadcasterSupport broadcaster = null;

    private final Object servletMappingsLock = new Object();

    private final ReadWriteLock resourcesLock = new ReentrantReadWriteLock();

    private WebResourceRoot resources;

    /**
     * Lifecycle provider.
     */
    private InstanceManager instanceManager = null;

    /**
     * The servlet mappings for this web application, keyed by
     * matching pattern.
     */
    private Map<String, String> servletMappings = new HashMap<>();

    /**
     * The public identifier of the DTD for the web application deployment
     * descriptor version we are currently parsing.  This is used to support
     * relaxed validation rules when processing version 2.2 web.xml files.
     */
    private String publicId = null;

    /**
     * Context level override for default {@link StandardHost#isCopyXML()}.
     */
    private boolean copyXML = false;

    /**
     * Unencoded path for this web application.
     */
    private String path = null;

    /**
     * The original document root for this web application.
     */
    private String originalDocBase = null;

    /**
     * The watched resources for this application.
     */
    private String watchedResources[] = new String[0];

    private final Object watchedResourcesLock = new Object();

    /**
     * The antiResourceLocking flag for this Context.
     */
    private boolean antiResourceLocking = false;

    private long startupTime;
    private long startTime;
    /**
     * Set the original document root for this Context.  This can be an absolute
     * pathname, a relative pathname, or a URL.
     *
     * @param docBase The original document root
     */
    public void setOriginalDocBase(String docBase) {

        this.originalDocBase = docBase;
    }


    /**
     * @return the antiResourceLocking flag for this Context.
     */
    public boolean getAntiResourceLocking() {
        return this.antiResourceLocking;
    }

    /**
     * Gets the time (in milliseconds) it took to start this context.
     *
     * @return Time (in milliseconds) it took to start this context.
     */
    public long getStartupTime() {
        return startupTime;
    }

    public void setStartupTime(long startupTime) {
        this.startupTime = startupTime;
    }

    /**
     * Encoded path.
     */
    private String encodedPath = null;

    /**
     * The security constraints for this web application.
     */
    private volatile SecurityConstraint constraints[] =
            new SecurityConstraint[0];

    private final Object constraintsLock = new Object();

    /**
     * The set of application listener class names configured for this
     * application, in the order they were encountered in the resulting merged
     * web.xml file.
     */
    private String applicationListeners[] = new String[0];

    private boolean parallelAnnotationScanning = false;

    /**
     * The "follow standard delegation model" flag that will be used to
     * configure our ClassLoader.
     * Graal cannot actually load a class from the webapp classloader,
     * so delegate by default.
     */
    private boolean delegate = JreCompat.isGraalAvailable();


    /**
     * The "correctly configured" flag for this Context.
     */
    private boolean configured = false;

    /**
     * The URL of the XML descriptor for this context.
     */
    private URL configFile = null;

    /**
     * Override the default context xml location.
     */
    private String defaultContextXml;

    /**
     * Override the default web xml location.
     */
    private String defaultWebXml;


    private String webappVersion = "";


    /**
     * The Loader implementation with which this Container is associated.
     */
    private Loader loader = null;
    private final ReadWriteLock loaderLock = new ReentrantReadWriteLock();

    // ----------------------------------------------------------- Constructors


    /**
     * Create a new StandardContext component with the default basic Valve.
     */
    public StandardContext() {

        super();
        pipeline.setBasic(new StandardContextValve());
        broadcaster = new NotificationBroadcasterSupport();
        // Set defaults
        if (!Globals.STRICT_SERVLET_COMPLIANCE) {
            // Strict servlet compliance requires all extension mapped servlets
            // to be checked against welcome files
            resourceOnlyServlets.add("jsp");
        }
    }


    /**
     * Unpack WAR property.
     */
    private boolean unpackWAR = true;
    /**
     * @return unpack WAR flag.
     */
    public boolean getUnpackWAR() {
        return unpackWAR;
    }

    /**
     * Flag which indicates if bundled context.xml files should be copied to the
     * config folder. The doesn't occur by default.
     *
     * @return <code>true</code> if the <code>META-INF/context.xml</code> file included
     *     in a WAR will be copied to the host configuration base folder on deployment
     */
    public boolean getCopyXML() {
        return copyXML;
    }

    /**
     * Remove a JMX notificationListener
     * @see javax.management.NotificationEmitter#removeNotificationListener(javax.management.NotificationListener, javax.management.NotificationFilter, java.lang.Object)
     */
    @Override
    public void removeNotificationListener(NotificationListener listener,
                                           NotificationFilter filter, Object object) throws ListenerNotFoundException {
        broadcaster.removeNotificationListener(listener,filter,object);
    }

    /**
     * Add a JMX NotificationListener
     * @see javax.management.NotificationBroadcaster#addNotificationListener(javax.management.NotificationListener, javax.management.NotificationFilter, java.lang.Object)
     */
    @Override
    public void addNotificationListener(NotificationListener listener,
                                        NotificationFilter filter, Object object) throws IllegalArgumentException {
        broadcaster.addNotificationListener(listener,filter,object);
    }

    /**
     * The pathname to the work directory for this context (relative to
     * the server's home if not absolute).
     */
    private String workDir = null;

    /**
     * @return the work directory for this Context.
     */
    public String getWorkDir() {
        return this.workDir;
    }

    /** Get the absolute path to the work dir.
     *  To avoid duplication.
     *
     * @return The work path
     */
    public String getWorkPath() {
        if (getWorkDir() == null) {
            return null;
        }
        File workDir = new File(getWorkDir());
        if (!workDir.isAbsolute()) {
            try {
                workDir = new File(getCatalinaBase().getCanonicalFile(),
                        getWorkDir());
            } catch (IOException e) {
                log.warn(sm.getString("standardContext.workPath", getName()),
                        e);
            }
        }
        return workDir.getAbsolutePath();
    }


    /**
     * Remove a JMX-NotificationListener
     * @see javax.management.NotificationBroadcaster#removeNotificationListener(javax.management.NotificationListener)
     */
    @Override
    public void removeNotificationListener(NotificationListener listener)
            throws ListenerNotFoundException {
        broadcaster.removeNotificationListener(listener);
    }


    private MBeanNotificationInfo[] notificationInfo;

    /**
     * Get JMX Broadcaster Info
     * @see javax.management.NotificationBroadcaster#getNotificationInfo()
     */
    @Override
    public MBeanNotificationInfo[] getNotificationInfo() {
        // FIXME: we not send j2ee.attribute.changed
        if (notificationInfo == null) {
            notificationInfo = new MBeanNotificationInfo[] {
                    new MBeanNotificationInfo(
                            new String[] { "j2ee.object.created" },
                            Notification.class.getName(),
                            "web application is created"),
                    new MBeanNotificationInfo(
                            new String[] { "j2ee.state.starting" },
                            Notification.class.getName(),
                            "change web application is starting"),
                    new MBeanNotificationInfo(
                            new String[] { "j2ee.state.running" },
                            Notification.class.getName(),
                            "web application is running"),
                    new MBeanNotificationInfo(
                            new String[] { "j2ee.state.stopping" },
                            Notification.class.getName(),
                            "web application start to stopped"),
                    new MBeanNotificationInfo(
                            new String[] { "j2ee.object.stopped" },
                            Notification.class.getName(),
                            "web application is stopped"),
                    new MBeanNotificationInfo(
                            new String[] { "j2ee.object.deleted" },
                            Notification.class.getName(),
                            "web application is deleted"),
                    new MBeanNotificationInfo(
                            new String[] { "j2ee.object.failed" },
                            Notification.class.getName(),
                            "web application failed") };
        }

        return notificationInfo;
    }


    /**
     * Add a child Container, only if the proposed child is an implementation
     * of Wrapper.
     *
     * @param child Child container to be added
     *
     * @exception IllegalArgumentException if the proposed container is
     *  not an implementation of Wrapper
     */
    @Override
    public void addChild(Container child) {

        // Global JspServlet
        Wrapper oldJspServlet = null;

        if (!(child instanceof Wrapper)) {
            throw new IllegalArgumentException
                    (sm.getString("standardContext.notWrapper"));
        }

        boolean isJspServlet = "jsp".equals(child.getName());

        // Allow webapp to override JspServlet inherited from global web.xml.
        if (isJspServlet) {
            oldJspServlet = (Wrapper) findChild("jsp");
            if (oldJspServlet != null) {
                removeChild(oldJspServlet);
            }
        }

        super.addChild(child);

        if (isJspServlet && oldJspServlet != null) {
            /*
             * The webapp-specific JspServlet inherits all the mappings
             * specified in the global web.xml, and may add additional ones.
             */
            String[] jspMappings = oldJspServlet.findMappings();
            for (int i=0; jspMappings!=null && i<jspMappings.length; i++) {
                addServletMappingDecoded(jspMappings[i], child.getName());
            }
        }
    }

    @Override
    public Set<String> addServletSecurity(ServletRegistration.Dynamic registration, ServletSecurityElement servletSecurityElement) {
        return null;
    }

    /**
     * Add a new servlet mapping, replacing any existing mapping for
     * the specified pattern.
     *
     * @param pattern URL pattern to be mapped
     * @param name Name of the corresponding servlet to execute
     * @param jspWildCard true if name identifies the JspServlet
     * and pattern contains a wildcard; false otherwise
     *
     * @exception IllegalArgumentException if the specified servlet name
     *  is not known to this Context
     */
    @Override
    public void addServletMappingDecoded(String pattern, String name,
                                         boolean jspWildCard) {
        // Validate the proposed mapping
        if (findChild(name) == null) {
            throw new IllegalArgumentException
                    (sm.getString("standardContext.servletMap.name", name));
        }
        String adjustedPattern = adjustURLPattern(pattern);
        if (!validateURLPattern(adjustedPattern)) {
            throw new IllegalArgumentException
                    (sm.getString("standardContext.servletMap.pattern", adjustedPattern));
        }

        // Add this mapping to our registered set
        synchronized (servletMappingsLock) {
            String name2 = servletMappings.get(adjustedPattern);
            if (name2 != null) {
                // Don't allow more than one servlet on the same pattern
                Wrapper wrapper = (Wrapper) findChild(name2);
                wrapper.removeMapping(adjustedPattern);
            }
            servletMappings.put(adjustedPattern, name);
        }
        Wrapper wrapper = (Wrapper) findChild(name);
        wrapper.addMapping(adjustedPattern);

        fireContainerEvent("addServletMapping", adjustedPattern);
    }


    /**
     * Check for unusual but valid <code>&lt;url-pattern&gt;</code>s.
     * See Bugzilla 34805, 43079 & 43080
     */
    private void checkUnusualURLPattern(String urlPattern) {
        if (log.isInfoEnabled()) {
            // First group checks for '*' or '/foo*' style patterns
            // Second group checks for *.foo.bar style patterns
            if((urlPattern.endsWith("*") && (urlPattern.length() < 2 ||
                    urlPattern.charAt(urlPattern.length()-2) != '/')) ||
                    urlPattern.startsWith("*.") && urlPattern.length() > 2 &&
                            urlPattern.lastIndexOf('.') > 1) {
                log.info(sm.getString("standardContext.suspiciousUrl", urlPattern, getName()));
            }
        }
    }

    /**
     * Validate the syntax of a proposed <code>&lt;url-pattern&gt;</code>
     * for conformance with specification requirements.
     *
     * @param urlPattern URL pattern to be validated
     * @return <code>true</code> if the URL pattern is conformant
     */
    private boolean validateURLPattern(String urlPattern) {

        if (urlPattern == null) {
            return false;
        }
        if (urlPattern.indexOf('\n') >= 0 || urlPattern.indexOf('\r') >= 0) {
            return false;
        }
        if (urlPattern.equals("")) {
            return true;
        }
        if (urlPattern.startsWith("*.")) {
            if (urlPattern.indexOf('/') < 0) {
                checkUnusualURLPattern(urlPattern);
                return true;
            } else {
                return false;
            }
        }
        if (urlPattern.startsWith("/") && !urlPattern.contains("*.")) {
            checkUnusualURLPattern(urlPattern);
            return true;
        } else {
            return false;
        }

    }

    /**
     * Are we processing a version 2.2 deployment descriptor?
     *
     * @return <code>true</code> if running a legacy Servlet 2.2 application
     */
    @Override
    public boolean isServlet22() {
        return XmlIdentifiers.WEB_22_PUBLIC.equals(publicId);
    }

    // ------------------------------------------------------ Protected Methods

    /**
     * Adjust the URL pattern to begin with a leading slash, if appropriate
     * (i.e. we are running a servlet 2.2 application).  Otherwise, return
     * the specified URL pattern unchanged.
     *
     * @param urlPattern The URL pattern to be adjusted (if needed)
     *  and returned
     * @return the URL pattern with a leading slash if needed
     */
    protected String adjustURLPattern(String urlPattern) {

        if (urlPattern == null) {
            return urlPattern;
        }
        if (urlPattern.startsWith("/") || urlPattern.startsWith("*.")) {
            return urlPattern;
        }
        if (!isServlet22()) {
            return urlPattern;
        }
        if(log.isDebugEnabled()) {
            log.debug(sm.getString("standardContext.urlPattern.patternWarning",
                    urlPattern));
        }
        return "/" + urlPattern;

    }

    @Override
    public WebResourceRoot getResources() {
        Lock readLock = resourcesLock.readLock();
        readLock.lock();
        try {
            return resources;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void setPublicId(String publicId) {

    }

    @Override
    public void setIgnoreAnnotations(boolean ignoreAnnotations) {

    }

    @Override
    public void addMimeMapping(String extension, String mimeType) {

    }

    @Override
    public void setRequestCharacterEncoding(String encoding) {

    }

    @Override
    public void setResponseCharacterEncoding(String encoding) {

    }

    @Override
    public String getResponseCharacterEncoding() {
        return null;
    }

    @Override
    public Wrapper createWrapper() {
        return null;
    }


    @Override
    public void setResources(WebResourceRoot resources) {

        Lock writeLock = resourcesLock.writeLock();
        writeLock.lock();
        WebResourceRoot oldResources = null;
        try {
            if (getState().isAvailable()) {
                throw new IllegalStateException
                        (sm.getString("standardContext.resourcesStart"));
            }

            oldResources = this.resources;
            if (oldResources == resources) {
                return;
            }

            this.resources = resources;
            if (oldResources != null) {
                oldResources.setContext(null);
            }
            if (resources != null) {
                resources.setContext(this);
            }

            support.firePropertyChange("resources", oldResources,
                    resources);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public InstanceManager getInstanceManager() {
        return instanceManager;
    }

    @Override
    public int getSessionTimeout() {
        return 0;
    }

    @Override
    public void setSessionTimeout(int timeout) {

    }


    @Override
    public void setInstanceManager(InstanceManager instanceManager) {
        this.instanceManager = instanceManager;
    }


    @Override
    public void backgroundProcess() {

        if (!getState().isAvailable()) {
            return;
        }

        Loader loader = getLoader();
        if (loader != null) {
            try {
                loader.backgroundProcess();
            } catch (Exception e) {
                log.warn(sm.getString(
                        "standardContext.backgroundProcess.loader", loader), e);
            }
        }
        Manager manager = getManager();
        if (manager != null) {
            try {
                manager.backgroundProcess();
            } catch (Exception e) {
                log.warn(sm.getString(
                                "standardContext.backgroundProcess.manager", manager),
                        e);
            }
        }
        WebResourceRoot resources = getResources();
        if (resources != null) {
            try {
                resources.backgroundProcess();
            } catch (Exception e) {
                log.warn(sm.getString(
                        "standardContext.backgroundProcess.resources",
                        resources), e);
            }
        }
        InstanceManager instanceManager = getInstanceManager();
        if (instanceManager != null) {
            try {
                instanceManager.backgroundProcess();
            } catch (Exception e) {
                log.warn(sm.getString(
                        "standardContext.backgroundProcess.instanceManager",
                        resources), e);
            }
        }
        super.backgroundProcess();
    }


    /**
     * @return the context path for this Context.
     */
    @Override
    public String getPath() {
        return path;
    }

    /**
     * Set the context path for this Context.
     *
     * @param path The new context path
     */
    @Override
    public void setPath(String path) {
        boolean invalid = false;
        if (path == null || path.equals("/")) {
            invalid = true;
            this.path = "";
        } else if (path.isEmpty() || path.startsWith("/")) {
            this.path = path;
        } else {
            invalid = true;
            this.path = "/" + path;
        }
        if (this.path.endsWith("/")) {
            invalid = true;
            this.path = this.path.substring(0, this.path.length() - 1);
        }
        if (invalid) {
            log.warn(sm.getString(
                    "standardContext.pathInvalid", path, this.path));
        }
        encodedPath = URLEncoder.DEFAULT.encode(this.path, StandardCharsets.UTF_8);
        if (getName() == null) {
            setName(this.path);
        }
    }

    /**
     * Set the "correctly configured" flag for this Context.  This can be
     * set to false by startup listeners that detect a fatal configuration
     * error to avoid the application from being made available.
     *
     * @param configured The new correctly configured flag
     */
    @Override
    public void setConfigured(boolean configured) {

        boolean oldConfigured = this.configured;
        this.configured = configured;
        support.firePropertyChange("configured",
                oldConfigured,
                this.configured);

    }

    /**
     * Add a security constraint to the set for this web application.
     *
     * @param constraint the new security constraint
     */
    @Override
    public void addConstraint(SecurityConstraint constraint) {

        // Validate the proposed constraint
        SecurityCollection collections[] = constraint.findCollections();
        for (SecurityCollection collection : collections) {
            String patterns[] = collection.findPatterns();
            for (int j = 0; j < patterns.length; j++) {
                patterns[j] = adjustURLPattern(patterns[j]);
                if (!validateURLPattern(patterns[j])) {
                    throw new IllegalArgumentException
                            (sm.getString
                                    ("standardContext.securityConstraint.pattern",
                                            patterns[j]));
                }
            }
            if (collection.findMethods().length > 0 &&
                    collection.findOmittedMethods().length > 0) {
                throw new IllegalArgumentException(sm.getString(
                        "standardContext.securityConstraint.mixHttpMethod"));
            }
        }

        // Add this constraint to the set for our web application
        synchronized (constraintsLock) {
            SecurityConstraint[] results = Arrays.copyOf(constraints, constraints.length + 1);
            results[constraints.length] = constraint;
            constraints = results;
        }

    }


    /**
     * Return the security constraints for this web application.
     * If there are none, a zero-length array is returned.
     */
    @Override
    public SecurityConstraint[] findConstraints() {
        return constraints;
    }

    @Override
    public void removeErrorPage(ErrorPage errorPage) {

    }

    @Override
    public FilterDef findFilterDef(String filterName) {
        return null;
    }

    @Override
    public void removeFilterDef(FilterDef filterDef) {

    }

    @Override
    public String[] findParameters() {
        return new String[0];
    }

    @Override
    public FilterDef[] findFilterDefs() {
        return new FilterDef[0];
    }

    @Override
    public String[] findMimeMappings() {
        return new String[0];
    }

    /**
     * Return the set of application listener class names configured
     * for this application.
     */
    @Override
    public String[] findApplicationListeners() {
        return applicationListeners;
    }

    @Override
    public void removeMimeMapping(String extension) {

    }

    @Override
    public void removeParameter(String name) {

    }

    @Override
    public String[] findSecurityRoles() {
        return new String[0];
    }

    @Override
    public void removeSecurityRole(String role) {

    }

    @Override
    public String[] findServletMappings() {
        return new String[0];
    }

    @Override
    public void removeServletMapping(String pattern) {

    }

    @Override
    public String[] findWelcomeFiles() {
        return new String[0];
    }

    @Override
    public void removeWelcomeFile(String name) {

    }

    @Override
    public String[] findWrapperLifecycles() {
        return new String[0];
    }

    @Override
    public void removeWrapperLifecycle(String listener) {

    }

    @Override
    public String[] findWrapperListeners() {
        return new String[0];
    }

    @Override
    public void addErrorPage(ErrorPage errorPage) {

    }

    @Override
    public void addFilterDef(FilterDef filterDef) {

    }

    @Override
    public void addFilterMap(FilterMap filterMap) {

    }

    @Override
    public void removeWrapperListener(String listener) {

    }

    @Override
    public void setJspConfigDescriptor(JspConfigDescriptor descriptor) {

    }

    @Override
    public void addLocaleEncodingMappingParameter(String locale, String encoding) {

    }

    @Override
    public void addApplicationListener(String listener) {

    }

    @Override
    public void setEffectiveMajorVersion(int major) {

    }

    @Override
    public int getEffectiveMinorVersion() {
        return 0;
    }

    @Override
    public void setEffectiveMinorVersion(int minor) {

    }

    @Override
    public void addParameter(String name, String value) {

    }

    @Override
    public void addServletContainerInitializer(ServletContainerInitializer sci, Set<Class<?>> classes) {

    }

    @Override
    public boolean getDenyUncoveredHttpMethods() {
        return false;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public void setDistributable(boolean distributable) {

    }

    @Override
    public boolean getDistributable() {
        return false;
    }

    @Override
    public void setDisplayName(String displayName) {

    }

    @Override
    public void setDenyUncoveredHttpMethods(boolean denyUncoveredHttpMethods) {

    }

    /**
     * Return the "follow standard delegation model" flag used to configure
     * our ClassLoader.
     *
     * @return <code>true</code> if classloading delegates to the parent classloader first
     */
    public boolean getDelegate() {
        return this.delegate;
    }

    @Override
    public boolean getXmlValidation() {
        return false;
    }

    @Override
    public boolean getXmlNamespaceAware() {
        return false;
    }

    @Override
    public boolean getIgnoreAnnotations() {
        return false;
    }

    @Override
    public void removeFilterMap(FilterMap filterMap) {

    }

    @Override
    public ErrorPage[] findErrorPages() {
        return new ErrorPage[0];
    }

    @Override
    public void removeConstraint(SecurityConstraint constraint) {

    }

    @Override
    public void addSecurityRole(String role) {

    }

    @Override
    public boolean findSecurityRole(String role) {
        return false;
    }

    @Override
    public LoginConfig getLoginConfig() {
        return null;
    }

    @Override
    public void setLoginConfig(LoginConfig config) {

    }

    @Override
    public Authenticator getAuthenticator() {
        return null;
    }

    @Override
    public boolean getXmlBlockExternal() {
        return false;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public void addWelcomeFile(String name) {

    }

    @Override
    public String findServletMapping(String pattern) {
        return null;
    }

    @Override
    public void addPostConstructMethod(String clazz, String method) {

    }

    @Override
    public void addPreDestroyMethod(String clazz, String method) {

    }

    /**
     * Add a new watched resource to the set recognized by this Context.
     *
     * @param name New watched resource file name
     */
    @Override
    public void addWatchedResource(String name) {

        synchronized (watchedResourcesLock) {
            String[] results = Arrays.copyOf(watchedResources, watchedResources.length + 1);
            results[watchedResources.length] = name;
            watchedResources = results;
        }
        fireContainerEvent("addWatchedResource", name);
    }

    @Override
    public JarScanner getJarScanner() {
        return null;
    }

    @Override
    public boolean getParallelAnnotationScanning() {
        return this.parallelAnnotationScanning;
    }

    @Override
    public void setParallelAnnotationScanning(boolean parallelAnnotationScanning) {

        boolean oldParallelAnnotationScanning = this.parallelAnnotationScanning;
        this.parallelAnnotationScanning = parallelAnnotationScanning;
        support.firePropertyChange("parallelAnnotationScanning", oldParallelAnnotationScanning,
                this.parallelAnnotationScanning);

    }

    @Override
    public boolean getLogEffectiveWebXml() {
        return false;
    }

    @Override
    public FilterMap[] findFilterMaps() {
        return new FilterMap[0];
    }

    /**
     * The default context override flag for this web application.
     */
    private boolean override = false;

    /**
     * @return the default context override flag for this web application.
     */
    @Override
    public boolean getOverride() {
        return this.override;
    }

    /**
     * Set the default context override flag for this web application.
     *
     * @param override The new override flag
     */
    @Override
    public void setOverride(boolean override) {

        boolean oldOverride = this.override;
        this.override = override;
        support.firePropertyChange("override",
                oldOverride,
                this.override);

    }

    @Override
    public URL getConfigFile() {
        return this.configFile;
    }

    public String getDefaultContextXml() {
        return defaultContextXml;
    }

    /**
     * Set the location of the default context xml that will be used.
     * If not absolute, it'll be made relative to the engine's base dir
     * ( which defaults to catalina.base system property ).
     *
     * @param defaultContextXml The default web xml
     */
    public void setDefaultContextXml(String defaultContextXml) {
        this.defaultContextXml = defaultContextXml;
    }

    public String getDefaultWebXml() {
        return defaultWebXml;
    }

    @Override
    public Loader getLoader() {
        Lock readLock = loaderLock.readLock();
        readLock.lock();
        try {
            return loader;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void setLoader(Loader loader) {

        Lock writeLock = loaderLock.writeLock();
        writeLock.lock();
        Loader oldLoader = null;
        try {
            // Change components if necessary
            oldLoader = this.loader;
            if (oldLoader == loader) {
                return;
            }
            this.loader = loader;

            // Stop the old component if necessary
            if (getState().isAvailable() && (oldLoader != null) &&
                    (oldLoader instanceof Lifecycle)) {
                try {
                    ((Lifecycle) oldLoader).stop();
                } catch (LifecycleException e) {
                    log.error(sm.getString("standardContext.setLoader.stop"), e);
                }
            }

            // Start the new component if necessary
            if (loader != null) {
                loader.setContext(this);
            }
            if (getState().isAvailable() && (loader != null) &&
                    (loader instanceof Lifecycle)) {
                try {
                    ((Lifecycle) loader).start();
                } catch (LifecycleException e) {
                    log.error(sm.getString("standardContext.setLoader.start"), e);
                }
            }
        } finally {
            writeLock.unlock();
        }

        // Report this property change to interested listeners
        support.firePropertyChange("loader", oldLoader, loader);
    }

    @Override
    public void setConfigFile(URL configFile) {
        this.configFile = configFile;
    }

    @Override
    public String getWebappVersion() {
        return webappVersion;
    }

    @Override
    public void setWebappVersion(String webappVersion) {

    }

    @Override
    public String getDocBase() {
        return null;
    }

    @Override
    public String getContainerSciFilter() {
        return null;
    }

    @Override
    public void setDocBase(String docBase) {

    }

    @Override
    public String[] findWatchedResources() {
        return new String[0];
    }

    @Override
    public void removeLifecycleListener(LifecycleListener listener) {

    }

    @Override
    public void reload() {

    }

    @Override
    public Manager getManager() {
        return null;
    }

    @Override
    public NamingResourcesImpl getNamingResources() {
        return null;
    }

    @Override
    public void setManager(Manager manager) {

    }

    @Override
    public ClassLoader bind(boolean usePrivilegedAction, ClassLoader originalClassLoader) {
        return null;
    }

    @Override
    public void unbind(boolean usePrivilegedAction, ClassLoader originalClassLoader) {

    }
}
