package org.apache.catalina;

public interface WebResourceRoot extends Lifecycle{

    /**
     * @return the web application this WebResourceRoot is associated with.
     */
    Context getContext();

    /**
     * Set the web application this WebResourceRoot is associated with.
     *
     * @param context the associated context
     */
    void setContext(Context context);


    /**
     * This method will be invoked by the context on a periodic basis and allows
     * the implementation a method that executes periodic tasks, such as purging
     * expired cache entries.
     */
    void backgroundProcess();

    /**
     * Obtain the list of all of the WebResources in the specified directory.
     *
     * @param path  The path for the resource of interest relative to the root
     *              of the web application. It must start with '/'.
     *
     * @return  The list of resources. If path does not refer to a directory
     *          then a zero length array will be returned.
     */
    WebResource[] listResources(String path);
}
