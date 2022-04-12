package org.apache.catalina;

import org.apache.catalina.deploy.NamingResourcesImpl;
import org.apache.catalina.startup.Catalina;

import java.io.File;
import java.util.concurrent.ScheduledExecutorService;

public interface Server extends Lifecycle{

    /**
     * Set the port number we listen to for shutdown commands.
     *
     * @param port The new port number
     *
     * @see #setPortOffset(int)
     */
    public void setPort(int port);

    /**
     * Get the number that offsets the port used for shutdown commands.
     * For example, if port is 8005, and portOffset is 1000,
     * the server listens at 9005.
     *
     * @return the port offset
     */
    public int getPortOffset();

    /**
     * Wait until a proper shutdown command is received, then return.
     */
    public void await();


    /**
     * @return the utility executor managed by the Service.
     */
    public ScheduledExecutorService getUtilityExecutor();

    /**
     * Get the utility thread count.
     * @return the thread count
     */
    public int getUtilityThreads();


    /**
     * Set the utility thread count.
     * @param utilityThreads the new thread count
     */
    public void setUtilityThreads(int utilityThreads);



    /**
     * Set the shutdown command we are waiting for.
     *
     * @param shutdown The new shutdown command
     */
    public void setShutdown(String shutdown);

    public Catalina getCatalina();

    /**
     * @return the configured base (instance) directory. Note that home and base
     * may be the same (and are by default). If this is not set the value
     * returned by {@link #getCatalinaHome()} will be used.
     */
    public File getCatalinaBase();

    /**
     * Add a new Service to the set of defined Services.
     *
     * @param service The Service to be added
     */
    public void addService(Service service);


    /**
     * @return the configured home (binary) directory. Note that home and base
     * may be the same (and are by default).
     */
    public File getCatalinaHome();


    void setCatalina(Catalina catalina);

    void setCatalinaHome(File catalinaHomeFile);

    void setCatalinaBase(File catalinaBaseFile);

    /**
     * @return the parent class loader for this component. If not set, return
     * {@link #getCatalina()} {@link Catalina#getParentClassLoader()}. If
     * catalina has not been set, return the system class loader.
     */
    public ClassLoader getParentClassLoader();

    /**
     * @return the port number we listen to for shutdown commands.
     *
     * @see #getPortOffset()
     * @see #getPortWithOffset()
     */
    public int getPort();

    /**
     * Get the actual port on which server is listening for the shutdown commands.
     * If you do not set port offset, port is returned. If you set
     * port offset, port offset + port is returned.
     *
     * @return the port with offset
     */
    public int getPortWithOffset();

    /**
     * Set the global naming resources.
     *
     * @param globalNamingResources The new global naming resources
     */
    public void setGlobalNamingResources
    (NamingResourcesImpl globalNamingResources);

    // ------------------------------------------------------------- Properties

    /**
     * @return the global naming resources.
     */
    public NamingResourcesImpl getGlobalNamingResources();

    /**
     * Find the specified Service
     *
     * @param name Name of the Service to be returned
     * @return the specified Service, or <code>null</code> if none exists.
     */
    public Service findService(String name);


    /**
     * @return the set of Services defined within this Server.
     */
    public Service[] findServices();


}
