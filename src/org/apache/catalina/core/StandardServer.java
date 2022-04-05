package org.apache.catalina.core;

import org.apache.catalina.*;
import org.apache.catalina.deploy.NamingResourcesImpl;
import org.apache.catalina.mbeans.MBeanFactory;
import org.apache.catalina.startup.Catalina;
import org.apache.catalina.util.LifecycleMBeanBase;
import org.apache.tomcat.util.buf.StringCache;
import org.apache.tomcat.util.threads.TaskThreadFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class StandardServer extends LifecycleMBeanBase implements Server {

    /**
     * The port number on which we wait for shutdown commands.
     */
    private int port = 8005;

    /**
     * The shutdown command string we are looking for.
     */
    private String shutdown = "SHUTDOWN";

    private File catalinaHome = null;

    private File catalinaBase = null;

    private Catalina catalina = null;

    /**
     * Global naming resources.
     */
    private NamingResourcesImpl globalNamingResources = null;

    /**
     * The set of Services associated with this Server.
     */
    private Service services[] = new Service[0];
    private final Object servicesLock = new Object();

    private ObjectName onameStringCache;
    private ObjectName onameMBeanFactory;


    public StandardServer() {
        System.out.println("dasf");
    }

    /**
     * The number of threads available to process utility tasks in this service.
     */
    protected int utilityThreads = 2;


    /**
     * The property change support for this component.
     */
    final PropertyChangeSupport support = new PropertyChangeSupport(this);
    /**
     * The utility threads daemon flag.
     */
    protected boolean utilityThreadsAsDaemon = false;

    /**
     * Utility executor with scheduling capabilities.
     */
    private ScheduledThreadPoolExecutor utilityExecutor = null;

    /**
     * Utility executor wrapper.
     */
    private ScheduledExecutorService utilityExecutorWrapper = null;



    /**
     * The current state of the source component.
     */
    private volatile LifecycleState state = LifecycleState.NEW;

    /**
     * Set the port number we listen to for shutdown commands.
     *
     * @param port The new port number
     */
    @Override
    public void setPort(int port) {
        this.port = port;
    }


    /**
     * Set the shutdown command we are waiting for.
     *
     * @param shutdown The new shutdown command
     */
    @Override
    public void setShutdown(String shutdown) {
        this.shutdown = shutdown;
    }

    @Override
    public Catalina getCatalina() {
        return catalina;
    }

    @Override
    public File getCatalinaBase() {
        if (catalinaBase != null) {
            return catalinaBase;
        }

        catalinaBase = getCatalinaHome();
        return catalinaBase;
    }
    @Override
    public File getCatalinaHome() {
        return catalinaHome;
    }

    /**
     * Set the outer Catalina startup/shutdown component if present.
     */
    @Override
    public void setCatalina(Catalina catalina) {
        this.catalina = catalina;
    }

    @Override
    public void setCatalinaHome(File catalinaHome) {
        this.catalinaHome = catalinaHome;
    }


    @Override
    public void setCatalinaBase(File catalinaBase) {
        this.catalinaBase = catalinaBase;
    }

    @Override
    public ClassLoader getParentClassLoader() {
        return null;
    }

    @Override
    protected final String getObjectNameKeyProperties() {
        return "type=Server";
    }

    /**
     * Obtain the MBean domain for this server. The domain is obtained using
     * the following search order:
     * <ol>
     * <li>Name of first {@link org.apache.catalina.Engine}.</li>
     * <li>Name of first {@link Service}.</li>
     * </ol>
     */
    @Override
    protected String getDomainInternal() {

        String domain = null;

        Service[] services = findServices();
        if (services.length > 0) {
            Service service = services[0];
            if (service != null) {
                domain = service.getDomain();
            }
        }
        return domain;
    }

    private int portOffset = 0;

    @Override
    public int getPortOffset() {
        return portOffset;
    }

    /**
     * Set the global naming resources.
     *
     * @param globalNamingResources The new global naming resources
     */
    @Override
    public void setGlobalNamingResources
    (NamingResourcesImpl globalNamingResources) {

        NamingResourcesImpl oldGlobalNamingResources =
                this.globalNamingResources;
        this.globalNamingResources = globalNamingResources;
        this.globalNamingResources.setContainer(this);
        support.firePropertyChange("globalNamingResources",
                oldGlobalNamingResources,
                this.globalNamingResources);

    }

    /**
     * @return the specified Service (if it exists); otherwise return
     * <code>null</code>.
     *
     * @param name Name of the Service to be returned
     */
    @Override
    public Service findService(String name) {
        if (name == null) {
            return null;
        }
        synchronized (servicesLock) {
            for (Service service : services) {
                if (name.equals(service.getName())) {
                    return service;
                }
            }
        }
        return null;
    }


    /**
     * @return the set of Services defined within this Server.
     */
    @Override
    public Service[] findServices() {
        return services;
    }

    /**
     * Invoke a pre-startup initialization. This is used to allow connectors
     * to bind to restricted ports under Unix operating environments.
     */
    @Override
    protected void initInternal() throws LifecycleException {

        super.initInternal();

        // Initialize utility executor
        reconfigureUtilityExecutor(getUtilityThreadsInternal(utilityThreads));
        register(utilityExecutor, "type=UtilityExecutor");

        // Register global String cache
        // Note although the cache is global, if there are multiple Servers
        // present in the JVM (may happen when embedding) then the same cache
        // will be registered under multiple names
        onameStringCache = register(new StringCache(), "type=StringCache");

        // Register the MBeanFactory
        MBeanFactory factory = new MBeanFactory();
        factory.setContainer(this);
        onameMBeanFactory = register(factory, "type=MBeanFactory");

        // Register the naming resources
        globalNamingResources.init();

        // Initialize our defined Services
        for (Service service : services) {
            service.init();
        }
    }

    /**
     * Handles the special values.
     */
    private static int getUtilityThreadsInternal(int utilityThreads) {
        int result = utilityThreads;
        if (result <= 0) {
            result = Runtime.getRuntime().availableProcessors() + result;
            if (result < 2) {
                result = 2;
            }
        }
        return result;
    }

    private synchronized void reconfigureUtilityExecutor(int threads) {
        // The ScheduledThreadPoolExecutor doesn't use MaximumPoolSize, only CorePoolSize is available
        if (utilityExecutor != null) {
            utilityExecutor.setCorePoolSize(threads);
        } else {
            ScheduledThreadPoolExecutor scheduledThreadPoolExecutor =
                    new ScheduledThreadPoolExecutor(threads,
                            new TaskThreadFactory("Catalina-utility-", utilityThreadsAsDaemon, Thread.MIN_PRIORITY));
            scheduledThreadPoolExecutor.setKeepAliveTime(10, TimeUnit.SECONDS);
            scheduledThreadPoolExecutor.setRemoveOnCancelPolicy(true);
            scheduledThreadPoolExecutor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
            utilityExecutor = scheduledThreadPoolExecutor;
            utilityExecutorWrapper = new org.apache.tomcat.util.threads.ScheduledThreadPoolExecutor(utilityExecutor);
        }
    }



    /**
     * Add a new Service to the set of defined Services.
     *
     * @param service The Service to be added
     */
    @Override
    public void addService(Service service) {

        service.setServer(this);

        synchronized (servicesLock) {
            Service results[] = new Service[services.length + 1];
            System.arraycopy(services, 0, results, 0, services.length);
            results[services.length] = service;
            services = results;

            if (getState().isAvailable()) {
                try {
                    service.start();
                } catch (LifecycleException e) {
                    // Ignore
                }
            }

            // Report this property change to interested listeners
            support.firePropertyChange("service", null, service);
        }

    }

    @Override
    protected void destroyInternal() throws LifecycleException {

    }

    @Override
    protected void stopInternal() throws LifecycleException {

    }

    @Override
    protected void startInternal() throws LifecycleException {

    }
}
