package org.apache.catalina.core;

import jakarta.servlet.*;
import jakarta.servlet.descriptor.JspConfigDescriptor;
import jakarta.servlet.http.HttpSessionAttributeListener;
import jakarta.servlet.http.HttpSessionIdListener;
import jakarta.servlet.http.HttpSessionListener;
import org.apache.catalina.*;
import org.apache.catalina.deploy.NamingResourcesImpl;
import org.apache.catalina.loader.WebappClassLoaderBase;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.org.apache.tomcat.util.descriptor.web.LoginConfig;
import org.apache.catalina.session.StandardManager;
import org.apache.catalina.util.CharsetMapper;
import org.apache.catalina.util.ContextName;
import org.apache.catalina.util.URLEncoder;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.jasper.servlet.jakarta.servlet.ServletContainerInitializer;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.naming.ContextBindings;
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.InstanceManagerBindings;
import org.apache.tomcat.JarScanner;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.compat.JreCompat;
import org.apache.tomcat.util.descriptor.XmlIdentifiers;
import org.apache.tomcat.util.descriptor.web.*;
import org.apache.tomcat.util.http.CookieProcessor;
import org.apache.tomcat.util.http.Rfc6265CookieProcessor;

import javax.management.*;
import javax.naming.NamingException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
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


    private boolean addWebinfClassesResources = false;

    /**
     * The naming context listener for this web application.
     */
    private NamingContextListener namingContextListener = null;



    /**
     * Should the effective web.xml be logged when the context starts?
     */
    private boolean logEffectiveWebXml = false;

    private int effectiveMajorVersion = 3;

    private int effectiveMinorVersion = 0;

    /**
     * The notification sequence number.
     */
    private AtomicLong sequenceNumber = new AtomicLong(0);

    /**
     * The naming resources for this web application.
     */
    private NamingResourcesImpl namingResources = null;


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
     * The list of instantiated application event listener objects. Note that
     * SCIs and other code may use the pluggability APIs to add listener
     * instances directly to this list before the application starts.
     */
    private List<Object> applicationEventListenersList = new CopyOnWriteArrayList<>();

    /**
     * The set of application listeners that are required to have limited access
     * to ServletContext methods. See Servlet 3.1 section 4.4.
     */
    private final Set<Object> noPluggabilityListeners = new HashSet<>();


    /**
     * The set of instantiated application lifecycle listener objects. Note that
     * SCIs and other code may use the pluggability APIs to add listener
     * instances directly to this list before the application starts.
     */
    private Object applicationLifecycleListenersObjects[] =
            new Object[0];


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

    private CookieProcessor cookieProcessor;


    /**
     * JNDI use flag.
     */
    private boolean useNaming = true;


    /**
     * @return true if the internal naming support is used.
     */
    public boolean isUseNaming() {
        return useNaming;
    }

    /**
     * The Locale to character set mapper for this application.
     */
    private CharsetMapper charsetMapper = null;

    /**
     * The Java class name of the CharsetMapper class to be created.
     */
    private String charsetMapperClass =
            "org.apache.catalina.util.CharsetMapper";

    private boolean jndiExceptionOnFailedWrite = true;

    /**
     * The Loader implementation with which this Container is associated.
     */
    private Loader loader = null;
    private final ReadWriteLock loaderLock = new ReentrantReadWriteLock();


    // ------------------------------------------------------ Public Properties

    /**
     * @return whether or not an attempt to modify the JNDI context will trigger
     * an exception or if the request will be ignored.
     */
    public boolean getJndiExceptionOnFailedWrite() {
        return jndiExceptionOnFailedWrite;
    }


    /**
     * Controls whether or not an attempt to modify the JNDI context will
     * trigger an exception or if the request will be ignored.
     *
     * @param jndiExceptionOnFailedWrite <code>false</code> to avoid an exception
     */
    public void setJndiExceptionOnFailedWrite(
            boolean jndiExceptionOnFailedWrite) {
        this.jndiExceptionOnFailedWrite = jndiExceptionOnFailedWrite;
    }

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
     * The ServletContext implementation associated with this Context.
     */
    protected ApplicationContext context = null;

    /**
     * The distributable flag for this web application.
     */
    private boolean distributable = false;

    /**
     * The ordered set of ServletContainerInitializers for this web application.
     */
    private Map<ServletContainerInitializer,Set<Class<?>>> initializers =
            new LinkedHashMap<>();


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

    /**
     * @return the set of watched resources for this Context. If none are
     * defined, a zero length array will be returned.
     */
    @Override
    public String[] findWatchedResources() {
        synchronized (watchedResourcesLock) {
            return watchedResources;
        }
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

    @Override
    public String getBaseName() {
        return new ContextName(path, webappVersion).getBaseName();
    }

    @Override
    public void setCookieProcessor(CookieProcessor cookieProcessor) {
        if (cookieProcessor == null) {
            throw new IllegalArgumentException(
                    sm.getString("standardContext.cookieProcessor.null"));
        }
        this.cookieProcessor = cookieProcessor;
    }

    /**
     * @return the Locale to character set mapper for this Context.
     */
    public CharsetMapper getCharsetMapper() {

        // Create a mapper the first time it is requested
        if (this.charsetMapper == null) {
            try {
                Class<?> clazz = Class.forName(charsetMapperClass);
                this.charsetMapper = (CharsetMapper) clazz.getConstructor().newInstance();
            } catch (Throwable t) {
                ExceptionUtils.handleThrowable(t);
                this.charsetMapper = new CharsetMapper();
            }
        }

        return this.charsetMapper;

    }

    /**
     * Set the work directory for this Context.
     *
     * @param workDir The new work directory
     */
    public void setWorkDir(String workDir) {

        this.workDir = workDir;

        if (getState().isAvailable()) {
            postWorkDirectory();
        }
    }


    /**
     * Set the appropriate context attribute for our work directory.
     */
    protected void postWorkDirectory() {

        // Acquire (or calculate) the work directory path
        String workDir = getWorkDir();
        if (workDir == null || workDir.length() == 0) {

            // Retrieve our parent (normally a host) name
            String hostName = null;
            String engineName = null;
            String hostWorkDir = null;
            Container parentHost = getParent();
            if (parentHost != null) {
                hostName = parentHost.getName();
                if (parentHost instanceof StandardHost) {
                    hostWorkDir = ((StandardHost)parentHost).getWorkDir();
                }
                Container parentEngine = parentHost.getParent();
                if (parentEngine != null) {
                    engineName = parentEngine.getName();
                }
            }
            if ((hostName == null) || (hostName.length() < 1)) {
                hostName = "_";
            }
            if ((engineName == null) || (engineName.length() < 1)) {
                engineName = "_";
            }

            String temp = getBaseName();
            if (temp.startsWith("/")) {
                temp = temp.substring(1);
            }
            temp = temp.replace('/', '_');
            temp = temp.replace('\\', '_');
            if (temp.length() < 1) {
                temp = ContextName.ROOT_NAME;
            }
            if (hostWorkDir != null ) {
                workDir = hostWorkDir + File.separator + temp;
            } else {
                workDir = "work" + File.separator + engineName +
                        File.separator + hostName + File.separator + temp;
            }
            setWorkDir(workDir);
        }

        // Create this directory if necessary
        File dir = new File(workDir);
        if (!dir.isAbsolute()) {
            String catalinaHomePath = null;
            try {
                catalinaHomePath = getCatalinaBase().getCanonicalPath();
                dir = new File(catalinaHomePath, workDir);
            } catch (IOException e) {
                log.warn(sm.getString("standardContext.workCreateException",
                        workDir, catalinaHomePath, getName()), e);
            }
        }
        if (!dir.mkdirs() && !dir.isDirectory()) {
            log.warn(sm.getString("standardContext.workCreateFail", dir,
                    getName()));
        }

        // Set the appropriate servlet context attribute
        if (context == null) {
            getServletContext();
        }
        context.setAttribute(ServletContext.TEMPDIR, dir);
        context.setAttributeReadOnly(ServletContext.TEMPDIR);
    }

    /**
     * Allocate resources, including proxy.
     * @throws LifecycleException if a start error occurs
     */
    public void resourcesStart() throws LifecycleException {

        // Check current status in case resources were added that had already
        // been started
        if (!resources.getState().isAvailable()) {
            resources.start();
        }

        if (effectiveMajorVersion >=3 && addWebinfClassesResources) {
            WebResource webinfClassesResource = resources.getResource(
                    "/WEB-INF/classes/META-INF/resources");
            if (webinfClassesResource.isDirectory()) {
                getResources().createWebResourceSet(
                        WebResourceRoot.ResourceSetType.RESOURCE_JAR, "/",
                        webinfClassesResource.getURL(), "/");
            }
        }
    }

    /**
     * Naming context listener accessor.
     *
     * @return the naming context listener associated with the webapp
     */
    public NamingContextListener getNamingContextListener() {
        return namingContextListener;
    }

    /**
     * Naming context listener setter.
     *
     * @param namingContextListener the new naming context listener
     */
    public void setNamingContextListener(NamingContextListener namingContextListener) {
        this.namingContextListener = namingContextListener;
    }


    /**
     * Get naming context full name.
     *
     * @return the context name
     */
    private String getNamingContextName() {
        if (namingContextName == null) {
            Container parent = getParent();
            if (parent == null) {
                namingContextName = getName();
            } else {
                Stack<String> stk = new Stack<>();
                StringBuilder buff = new StringBuilder();
                while (parent != null) {
                    stk.push(parent.getName());
                    parent = parent.getParent();
                }
                while (!stk.empty()) {
                    buff.append("/" + stk.pop());
                }
                buff.append(getName());
                namingContextName = buff.toString();
            }
        }
        return namingContextName;
    }


    /**
     * Name of the associated naming context.
     */
    private String namingContextName = null;
    private final Object namingToken = new Object();


    @Override
    public Object getNamingToken() {
        return namingToken;
    }

    /**
     * Bind current thread, both for CL purposes and for JNDI ENC support
     * during : startup, shutdown and reloading of the context.
     *
     * @return the previous context class loader
     */
    protected ClassLoader bindThread() {

        ClassLoader oldContextClassLoader = bind(false, null);

        if (isUseNaming()) {
            try {
                ContextBindings.bindThread(this, getNamingToken());
            } catch (NamingException e) {
                // Silent catch, as this is a normal case during the early
                // startup stages
            }
        }

        return oldContextClassLoader;
    }

    /**
     * Enables the RMI Target memory leak detection to be controlled. This is
     * necessary since the detection can only work if some of the modularity
     * checks are disabled.
     */
    private boolean clearReferencesRmiTargets = true;

    /**
     * Should Tomcat attempt to terminate threads that have been started by the
     * web application? Stopping threads is performed via the deprecated (for
     * good reason) <code>Thread.stop()</code> method and is likely to result in
     * instability. As such, enabling this should be viewed as an option of last
     * resort in a development environment and is not recommended in a
     * production environment. If not specified, the default value of
     * <code>false</code> will be used.
     */
    private boolean clearReferencesStopThreads = false;

    /**
     * Should Tomcat attempt to terminate any {@link java.util.TimerThread}s
     * that have been started by the web application? If not specified, the
     * default value of <code>false</code> will be used.
     */
    private boolean clearReferencesStopTimerThreads = false;


    /**
     * If an HttpClient keep-alive timer thread has been started by this web
     * application and is still running, should Tomcat change the context class
     * loader from the current {@link ClassLoader} to
     * {@link ClassLoader#getParent()} to prevent a memory leak? Note that the
     * keep-alive timer thread will stop on its own once the keep-alives all
     * expire however, on a busy system that might not happen for some time.
     */
    private boolean clearReferencesHttpClientKeepAliveThread = true;

    /**
     * Should Tomcat attempt to clear references to classes loaded by the web
     * application class loader from the ObjectStreamClass caches?
     */
    private boolean clearReferencesObjectStreamClassCaches = true;

    /**
     * Should Tomcat attempt to clear references to classes loaded by this class
     * loader from ThreadLocals?
     */
    private boolean clearReferencesThreadLocals = true;


    public boolean getClearReferencesRmiTargets() {
        return this.clearReferencesRmiTargets;
    }

    /**
     * @return the clearReferencesStopThreads flag for this Context.
     */
    public boolean getClearReferencesStopThreads() {
        return this.clearReferencesStopThreads;
    }

    /**
     * @return the clearReferencesStopTimerThreads flag for this Context.
     */
    public boolean getClearReferencesStopTimerThreads() {
        return this.clearReferencesStopTimerThreads;
    }

    /**
     * @return the clearReferencesHttpClientKeepAliveThread flag for this
     * Context.
     */
    public boolean getClearReferencesHttpClientKeepAliveThread() {
        return this.clearReferencesHttpClientKeepAliveThread;
    }

    public boolean getClearReferencesObjectStreamClassCaches() {
        return clearReferencesObjectStreamClassCaches;
    }

    public boolean getClearReferencesThreadLocals() {
        return clearReferencesThreadLocals;
    }



    @Override
    public boolean getConfigured() {
        return this.configured;
    }


    @Override
    public InstanceManager createInstanceManager() {
        javax.naming.Context context = null;
        if (isUseNaming() && getNamingContextListener() != null) {
            context = getNamingContextListener().getEnvContext();
        }
        Map<String, Map<String, String>> injectionMap = buildInjectionMap(
                getIgnoreAnnotations() ? new NamingResourcesImpl(): getNamingResources());
        return new DefaultInstanceManager(context, injectionMap,
                this, this.getClass().getClassLoader());
    }

    private void addInjectionTarget(Injectable resource, Map<String, Map<String, String>> injectionMap) {
        List<InjectionTarget> injectionTargets = resource.getInjectionTargets();
        if (injectionTargets != null && injectionTargets.size() > 0) {
            String jndiName = resource.getName();
            for (InjectionTarget injectionTarget: injectionTargets) {
                String clazz = injectionTarget.getTargetClass();
                Map<String, String> injections = injectionMap.get(clazz);
                if (injections == null) {
                    injections = new HashMap<>();
                    injectionMap.put(clazz, injections);
                }
                injections.put(injectionTarget.getTargetName(), jndiName);
            }
        }
    }

    private Map<String, Map<String, String>> buildInjectionMap(NamingResourcesImpl namingResources) {
        Map<String, Map<String, String>> injectionMap = new HashMap<>();
        for (Injectable resource: namingResources.findLocalEjbs()) {
            addInjectionTarget(resource, injectionMap);
        }
        for (Injectable resource: namingResources.findEjbs()) {
            addInjectionTarget(resource, injectionMap);
        }
        for (Injectable resource: namingResources.findEnvironments()) {
            addInjectionTarget(resource, injectionMap);
        }
        for (Injectable resource: namingResources.findMessageDestinationRefs()) {
            addInjectionTarget(resource, injectionMap);
        }
        for (Injectable resource: namingResources.findResourceEnvRefs()) {
            addInjectionTarget(resource, injectionMap);
        }
        for (Injectable resource: namingResources.findResources()) {
            addInjectionTarget(resource, injectionMap);
        }
        for (Injectable resource: namingResources.findServices()) {
            addInjectionTarget(resource, injectionMap);
        }
        return injectionMap;
    }


    @Override
    public Object[] getApplicationEventListeners() {
        return applicationEventListenersList.toArray();
    }

    /**
     * {@inheritDoc}
     *
     * Note that this implementation is not thread safe. If two threads call
     * this method concurrently, the result may be either set of listeners or a
     * the union of both.
     */
    @Override
    public void setApplicationEventListeners(Object listeners[]) {
        applicationEventListenersList.clear();
        if (listeners != null && listeners.length > 0) {
            applicationEventListenersList.addAll(Arrays.asList(listeners));
        }
    }


    /**
     * Add a listener to the end of the list of initialized application event
     * listeners.
     *
     * @param listener The listener to add
     */
    public void addApplicationEventListener(Object listener) {
        applicationEventListenersList.add(listener);
    }


    @Override
    public Object[] getApplicationLifecycleListeners() {
        return applicationLifecycleListenersObjects;
    }



    /**
     * Store the set of initialized application lifecycle listener objects,
     * in the order they were specified in the web application deployment
     * descriptor, for this application.
     *
     * @param listeners The set of instantiated listener objects.
     */
    @Override
    public void setApplicationLifecycleListeners(Object listeners[]) {
        applicationLifecycleListenersObjects = listeners;
    }

    /**
     * Configure the set of instantiated application event listeners
     * for this Context.
     * @return <code>true</code> if all listeners wre
     * initialized successfully, or <code>false</code> otherwise.
     */
    public boolean listenerStart() {

        if (log.isDebugEnabled()) {
            log.debug("Configuring application event listeners");
        }

        // Instantiate the required listeners
        String listeners[] = findApplicationListeners();
        Object results[] = new Object[listeners.length];
        boolean ok = true;
        for (int i = 0; i < results.length; i++) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug(" Configuring event listener class '" +
                        listeners[i] + "'");
            }
            try {
                String listener = listeners[i];
                results[i] = getInstanceManager().newInstance(listener);
            } catch (Throwable t) {
                t = ExceptionUtils.unwrapInvocationTargetException(t);
                ExceptionUtils.handleThrowable(t);
                getLogger().error(sm.getString(
                        "standardContext.applicationListener", listeners[i]), t);
                ok = false;
            }
        }
        if (!ok) {
            getLogger().error(sm.getString("standardContext.applicationSkipped"));
            return false;
        }

        // Sort listeners in two arrays
        List<Object> eventListeners = new ArrayList<>();
        List<Object> lifecycleListeners = new ArrayList<>();
        for (Object result : results) {
            if ((result instanceof ServletContextAttributeListener)
                    || (result instanceof ServletRequestAttributeListener)
                    || (result instanceof ServletRequestListener)
                    || (result instanceof HttpSessionIdListener)
                    || (result instanceof HttpSessionAttributeListener)) {
                eventListeners.add(result);
            }
            if ((result instanceof ServletContextListener)
                    || (result instanceof HttpSessionListener)) {
                lifecycleListeners.add(result);
            }
        }

        // Listener instances may have been added directly to this Context by
        // ServletContextInitializers and other code via the pluggability APIs.
        // Put them these listeners after the ones defined in web.xml and/or
        // annotations then overwrite the list of instances with the new, full
        // list.
        eventListeners.addAll(Arrays.asList(getApplicationEventListeners()));
        setApplicationEventListeners(eventListeners.toArray());
        for (Object lifecycleListener: getApplicationLifecycleListeners()) {
            lifecycleListeners.add(lifecycleListener);
            if (lifecycleListener instanceof ServletContextListener) {
                noPluggabilityListeners.add(lifecycleListener);
            }
        }
        setApplicationLifecycleListeners(lifecycleListeners.toArray());

        // Send application start events

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Sending application start events");
        }

        // Ensure context is not null
        getServletContext();
        context.setNewServletContextListenerAllowed(false);

        Object instances[] = getApplicationLifecycleListeners();
        if (instances == null || instances.length == 0) {
            return ok;
        }

        ServletContextEvent event = new ServletContextEvent(getServletContext());
        ServletContextEvent tldEvent = null;
        if (noPluggabilityListeners.size() > 0) {
            noPluggabilityServletContext = new NoPluggabilityServletContext(getServletContext());
            tldEvent = new ServletContextEvent(noPluggabilityServletContext);
        }
        for (Object instance : instances) {
            if (!(instance instanceof ServletContextListener)) {
                continue;
            }
            ServletContextListener listener = (ServletContextListener) instance;
            try {
                fireContainerEvent("beforeContextInitialized", listener);
                if (noPluggabilityListeners.contains(listener)) {
                    listener.contextInitialized(tldEvent);
                } else {
                    listener.contextInitialized(event);
                }
                fireContainerEvent("afterContextInitialized", listener);
            } catch (Throwable t) {
                ExceptionUtils.handleThrowable(t);
                fireContainerEvent("afterContextInitialized", listener);
                getLogger().error(sm.getString("standardContext.listenerStart",
                        instance.getClass().getName()), t);
                ok = false;
            }
        }
        return ok;

    }

    @Override
    protected synchronized void startInternal() throws LifecycleException {

        if(log.isDebugEnabled()) {
            log.debug("Starting " + getBaseName());
        }

        // Send j2ee.state.starting notification
        if (this.getObjectName() != null) {
            Notification notification = new Notification("j2ee.state.starting",
                    this.getObjectName(), sequenceNumber.getAndIncrement());
            broadcaster.sendNotification(notification);
        }

        setConfigured(false);
        boolean ok = true;

        // Currently this is effectively a NO-OP but needs to be called to
        // ensure the NamingResources follows the correct lifecycle
        if (namingResources != null) {
            namingResources.start();
        }

        // Post work directory
        postWorkDirectory();

        // Add missing components as necessary
        if (getResources() == null) {   // (1) Required by Loader
            if (log.isDebugEnabled()) {
                log.debug("Configuring default Resources");
            }

            try {
                setResources(new StandardRoot(this));
            } catch (IllegalArgumentException e) {
                log.error(sm.getString("standardContext.resourcesInit"), e);
                ok = false;
            }
        }
        if (ok) {
            resourcesStart();
        }

        if (getLoader() == null) {
            WebappLoader webappLoader = new WebappLoader();
            webappLoader.setDelegate(getDelegate());
            setLoader(webappLoader);
        }

        // An explicit cookie processor hasn't been specified; use the default
        if (cookieProcessor == null) {
            cookieProcessor = new Rfc6265CookieProcessor();
        }

        // Initialize character set mapper
        getCharsetMapper();

        // Reading the "catalina.useNaming" environment variable
        String useNamingProperty = System.getProperty("catalina.useNaming");
        if ((useNamingProperty != null)
                && (useNamingProperty.equals("false"))) {
            useNaming = false;
        }

        if (ok && isUseNaming()) {
            if (getNamingContextListener() == null) {
                NamingContextListener ncl = new NamingContextListener();
                ncl.setName(getNamingContextName());
                ncl.setExceptionOnFailedWrite(getJndiExceptionOnFailedWrite());
                addLifecycleListener(ncl);
                setNamingContextListener(ncl);
            }
        }

        // Standard container startup
        if (log.isDebugEnabled()) {
            log.debug("Processing standard container startup");
        }


        // Binding thread
        ClassLoader oldCCL = bindThread();

        try {
            if (ok) {
                // Start our subordinate components, if any
                Loader loader = getLoader();
                if (loader instanceof Lifecycle) {
                    ((Lifecycle) loader).start();
                }

                // since the loader just started, the webapp classloader is now
                // created.
                if (loader.getClassLoader() instanceof WebappClassLoaderBase) {
                    WebappClassLoaderBase cl = (WebappClassLoaderBase) loader.getClassLoader();
                    cl.setClearReferencesRmiTargets(getClearReferencesRmiTargets());
                    cl.setClearReferencesStopThreads(getClearReferencesStopThreads());
                    cl.setClearReferencesStopTimerThreads(getClearReferencesStopTimerThreads());
                    cl.setClearReferencesHttpClientKeepAliveThread(getClearReferencesHttpClientKeepAliveThread());
                    cl.setClearReferencesObjectStreamClassCaches(getClearReferencesObjectStreamClassCaches());
                    cl.setClearReferencesThreadLocals(getClearReferencesThreadLocals());
                }

                // By calling unbindThread and bindThread in a row, we setup the
                // current Thread CCL to be the webapp classloader
                unbindThread(oldCCL);
                oldCCL = bindThread();

                // Initialize logger again. Other components might have used it
                // too early, so it should be reset.
                logger = null;
                getLogger();

                Realm realm = getRealmInternal();
                if(null != realm) {
                    if (realm instanceof Lifecycle) {
                        ((Lifecycle) realm).start();
                    }

                    // Place the CredentialHandler into the ServletContext so
                    // applications can have access to it. Wrap it in a "safe"
                    // handler so application's can't modify it.
                    CredentialHandler safeHandler = new CredentialHandler() {
                        @Override
                        public boolean matches(String inputCredentials, String storedCredentials) {
                            return getRealmInternal().getCredentialHandler().matches(inputCredentials, storedCredentials);
                        }

                        @Override
                        public String mutate(String inputCredentials) {
                            return getRealmInternal().getCredentialHandler().mutate(inputCredentials);
                        }
                    };
                    context.setAttribute(Globals.CREDENTIAL_HANDLER, safeHandler);
                }

                // Notify our interested LifecycleListeners
                fireLifecycleEvent(Lifecycle.CONFIGURE_START_EVENT, null);

                // Start our child containers, if not already started
                for (Container child : findChildren()) {
                    if (!child.getState().isAvailable()) {
                        child.start();
                    }
                }

                // Start the Valves in our pipeline (including the basic),
                // if any
                if (pipeline instanceof Lifecycle) {
                    ((Lifecycle) pipeline).start();
                }

                // Acquire clustered manager
                Manager contextManager = null;
                Manager manager = getManager();
                if (manager == null) {
                    if (log.isDebugEnabled()) {
                        log.debug(sm.getString("standardContext.cluster.noManager",
                                Boolean.valueOf((getCluster() != null)),
                                Boolean.valueOf(distributable)));
                    }
                    if ((getCluster() != null) && distributable) {
                        try {
                            contextManager = getCluster().createManager(getName());
                        } catch (Exception ex) {
                            log.error(sm.getString("standardContext.cluster.managerError"), ex);
                            ok = false;
                        }
                    } else {
                        contextManager = new StandardManager();
                    }
                }

                // Configure default manager if none was specified
                if (contextManager != null) {
                    if (log.isDebugEnabled()) {
                        log.debug(sm.getString("standardContext.manager",
                                contextManager.getClass().getName()));
                    }
                    setManager(contextManager);
                }

                if (manager!=null && (getCluster() != null) && distributable) {
                    //let the cluster know that there is a context that is distributable
                    //and that it has its own manager
                    getCluster().registerManager(manager);
                }
            }

            if (!getConfigured()) {
                log.error(sm.getString("standardContext.configurationFail"));
                ok = false;
            }

            // We put the resources into the servlet context
            if (ok) {
                getServletContext().setAttribute
                        (Globals.RESOURCES_ATTR, getResources());

                if (getInstanceManager() == null) {
                    setInstanceManager(createInstanceManager());
                }
                getServletContext().setAttribute(
                        InstanceManager.class.getName(), getInstanceManager());
                InstanceManagerBindings.bind(getLoader().getClassLoader(), getInstanceManager());

                // Create context attributes that will be required
                getServletContext().setAttribute(
                        JarScanner.class.getName(), getJarScanner());

                // Make the version info available
                getServletContext().setAttribute(Globals.WEBAPP_VERSION, getWebappVersion());
            }

            // Set up the context init params
            mergeParameters();

            // Call ServletContainerInitializers
            for (Map.Entry<ServletContainerInitializer, Set<Class<?>>> entry :
                    initializers.entrySet()) {
                try {
                    entry.getKey().onStartup(entry.getValue(),
                            getServletContext());
                } catch (ServletException e) {
                    log.error(sm.getString("standardContext.sciFail"), e);
                    ok = false;
                    break;
                }
            }

            // Configure and call application event listeners
            if (ok) {
                if (!listenerStart()) {
                    log.error(sm.getString("standardContext.listenerFail"));
                    ok = false;
                }
            }

            // Check constraints for uncovered HTTP methods
            // Needs to be after SCIs and listeners as they may programmatically
            // change constraints
            if (ok) {
                checkConstraintsForUncoveredMethods(findConstraints());
            }

            try {
                // Start manager
                Manager manager = getManager();
                if (manager instanceof Lifecycle) {
                    ((Lifecycle) manager).start();
                }
            } catch(Exception e) {
                log.error(sm.getString("standardContext.managerFail"), e);
                ok = false;
            }

            // Configure and call application filters
            if (ok) {
                if (!filterStart()) {
                    log.error(sm.getString("standardContext.filterFail"));
                    ok = false;
                }
            }

            // Load and initialize all "load on startup" servlets
            if (ok) {
                if (!loadOnStartup(findChildren())){
                    log.error(sm.getString("standardContext.servletFail"));
                    ok = false;
                }
            }

            // Start ContainerBackgroundProcessor thread
            super.threadStart();
        } finally {
            // Unbinding thread
            unbindThread(oldCCL);
        }

        // Set available status depending upon startup success
        if (ok) {
            if (log.isDebugEnabled()) {
                log.debug("Starting completed");
            }
        } else {
            log.error(sm.getString("standardContext.startFailed", getName()));
        }

        startTime=System.currentTimeMillis();

        // Send j2ee.state.running notification
        if (ok && (this.getObjectName() != null)) {
            Notification notification =
                    new Notification("j2ee.state.running", this.getObjectName(),
                            sequenceNumber.getAndIncrement());
            broadcaster.sendNotification(notification);
        }

        // The WebResources implementation caches references to JAR files. On
        // some platforms these references may lock the JAR files. Since web
        // application start is likely to have read from lots of JARs, trigger
        // a clean-up now.
        getResources().gc();

        // Reinitializing if something went wrong
        if (!ok) {
            setState(LifecycleState.FAILED);
            // Send j2ee.object.failed notification
            if (this.getObjectName() != null) {
                Notification notification = new Notification("j2ee.object.failed",
                        this.getObjectName(), sequenceNumber.getAndIncrement());
                broadcaster.sendNotification(notification);
            }
        } else {
            setState(LifecycleState.STARTING);
        }
    }


    /**
     * Merge the context initialization parameters specified in the application
     * deployment descriptor with the application parameters described in the
     * server configuration, respecting the <code>override</code> property of
     * the application parameters appropriately.
     */
    private void mergeParameters() {
        Map<String,String> mergedParams = new HashMap<>();

        String names[] = findParameters();
        for (String s : names) {
            mergedParams.put(s, findParameter(s));
        }

        ApplicationParameter params[] = findApplicationParameters();
        for (ApplicationParameter param : params) {
            if (param.getOverride()) {
                if (mergedParams.get(param.getName()) == null) {
                    mergedParams.put(param.getName(),
                            param.getValue());
                }
            } else {
                mergedParams.put(param.getName(), param.getValue());
            }
        }

        ServletContext sc = getServletContext();
        for (Map.Entry<String,String> entry : mergedParams.entrySet()) {
            sc.setInitParameter(entry.getKey(), entry.getValue());
        }

    }

    /**
     * Unbind thread and restore the specified context classloader.
     *
     * @param oldContextClassLoader the previous classloader
     */
    protected void unbindThread(ClassLoader oldContextClassLoader) {

        if (isUseNaming()) {
            ContextBindings.unbindThread(this, getNamingToken());
        }

        unbind(false, oldContextClassLoader);
    }


    private static class NoPluggabilityServletContext
            implements ServletContext {

        private final ServletContext sc;

        public NoPluggabilityServletContext(ServletContext sc) {
            this.sc = sc;
        }

        @Override
        public String getContextPath() {
            return sc.getContextPath();
        }

        @Override
        public ServletContext getContext(String uripath) {
            return sc.getContext(uripath);
        }

        @Override
        public int getMajorVersion() {
            return sc.getMajorVersion();
        }

        @Override
        public int getMinorVersion() {
            return sc.getMinorVersion();
        }

        @Override
        public int getEffectiveMajorVersion() {
            return sc.getEffectiveMajorVersion();
        }

        @Override
        public int getEffectiveMinorVersion() {
            return sc.getEffectiveMinorVersion();
        }

        @Override
        public String getMimeType(String file) {
            return sc.getMimeType(file);
        }

        @Override
        public Set<String> getResourcePaths(String path) {
            return sc.getResourcePaths(path);
        }

        @Override
        public URL getResource(String path) throws MalformedURLException {
            return sc.getResource(path);
        }

        @Override
        public InputStream getResourceAsStream(String path) {
            return sc.getResourceAsStream(path);
        }

        @Override
        public RequestDispatcher getRequestDispatcher(String path) {
            return sc.getRequestDispatcher(path);
        }

        @Override
        public RequestDispatcher getNamedDispatcher(String name) {
            return sc.getNamedDispatcher(name);
        }

        @Override
        public void log(String msg) {
            sc.log(msg);
        }

        @Override
        public void log(String message, Throwable throwable) {
            sc.log(message, throwable);
        }

        @Override
        public String getRealPath(String path) {
            return sc.getRealPath(path);
        }

        @Override
        public String getServerInfo() {
            return sc.getServerInfo();
        }

        @Override
        public String getInitParameter(String name) {
            return sc.getInitParameter(name);
        }

        @Override
        public Enumeration<String> getInitParameterNames() {
            return sc.getInitParameterNames();
        }

        @Override
        public boolean setInitParameter(String name, String value) {
            throw new UnsupportedOperationException(
                    sm.getString("noPluggabilityServletContext.notAllowed"));
        }

        @Override
        public Object getAttribute(String name) {
            return sc.getAttribute(name);
        }

        @Override
        public Enumeration<String> getAttributeNames() {
            return sc.getAttributeNames();
        }

        @Override
        public void setAttribute(String name, Object object) {
            sc.setAttribute(name, object);
        }

        @Override
        public void removeAttribute(String name) {
            sc.removeAttribute(name);
        }

        @Override
        public String getServletContextName() {
            return sc.getServletContextName();
        }

        @Override
        public ServletRegistration.Dynamic addServlet(String servletName, String className) {
            throw new UnsupportedOperationException(
                    sm.getString("noPluggabilityServletContext.notAllowed"));
        }

        @Override
        public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
            throw new UnsupportedOperationException(
                    sm.getString("noPluggabilityServletContext.notAllowed"));
        }

        @Override
        public ServletRegistration.Dynamic addServlet(String servletName,
                                                      Class<? extends Servlet> servletClass) {
            throw new UnsupportedOperationException(
                    sm.getString("noPluggabilityServletContext.notAllowed"));
        }

        @Override
        public ServletRegistration.Dynamic addJspFile(String jspName, String jspFile) {
            throw new UnsupportedOperationException(
                    sm.getString("noPluggabilityServletContext.notAllowed"));
        }

        @Override
        public <T extends Servlet> T createServlet(Class<T> c)
                throws ServletException {
            throw new UnsupportedOperationException(
                    sm.getString("noPluggabilityServletContext.notAllowed"));
        }

        @Override
        public ServletRegistration getServletRegistration(String servletName) {
            throw new UnsupportedOperationException(
                    sm.getString("noPluggabilityServletContext.notAllowed"));
        }

        @Override
        public Map<String,? extends ServletRegistration> getServletRegistrations() {
            throw new UnsupportedOperationException(
                    sm.getString("noPluggabilityServletContext.notAllowed"));
        }

        @Override
        public jakarta.servlet.FilterRegistration.Dynamic addFilter(
                String filterName, String className) {
            throw new UnsupportedOperationException(
                    sm.getString("noPluggabilityServletContext.notAllowed"));
        }

        @Override
        public jakarta.servlet.FilterRegistration.Dynamic addFilter(
                String filterName, Filter filter) {
            throw new UnsupportedOperationException(
                    sm.getString("noPluggabilityServletContext.notAllowed"));
        }

        @Override
        public jakarta.servlet.FilterRegistration.Dynamic addFilter(
                String filterName, Class<? extends Filter> filterClass) {
            throw new UnsupportedOperationException(
                    sm.getString("noPluggabilityServletContext.notAllowed"));
        }

        @Override
        public <T extends Filter> T createFilter(Class<T> c)
                throws ServletException {
            throw new UnsupportedOperationException(
                    sm.getString("noPluggabilityServletContext.notAllowed"));
        }

        @Override
        public FilterRegistration getFilterRegistration(String filterName) {
            throw new UnsupportedOperationException(
                    sm.getString("noPluggabilityServletContext.notAllowed"));
        }

        @Override
        public Map<String,? extends FilterRegistration> getFilterRegistrations() {
            throw new UnsupportedOperationException(
                    sm.getString("noPluggabilityServletContext.notAllowed"));
        }

        @Override
        public SessionCookieConfig getSessionCookieConfig() {
            throw new UnsupportedOperationException(
                    sm.getString("noPluggabilityServletContext.notAllowed"));
        }

        @Override
        public void setSessionTrackingModes(
                Set<SessionTrackingMode> sessionTrackingModes) {
            throw new UnsupportedOperationException(
                    sm.getString("noPluggabilityServletContext.notAllowed"));
        }

        @Override
        public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
            return sc.getDefaultSessionTrackingModes();
        }

        @Override
        public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
            return sc.getEffectiveSessionTrackingModes();
        }

        @Override
        public void addListener(String className) {
            throw new UnsupportedOperationException(
                    sm.getString("noPluggabilityServletContext.notAllowed"));
        }

        @Override
        public <T extends EventListener> void addListener(T t) {
            throw new UnsupportedOperationException(
                    sm.getString("noPluggabilityServletContext.notAllowed"));
        }

        @Override
        public void addListener(Class<? extends EventListener> listenerClass) {
            throw new UnsupportedOperationException(
                    sm.getString("noPluggabilityServletContext.notAllowed"));
        }

        @Override
        public <T extends EventListener> T createListener(Class<T> c)
                throws ServletException {
            throw new UnsupportedOperationException(
                    sm.getString("noPluggabilityServletContext.notAllowed"));
        }

        @Override
        public JspConfigDescriptor getJspConfigDescriptor() {
            return sc.getJspConfigDescriptor();
        }

        @Override
        public ClassLoader getClassLoader() {
            return sc.getClassLoader();
        }

        @Override
        public void declareRoles(String... roleNames) {
            throw new UnsupportedOperationException(
                    sm.getString("noPluggabilityServletContext.notAllowed"));
        }

        @Override
        public String getVirtualServerName() {
            return sc.getVirtualServerName();
        }

        @Override
        public int getSessionTimeout() {
            return sc.getSessionTimeout();
        }

        @Override
        public void setSessionTimeout(int sessionTimeout) {
            throw new UnsupportedOperationException(
                    sm.getString("noPluggabilityServletContext.notAllowed"));
        }

        @Override
        public String getRequestCharacterEncoding() {
            return sc.getRequestCharacterEncoding();
        }

        @Override
        public void setRequestCharacterEncoding(String encoding) {
            throw new UnsupportedOperationException(
                    sm.getString("noPluggabilityServletContext.notAllowed"));
        }

        @Override
        public String getResponseCharacterEncoding() {
            return sc.getResponseCharacterEncoding();
        }

        @Override
        public void setResponseCharacterEncoding(String encoding) {
            throw new UnsupportedOperationException(
                    sm.getString("noPluggabilityServletContext.notAllowed"));
        }
    }

}
