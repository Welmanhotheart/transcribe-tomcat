package org.apache.catalina;

import java.net.URL;

public interface WebResourceRoot extends Lifecycle{

    enum ResourceSetType {
        PRE,
        RESOURCE_JAR,
        POST,
        CLASSES_JAR
    }

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

    /**
     * Creates a new {@link WebResourceSet} for this {@link WebResourceRoot}
     * based on the provided parameters.
     *
     * @param type          The type of {@link WebResourceSet} to create
     * @param webAppMount   The path within the web application that the
     *                          resources should be published at. It must start
     *                          with '/'.
     * @param url           The URL of the resource (must locate a JAR, file or
     *                          directory)
     * @param internalPath  The path within the resource where the content is to
     *                          be found. It must start with '/'.
     */
    void createWebResourceSet(ResourceSetType type, String webAppMount, URL url,
                              String internalPath);

    /**
     * Creates a new {@link WebResourceSet} for this {@link WebResourceRoot}
     * based on the provided parameters.
     *
     * @param type          The type of {@link WebResourceSet} to create
     * @param webAppMount   The path within the web application that the
     *                          resources should be published at. It must start
     *                          with '/'.
     * @param base          The location of the resources
     * @param archivePath   The path within the resource to the archive where
     *                          the content is to be found. If there is no
     *                          archive then this should be <code>null</code>.
     * @param internalPath  The path within the archive (or the resource if the
     *                          archivePath is <code>null</code> where the
     *                          content is to be found. It must start with '/'.
     */
    void createWebResourceSet(ResourceSetType type, String webAppMount,
                              String base, String archivePath, String internalPath);

}
