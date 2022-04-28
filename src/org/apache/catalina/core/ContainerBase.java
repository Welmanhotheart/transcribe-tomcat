package org.apache.catalina.core;

import org.apache.catalina.*;
import org.apache.catalina.util.ContextName;
import org.apache.catalina.util.LifecycleMBeanBase;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.MultiThrowable;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.util.threads.InlineExecutorService;

import javax.management.NotificationBroadcasterSupport;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class ContainerBase extends LifecycleMBeanBase implements Container {
    private static final Log log = LogFactory.getLog(ContainerBase.class);

    /**
     * The string manager for this package.
     */
    protected static final StringManager sm = StringManager.getManager(ContainerBase.class);
    /**
     * The human-readable name of this Container.
     */
    protected String name = null;


    /**
     * The parent class loader to be configured when we install a Loader.
     */
    protected ClassLoader parentClassLoader = null;
    /**
     * The Pipeline object with which this Container is associated.
     */
    protected final Pipeline pipeline = new StandardPipeline(this);



    /**
     * The broadcaster that sends j2ee notifications.
     */
    private NotificationBroadcasterSupport broadcaster = null;

    protected ScheduledFuture<?> monitorFuture;

    /**
     * The future allowing control of the background processor.
     */
    protected ScheduledFuture<?> backgroundProcessorFuture;

    /**
     * The processor delay for this component.
     */
    protected int backgroundProcessorDelay = -1;

    /**
     * The distributable flag for this web application.
     */
    private boolean distributable = false;


    /**
     * The Logger implementation with which this Container is associated.
     */
    protected Log logger = null;


    /**
     * Associated logger name.
     */
    protected String logName = null;

    /**
     * The parent Container to which this Container is a child.
     */
    protected Container parent = null;

    /**
     * Will children be started automatically when they are added.
     */
    protected boolean startChildren = true;


    /**
     * The number of threads available to process start and stop events for any
     * children associated with this container.
     */
    private int startStopThreads = 1;
    protected ExecutorService startStopExecutor;


    // ------------------------------------------------------------- Properties

    @Override
    public int getStartStopThreads() {
        return startStopThreads;
    }

    @Override
    public void setStartStopThreads(int startStopThreads) {
        int oldStartStopThreads = this.startStopThreads;
        this.startStopThreads = startStopThreads;

        // Use local copies to ensure thread safety
        if (oldStartStopThreads != startStopThreads && startStopExecutor != null) {
            reconfigureStartStopExecutor(getStartStopThreads());
        }
    }

    @Override
    protected void initInternal() throws LifecycleException {
        reconfigureStartStopExecutor(getStartStopThreads());
        super.initInternal();
    }


    private void reconfigureStartStopExecutor(int threads) {
        if (threads == 1) {
            // Use a fake executor
            if (!(startStopExecutor instanceof InlineExecutorService)) {
                startStopExecutor = new InlineExecutorService();
            }
        } else {
            // Delegate utility execution to the Service
            Server server = Container.getService(this).getServer();
            server.setUtilityThreads(threads);
            startStopExecutor = server.getUtilityExecutor();
        }
    }

    /**
     * Convenience method, intended for use by the digester to simplify the
     * process of adding Valves to containers. See
     * {@link Pipeline#addValve(Valve)} for full details. Components other than
     * the digester should use {@link #getPipeline()}.{@link #addValve(Valve)} in case a
     * future implementation provides an alternative method for the digester to
     * use.
     *
     * @param valve Valve to be added
     *
     * @exception IllegalArgumentException if this Container refused to
     *  accept the specified Valve
     * @exception IllegalArgumentException if the specified Valve refuses to be
     *  associated with this Container
     * @exception IllegalStateException if the specified Valve is already
     *  associated with a different Container
     */
    public synchronized void addValve(Valve valve) {

        pipeline.addValve(valve);
    }

    @Override
    public void addChild(Container child) {
        if (Globals.IS_SECURITY_ENABLED) {
            PrivilegedAction<Void> dp =
                    new PrivilegedAddChild(child);
            AccessController.doPrivileged(dp);
        } else {
            addChildInternal(child);
        }
    }

    /**
     * Add a container event listener to this component.
     *
     * @param listener The listener to add
     */
    @Override
    public void addContainerListener(ContainerListener listener) {
        listeners.add(listener);
    }

    private void addChildInternal(Container child) {

        if (log.isDebugEnabled()) {
            log.debug("Add child " + child + " " + this);
        }

        synchronized(children) {
            if (children.get(child.getName()) != null) {
                throw new IllegalArgumentException(
                        sm.getString("containerBase.child.notUnique", child.getName()));
            }
            child.setParent(this);  // May throw IAE
            children.put(child.getName(), child);
        }

        fireContainerEvent(ADD_CHILD_EVENT, child);

        // Start child
        // Don't do this inside sync block - start can be a slow process and
        // locking the children object can cause problems elsewhere
        try {
            if ((getState().isAvailable() ||
                    LifecycleState.STARTING_PREP.equals(getState())) &&
                    startChildren) {
                child.start();
            }
        } catch (LifecycleException e) {
            throw new IllegalStateException(sm.getString("containerBase.child.start"), e);
        }
    }

    /**
     * The Realm with which this Container is associated.
     */
    private volatile Realm realm = null;

    /**
     * Lock used to control access to the Realm.
     */
    private final ReadWriteLock realmLock = new ReentrantReadWriteLock();

    /**
     * The cluster with which this Container is associated.
     */
    protected Cluster cluster = null;
    private final ReadWriteLock clusterLock = new ReentrantReadWriteLock();

    /**
     * The property change support for this component.
     */
    protected final PropertyChangeSupport support =
            new PropertyChangeSupport(this);

    /**
     * The child Containers belonging to this Container, keyed by name.
     */
    protected final HashMap<String, Container> children = new HashMap<>();

    /**
     * The container event listeners for this Container. Implemented as a
     * CopyOnWriteArrayList since listeners may invoke methods to add/remove
     * themselves or other listeners and with a ReadWriteLock that would trigger
     * a deadlock.
     */
    protected final List<ContainerListener> listeners = new CopyOnWriteArrayList<>();


    @Override
    public void setParentClassLoader(ClassLoader parent) {
        ClassLoader oldParentClassLoader = this.parentClassLoader;
        this.parentClassLoader = parent;
        support.firePropertyChange("parentClassLoader", oldParentClassLoader,
                this.parentClassLoader);

    }

    @Override
    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException(sm.getString("containerBase.nullName"));
        }
        String oldName = this.name;
        this.name = name;
        support.firePropertyChange("name", oldName, this.name);
    }


    /**
     * Return the parent class loader (if any) for this web application.
     * This call is meaningful only <strong>after</strong> a Loader has
     * been configured.
     */
    @Override
    public ClassLoader getParentClassLoader() {
        if (parentClassLoader != null) {
            return parentClassLoader;
        }
        if (parent != null) {
            return parent.getParentClassLoader();
        }
        return ClassLoader.getSystemClassLoader();
    }


    @Override
    public String getName() {
        return name;
    }

    /**
     * Return the Logger for this Container.
     */
    @Override
    public Log getLogger() {
        if (logger != null) {
            return logger;
        }
        logger = LogFactory.getLog(getLogName());
        return logger;
    }

    /**
     * @return the abbreviated name of this container for logging messages
     */
    @Override
    public String getLogName() {

        if (logName != null) {
            return logName;
        }
        String loggerName = null;
        Container current = this;
        while (current != null) {
            String name = current.getName();
            if ((name == null) || (name.equals(""))) {
                name = "/";
            } else if (name.startsWith("##")) {
                name = "/" + name;
            }
            loggerName = "[" + name + "]"
                    + ((loggerName != null) ? ("." + loggerName) : "");
            current = current.getParent();
        }
        logName = ContainerBase.class.getName() + "." + loggerName;
        return logName;

    }


    /**
     * Execute a periodic task, such as reloading, etc. This method will be
     * invoked inside the classloading context of this container. Unexpected
     * throwables will be caught and logged.
     */
    @Override
    public void backgroundProcess() {

        if (!getState().isAvailable()) {
            return;
        }

        Cluster cluster = getClusterInternal();
        if (cluster != null) {
            try {
                cluster.backgroundProcess();
            } catch (Exception e) {
                log.warn(sm.getString("containerBase.backgroundProcess.cluster",
                        cluster), e);
            }
        }
        Realm realm = getRealmInternal();
        if (realm != null) {
            try {
                realm.backgroundProcess();
            } catch (Exception e) {
                log.warn(sm.getString("containerBase.backgroundProcess.realm", realm), e);
            }
        }
        Valve current = pipeline.getFirst();
        while (current != null) {
            try {
                current.backgroundProcess();
            } catch (Exception e) {
                log.warn(sm.getString("containerBase.backgroundProcess.valve", current), e);
            }
            current = current.getNext();
        }
        fireLifecycleEvent(Lifecycle.PERIODIC_EVENT, null);
    }


    /**
     * Return the child Container, associated with this Container, with
     * the specified name (if any); otherwise, return <code>null</code>
     *
     * @param name Name of the child Container to be retrieved
     */
    @Override
    public Container findChild(String name) {
        if (name == null) {
            return null;
        }
        synchronized (children) {
            return children.get(name);
        }
    }

    @Override
    protected void destroyInternal() throws LifecycleException {

        Realm realm = getRealmInternal();
        if (realm instanceof Lifecycle) {
            ((Lifecycle) realm).destroy();
        }
        Cluster cluster = getClusterInternal();
        if (cluster instanceof Lifecycle) {
            ((Lifecycle) cluster).destroy();
        }

        // Stop the Valves in our pipeline (including the basic), if any
        if (pipeline instanceof Lifecycle) {
            ((Lifecycle) pipeline).destroy();
        }

        // Remove children now this container is being destroyed
        for (Container child : findChildren()) {
            removeChild(child);
        }

        // Required if the child is destroyed directly.
        if (parent != null) {
            parent.removeChild(this);
        }

        // If init fails, this may be null
        if (startStopExecutor != null) {
            startStopExecutor.shutdownNow();
        }

        super.destroyInternal();
    }


    /**
     * Set the Realm with which this Container is associated.
     *
     * @param realm The newly associated Realm
     */
    @Override
    public void setRealm(Realm realm) {

        Lock l = realmLock.writeLock();
        l.lock();
        try {
            // Change components if necessary
            Realm oldRealm = this.realm;
            if (oldRealm == realm) {
                return;
            }
            this.realm = realm;

            // Stop the old component if necessary
            if (getState().isAvailable() && (oldRealm != null) &&
                    (oldRealm instanceof Lifecycle)) {
                try {
                    ((Lifecycle) oldRealm).stop();
                } catch (LifecycleException e) {
                    log.error(sm.getString("containerBase.realm.stop"), e);
                }
            }

            // Start the new component if necessary
            if (realm != null) {
                realm.setContainer(this);
            }
            if (getState().isAvailable() && (realm != null) &&
                    (realm instanceof Lifecycle)) {
                try {
                    ((Lifecycle) realm).start();
                } catch (LifecycleException e) {
                    log.error(sm.getString("containerBase.realm.start"), e);
                }
            }

            // Report this property change to interested listeners
            support.firePropertyChange("realm", oldRealm, this.realm);
        } finally {
            l.unlock();
        }

    }


    /**
     * Remove an existing child Container from association with this parent
     * Container.
     *
     * @param child Existing child Container to be removed
     */
    @Override
    public void removeChild(Container child) {

        if (child == null) {
            return;
        }

        try {
            if (child.getState().isAvailable()) {
                child.stop();
            }
        } catch (LifecycleException e) {
            log.error(sm.getString("containerBase.child.stop"), e);
        }

        boolean destroy = false;
        try {
            // child.destroy() may have already been called which would have
            // triggered this call. If that is the case, no need to destroy the
            // child again.
            if (!LifecycleState.DESTROYING.equals(child.getState())) {
                child.destroy();
                destroy = true;
            }
        } catch (LifecycleException e) {
            log.error(sm.getString("containerBase.child.destroy"), e);
        }

        if (!destroy) {
            fireContainerEvent(REMOVE_CHILD_EVENT, child);
        }

        synchronized(children) {
            if (children.get(child.getName()) == null) {
                return;
            }
            children.remove(child.getName());
        }

    }

    /**
     * Notify all container event listeners that a particular event has
     * occurred for this Container.  The default implementation performs
     * this notification synchronously using the calling thread.
     *
     * @param type Event type
     * @param data Event data
     */
    @Override
    public void fireContainerEvent(String type, Object data) {

        if (listeners.size() < 1) {
            return;
        }

        ContainerEvent event = new ContainerEvent(this, type, data);
        // Note for each uses an iterator internally so this is safe
        for (ContainerListener listener : listeners) {
            listener.containerEvent(event);
        }
    }


    /**
     * Return the set of children Containers associated with this Container.
     * If this Container has no children, a zero-length array is returned.
     */
    @Override
    public Container[] findChildren() {
        synchronized (children) {
            Container results[] = new Container[children.size()];
            return children.values().toArray(results);
        }
    }


    /*
     * Provide access to just the cluster component attached to this container.
     */
    protected Cluster getClusterInternal() {
        Lock readLock = clusterLock.readLock();
        readLock.lock();
        try {
            return cluster;
        } finally {
            readLock.unlock();
        }
    }

    protected Realm getRealmInternal() {
        Lock l = realmLock.readLock();
        l.lock();
        try {
            return realm;
        } finally {
            l.unlock();
        }
    }

    @Override
    protected void stopInternal() throws LifecycleException {

    }

    /**
     * Return the Cluster with which this Container is associated.  If there is
     * no associated Cluster, return the Cluster associated with our parent
     * Container (if any); otherwise return <code>null</code>.
     */
    @Override
    public Cluster getCluster() {
        Lock readLock = clusterLock.readLock();
        readLock.lock();
        try {
            if (cluster != null) {
                return cluster;
            }

            if (parent != null) {
                return parent.getCluster();
            }

            return null;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Start this component and implement the requirements
     * of {@link org.apache.catalina.util.LifecycleBase#startInternal()}.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
    @Override
    protected synchronized void startInternal() throws LifecycleException {

        // Start our subordinate components, if any
        logger = null;
        getLogger();
        Cluster cluster = getClusterInternal();
        if (cluster instanceof Lifecycle) {
            ((Lifecycle) cluster).start();
        }
        Realm realm = getRealmInternal();
        if (realm instanceof Lifecycle) {
            ((Lifecycle) realm).start();
        }

        // Start our child containers, if any
        Container children[] = findChildren();
        List<Future<Void>> results = new ArrayList<>();
        for (Container child : children) {
            results.add(startStopExecutor.submit(new StartChild(child)));
        }

        MultiThrowable multiThrowable = null;

        for (Future<Void> result : results) {
            try {
                result.get();
            } catch (Throwable e) {
                log.error(sm.getString("containerBase.threadedStartFailed"), e);
                if (multiThrowable == null) {
                    multiThrowable = new MultiThrowable();
                }
                multiThrowable.add(e);
            }

        }
        if (multiThrowable != null) {
            throw new LifecycleException(sm.getString("containerBase.threadedStartFailed"),
                    multiThrowable.getThrowable());
        }

        // Start the Valves in our pipeline (including the basic), if any
        if (pipeline instanceof Lifecycle) {
            ((Lifecycle) pipeline).start();
        }

        setState(LifecycleState.STARTING);

        // Start our thread
        if (backgroundProcessorDelay > 0) {
            monitorFuture = Container.getService(ContainerBase.this).getServer()
                    .getUtilityExecutor().scheduleWithFixedDelay(
                            new ContainerBackgroundProcessorMonitor(), 0, 60, TimeUnit.SECONDS);
        }
    }



    @Override
    protected String getObjectNameKeyProperties() {
        return null;
    }

    @Override
    protected String getDomainInternal() {
        return null;
    }

    /**
     * Return the Realm with which this Container is associated.  If there is
     * no associated Realm, return the Realm associated with our parent
     * Container (if any); otherwise return <code>null</code>.
     */
    @Override
    public Realm getRealm() {

        Lock l = realmLock.readLock();
        l.lock();
        try {
            if (realm != null) {
                return realm;
            }
            if (parent != null) {
                return parent.getRealm();
            }
            return null;
        } finally {
            l.unlock();
        }
    }


    @Override
    public String getMBeanKeyProperties() {
        Container c = this;
        StringBuilder keyProperties = new StringBuilder();
        int containerCount = 0;

        // Work up container hierarchy, add a component to the name for
        // each container
        while (!(c instanceof Engine)) {
            if (c instanceof Wrapper) {
                keyProperties.insert(0, ",servlet=");
                keyProperties.insert(9, c.getName());
            } else if (c instanceof Context) {
                keyProperties.insert(0, ",context=");
                ContextName cn = new ContextName(c.getName(), false);
                keyProperties.insert(9,cn.getDisplayName());
            } else if (c instanceof Host) {
                keyProperties.insert(0, ",host=");
                keyProperties.insert(6, c.getName());
            } else if (c == null) {
                // May happen in unit testing and/or some embedding scenarios
                keyProperties.append(",container");
                keyProperties.append(containerCount++);
                keyProperties.append("=null");
                break;
            } else {
                // Should never happen...
                keyProperties.append(",container");
                keyProperties.append(containerCount++);
                keyProperties.append('=');
                keyProperties.append(c.getName());
            }
            c = c.getParent();
        }
        return keyProperties.toString();
    }

    /**
     * Return the Pipeline object that manages the Valves associated with
     * this Container.
     */
    @Override
    public Pipeline getPipeline() {
        return this.pipeline;
    }

    /**
     * Return the Container for which this Container is a child, if there is
     * one.  If there is no defined parent, return <code>null</code>.
     */
    @Override
    public Container getParent() {
        return parent;
    }

    @Override
    public File getCatalinaBase() {

        if (parent == null) {
            return null;
        }

        return parent.getCatalinaBase();
    }

    /**
     * Set the parent Container to which this Container is being added as a
     * child.  This Container may refuse to become attached to the specified
     * Container by throwing an exception.
     *
     * @param container Container to which this Container is being added
     *  as a child
     *
     * @exception IllegalArgumentException if this Container refuses to become
     *  attached to the specified Container
     */
    @Override
    public void setParent(Container container) {

        Container oldParent = this.parent;
        this.parent = container;
        support.firePropertyChange("parent", oldParent, this.parent);

    }

    // -------------------- Background Thread --------------------

    /**
     * Start the background thread that will periodically check for
     * session timeouts.
     */
    protected void threadStart() {
        if (backgroundProcessorDelay > 0
                && (getState().isAvailable() || LifecycleState.STARTING_PREP.equals(getState()))
                && (backgroundProcessorFuture == null || backgroundProcessorFuture.isDone())) {
            if (backgroundProcessorFuture != null && backgroundProcessorFuture.isDone()) {
                // There was an error executing the scheduled task, get it and log it
                try {
                    backgroundProcessorFuture.get();
                } catch (InterruptedException | ExecutionException e) {
                    log.error(sm.getString("containerBase.backgroundProcess.error"), e);
                }
            }
            backgroundProcessorFuture = Container.getService(this).getServer().getUtilityExecutor()
                    .scheduleWithFixedDelay(new ContainerBackgroundProcessor(),
                            backgroundProcessorDelay, backgroundProcessorDelay,
                            TimeUnit.SECONDS);
        }
    }




    // ------------------------------- ContainerBackgroundProcessor Inner Class

    protected class ContainerBackgroundProcessorMonitor implements Runnable {
        @Override
        public void run() {
            if (getState().isAvailable()) {
                threadStart();
            }
        }
    }


    /**
     * Perform addChild with the permissions of this class.
     * addChild can be called with the XML parser on the stack,
     * this allows the XML parser to have fewer privileges than
     * Tomcat.
     */
    protected class PrivilegedAddChild implements PrivilegedAction<Void> {

        private final Container child;

        PrivilegedAddChild(Container child) {
            this.child = child;
        }

        @Override
        public Void run() {
            addChildInternal(child);
            return null;
        }

    }

    /**
     * Private runnable class to invoke the backgroundProcess method
     * of this container and its children after a fixed delay.
     */
    protected class ContainerBackgroundProcessor implements Runnable {

        @Override
        public void run() {
            processChildren(ContainerBase.this);
        }

        protected void processChildren(Container container) {
            ClassLoader originalClassLoader = null;

            try {
                if (container instanceof Context) {
                    Loader loader = ((Context) container).getLoader();
                    // Loader will be null for FailedContext instances
                    if (loader == null) {
                        return;
                    }

                    // Ensure background processing for Contexts and Wrappers
                    // is performed under the web app's class loader
                    originalClassLoader = ((Context) container).bind(false, null);
                }
                container.backgroundProcess();
                Container[] children = container.findChildren();
                for (Container child : children) {
                    if (child.getBackgroundProcessorDelay() <= 0) {
                        processChildren(child);
                    }
                }
            } catch (Throwable t) {
                ExceptionUtils.handleThrowable(t);
                log.error(sm.getString("containerBase.backgroundProcess.error"), t);
            } finally {
                if (container instanceof Context) {
                    ((Context) container).unbind(false, originalClassLoader);
                }
            }
        }
    }


    /**
     * Get the delay between the invocation of the backgroundProcess method on
     * this container and its children. Child containers will not be invoked
     * if their delay value is not negative (which would mean they are using
     * their own thread). Setting this to a positive value will cause
     * a thread to be spawn. After waiting the specified amount of time,
     * the thread will invoke the executePeriodic method on this container
     * and all its children.
     */
    @Override
    public int getBackgroundProcessorDelay() {
        return backgroundProcessorDelay;
    }


    // ---------------------------- Inner classes used with start/stop Executor

    private static class StartChild implements Callable<Void> {

        private Container child;

        public StartChild(Container child) {
            this.child = child;
        }

        @Override
        public Void call() throws LifecycleException {
            child.start();
            return null;
        }
    }

    private static class StopChild implements Callable<Void> {

        private Container child;

        public StopChild(Container child) {
            this.child = child;
        }

        @Override
        public Void call() throws LifecycleException {
            if (child.getState().isAvailable()) {
                child.stop();
            }
            return null;
        }
    }


}
