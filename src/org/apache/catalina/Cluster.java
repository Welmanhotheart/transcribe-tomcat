package org.apache.catalina;

public interface Cluster {


    /**
     * Execute a periodic task, such as reloading, etc. This method will be
     * invoked inside the classloading context of this container. Unexpected
     * throwables will be caught and logged.
     */
    public void backgroundProcess();


    /**
     * Create a new manager which will use this cluster to replicate its
     * sessions.
     *
     * @param name Name (key) of the application with which the manager is
     * associated
     *
     * @return The newly created Manager instance
     */
    public Manager createManager(String name);


    /**
     * Register a manager with the cluster. If the cluster is not responsible
     * for creating a manager, then the container will at least notify the
     * cluster that this manager is participating in the cluster.
     * @param manager Manager
     */
    public void registerManager(Manager manager);


}
