package org.apache.catalina;

import org.apache.catalina.deploy.NamingResourcesImpl;
import org.apache.tomcat.ContextBind;
import org.apache.tomcat.InstanceManager;

import java.net.URL;

public interface Context extends Container, ContextBind {
    /**
     * @return the context path for this web application.
     */
    public String getPath();


    /**
     * Set the context path for this web application.
     *
     * @param path The new context path
     */
    public void setPath(String path);


    /**
     * Set the "correctly configured" flag for this Context.  This can be
     * set to false by startup listeners that detect a fatal configuration
     * error to avoid the application from being made available.
     *
     * @param configured The new correctly configured flag
     */
    public void setConfigured(boolean configured);


    /**
     * @return the Resources with which this Context is associated.
     */
    public WebResourceRoot getResources();


    /**
     * Set the Resources object with which this Context is associated.
     *
     * @param resources The newly associated Resources
     */
    public void setResources(WebResourceRoot resources);

    /**
     * @return the instance manager associated with this context.
     */
    public InstanceManager getInstanceManager();

    /**
     * Set the instance manager associated with this context.
     *
     * @param instanceManager the new instance manager instance
     */
    public void setInstanceManager(InstanceManager instanceManager);


    /**
     * Return the URL of the XML descriptor for this context.
     *
     * @return The URL of the XML descriptor for this context
     */
    public URL getConfigFile();

    /**
     * @return the Loader with which this Context is associated.
     */
    public Loader getLoader();

    /**
     * Set the Loader with which this Context is associated.
     *
     * @param loader The newly associated loader
     */
    public void setLoader(Loader loader);

    /**
     * Set the URL of the XML descriptor for this context.
     *
     * @param configFile The URL of the XML descriptor for this context.
     */
    public void setConfigFile(URL configFile);


    /**
     * @return The version of this web application, used to differentiate
     * different versions of the same web application when using parallel
     * deployment. If not specified, defaults to the empty string.
     */
    public String getWebappVersion();


    /**
     * Set the version of this web application - used to differentiate
     * different versions of the same web application when using parallel
     * deployment.
     *
     * @param webappVersion The webapp version associated with the context,
     *    which should be unique
     */
    public void setWebappVersion(String webappVersion);

    /**
     * Obtain the document root for this Context.
     *
     * @return An absolute pathname or a relative (to the Host's appBase)
     *         pathname.
     */
    public String getDocBase();

    /**
     * Set the document root for this Context. This can be either an absolute
     * pathname or a relative pathname. Relative pathnames are relative to the
     * containing Host's appBase.
     *
     * @param docBase The new document root
     */
    public void setDocBase(String docBase);


    /**
     * @return the set of watched resources for this Context. If none are
     * defined, a zero length array will be returned.
     */
    public String[] findWatchedResources();


    /**
     * Remove a LifecycleEvent listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removeLifecycleListener(LifecycleListener listener);

    /**
     * Reload this web application, if reloading is supported.
     *
     * @exception IllegalStateException if the <code>reloadable</code>
     *  property is set to <code>false</code>.
     */
    public void reload();



    /**
     * @return the Manager with which this Context is associated.  If there is
     * no associated Manager, return <code>null</code>.
     */
    public Manager getManager();


    /**
     * @return the naming resources associated with this web application.
     */
    public NamingResourcesImpl getNamingResources();



    /**
     * Set the Manager with which this Context is associated.
     *
     * @param manager The newly associated Manager
     */
    public void setManager(Manager manager);

    /**
     * Add a new servlet mapping, replacing any existing mapping for
     * the specified pattern.
     *
     * @param pattern URL pattern to be mapped
     * @param name Name of the corresponding servlet to execute
     * @param jspWildcard true if name identifies the JspServlet
     * and pattern contains a wildcard; false otherwise
     */
    public void addServletMappingDecoded(String pattern, String name,
                                         boolean jspWildcard);


    /**
     * Add a new servlet mapping, replacing any existing mapping for
     * the specified pattern.
     *
     * @param pattern URL pattern to be mapped
     * @param name Name of the corresponding servlet to execute
     */
    public default void addServletMappingDecoded(String pattern, String name) {
        addServletMappingDecoded(pattern, name, false);
    }

    /**
     * Is this context using version 2.2 of the Servlet spec?
     *
     * @return <code>true</code> for a legacy Servlet 2.2 webapp
     */
    boolean isServlet22();

}
