package org.apache.catalina;

public interface Loader {


    /**
     * @return the Java class loader to be used by this Container.
     */
    public ClassLoader getClassLoader();


    /**
     * Execute a periodic task, such as reloading, etc. This method will be
     * invoked inside the classloading context of this container. Unexpected
     * throwables will be caught and logged.
     */
    public void backgroundProcess();


    /**
     * Set the Context with which this Loader has been associated.
     *
     * @param context The associated Context
     */
    public void setContext(Context context);


    /**
     * Set the "follow standard delegation model" flag used to configure
     * our ClassLoader.
     *
     * @param delegate The new flag
     */
    public void setDelegate(boolean delegate);

}
