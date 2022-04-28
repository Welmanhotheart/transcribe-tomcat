package org.apache.catalina.loader;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;
import org.apache.juli.WebappProperties;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.InstrumentableClassLoader;
import org.apache.tomcat.util.compat.JreCompat;
import org.apache.tomcat.util.security.PermissionCheck;

import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessControlException;
import java.security.Permission;
import java.security.Policy;
import java.util.ArrayList;
import java.util.List;

/***
 * TODO
 * @author <a href="zhiwei.wei@bintools.cn">zhiwei.wei</a>
 * @version 1.0.0 2022-04-2022/4/28-下午8:03
 */
public class WebappClassLoaderBase   extends URLClassLoader
        implements Lifecycle, InstrumentableClassLoader, WebappProperties, PermissionCheck {

    private static final Log log = LogFactory.getLog(WebappClassLoaderBase.class);

    /**
     * List of ThreadGroup names to ignore when scanning for web application
     * started threads that need to be shut down.
     */
    private static final List<String> JVM_THREAD_GROUP_NAMES = new ArrayList<>();

    private static final String JVM_THREAD_GROUP_SYSTEM = "system";

    private static final String CLASS_FILE_SUFFIX = ".class";

    static {
        if (!JreCompat.isGraalAvailable()) {
            ClassLoader.registerAsParallelCapable();
        }
        JVM_THREAD_GROUP_NAMES.add(JVM_THREAD_GROUP_SYSTEM);
        JVM_THREAD_GROUP_NAMES.add("RMI Runtime");
    }

    /**
     * The parent class loader.
     */
    protected final ClassLoader parent;

    /**
     * The bootstrap class loader used to load the JavaSE classes. In some
     * implementations this class loader is always <code>null</code> and in
     * those cases {@link ClassLoader#getParent()} will be called recursively on
     * the system class loader and the last non-null result used.
     */
    private ClassLoader javaseClassLoader;

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
     * Instance of the SecurityManager installed.
     */
    protected final SecurityManager securityManager;

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
     * Should Tomcat attempt to clear references to classes loaded by this class
     * loader from the ObjectStreamClass caches?
     */
    private boolean clearReferencesObjectStreamClassCaches = true;


    /**
     * Should Tomcat attempt to clear references to classes loaded by this class
     * loader from ThreadLocals?
     */
    private boolean clearReferencesThreadLocals = true;

    /**
     * Set the clearReferencesHttpClientKeepAliveThread feature for this
     * Context.
     *
     * @param clearReferencesHttpClientKeepAliveThread The new flag value
     */
    public void setClearReferencesHttpClientKeepAliveThread(
            boolean clearReferencesHttpClientKeepAliveThread) {
        this.clearReferencesHttpClientKeepAliveThread =
                clearReferencesHttpClientKeepAliveThread;
    }


    public boolean getClearReferencesObjectStreamClassCaches() {
        return clearReferencesObjectStreamClassCaches;
    }


    /**
     * @return the clearReferencesStopTimerThreads flag for this Context.
     */
    public boolean getClearReferencesStopTimerThreads() {
        return this.clearReferencesStopTimerThreads;
    }


    /**
     * Set the clearReferencesStopTimerThreads feature for this Context.
     *
     * @param clearReferencesStopTimerThreads The new flag value
     */
    public void setClearReferencesStopTimerThreads(
            boolean clearReferencesStopTimerThreads) {
        this.clearReferencesStopTimerThreads = clearReferencesStopTimerThreads;
    }

    // ----------------------------------------------------------- Constructors

    public boolean getClearReferencesRmiTargets() {
        return this.clearReferencesRmiTargets;
    }


    public void setClearReferencesRmiTargets(boolean clearReferencesRmiTargets) {
        this.clearReferencesRmiTargets = clearReferencesRmiTargets;
    }


    /**
     * @return the clearReferencesStopThreads flag for this Context.
     */
    public boolean getClearReferencesStopThreads() {
        return this.clearReferencesStopThreads;
    }


    /**
     * Set the clearReferencesStopThreads feature for this Context.
     *
     * @param clearReferencesStopThreads The new flag value
     */
    public void setClearReferencesStopThreads(
            boolean clearReferencesStopThreads) {
        this.clearReferencesStopThreads = clearReferencesStopThreads;
    }



    public void setClearReferencesObjectStreamClassCaches(
            boolean clearReferencesObjectStreamClassCaches) {
        this.clearReferencesObjectStreamClassCaches = clearReferencesObjectStreamClassCaches;
    }

    public boolean getClearReferencesThreadLocals() {
        return clearReferencesThreadLocals;
    }


    public void setClearReferencesThreadLocals(boolean clearReferencesThreadLocals) {
        this.clearReferencesThreadLocals = clearReferencesThreadLocals;
    }

    /**
     * Construct a new ClassLoader with no defined repositories and no
     * parent ClassLoader.
     */
    protected WebappClassLoaderBase() {

        super(new URL[0]);

        ClassLoader p = getParent();
        if (p == null) {
            p = getSystemClassLoader();
        }
        this.parent = p;

        ClassLoader j = String.class.getClassLoader();
        if (j == null) {
            j = getSystemClassLoader();
            while (j.getParent() != null) {
                j = j.getParent();
            }
        }
        this.javaseClassLoader = j;

        securityManager = System.getSecurityManager();
        if (securityManager != null) {
            refreshPolicy();
        }
    }


    /**
     * Refresh the system policy file, to pick up eventual changes.
     */
    protected void refreshPolicy() {

        try {
            // The policy file may have been modified to adjust
            // permissions, so we're reloading it when loading or
            // reloading a Context
            Policy policy = Policy.getPolicy();
            policy.refresh();
        } catch (AccessControlException e) {
            // Some policy files may restrict this, even for the core,
            // so this exception is ignored
        }

    }

    /**
     * Construct a new ClassLoader with no defined repositories and the given
     * parent ClassLoader.
     * <p>
     * Method is used via reflection -
     * see {@link WebappLoader#createClassLoader()}
     *
     * @param parent Our parent class loader
     */
    protected WebappClassLoaderBase(ClassLoader parent) {

        super(new URL[0], parent);

        ClassLoader p = getParent();
        if (p == null) {
            p = getSystemClassLoader();
        }
        this.parent = p;

        ClassLoader j = String.class.getClassLoader();
        if (j == null) {
            j = getSystemClassLoader();
            while (j.getParent() != null) {
                j = j.getParent();
            }
        }
        this.javaseClassLoader = j;

        securityManager = System.getSecurityManager();
        if (securityManager != null) {
            refreshPolicy();
        }
    }

    @Override
    public LifecycleState getState() {
        return null;
    }

    @Override
    public void addLifecycleListener(LifecycleListener listener) {

    }

    @Override
    public void init() throws LifecycleException {

    }

    @Override
    public void start() throws LifecycleException {

    }

    @Override
    public void stop() throws LifecycleException {

    }

    @Override
    public void destroy() throws LifecycleException {

    }

    @Override
    public boolean check(Permission permission) {
        return false;
    }
}