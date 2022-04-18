package org.apache.catalina;

import jakarta.servlet.ServletContext;
import jakarta.servlet.descriptor.JspConfigDescriptor;
import org.apache.catalina.deploy.NamingResourcesImpl;
import org.apache.catalina.org.apache.tomcat.util.descriptor.web.LoginConfig;
import org.apache.jasper.servlet.jakarta.servlet.ServletContainerInitializer;
import org.apache.tomcat.ContextBind;
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.JarScanner;
import org.apache.tomcat.util.descriptor.web.ErrorPage;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;

import java.net.URL;
import java.util.Set;

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
     * @return the override flag for this web application.
     */
    public boolean getOverride();


    /**
     * Set the override flag for this web application.
     *
     * @param override The new override flag
     */
    public void setOverride(boolean override);

    /**
     * Set the "correctly configured" flag for this Context.  This can be
     * set to false by startup listeners that detect a fatal configuration
     * error to avoid the application from being made available.
     *
     * @param configured The new correctly configured flag
     */
    public void setConfigured(boolean configured);

    /**
     * Add a security constraint to the set for this web application.
     *
     * @param constraint The security constraint that should be added
     */
    public void addConstraint(SecurityConstraint constraint);

    /**
     * @return the set of security constraints for this web application.
     * If there are none, a zero-length array is returned.
     */
    public SecurityConstraint[] findConstraints();

    /**
     * Remove the error page for the specified error code or
     * Java language exception, if it exists; otherwise, no action is taken.
     *
     * @param errorPage The error page definition to be removed
     */
    public void removeErrorPage(ErrorPage errorPage);

    /**
     * @return the filter definition for the specified filter name, if any;
     * otherwise return <code>null</code>.
     *
     * @param filterName Filter name to look up
     */
    public FilterDef findFilterDef(String filterName);

    /**
     * @return the set of application listener class names configured
     * for this application.
     */
    public String[] findApplicationListeners();

    /**
     * Remove the specified filter definition from this Context, if it exists;
     * otherwise, no action is taken.
     *
     * @param filterDef Filter definition to be removed
     */
    public void removeFilterDef(FilterDef filterDef);


    /**
     * @return the names of all defined context initialization parameters
     * for this Context.  If no parameters are defined, a zero-length
     * array is returned.
     */
    public String[] findParameters();

    /**
     * @return the set of defined filters for this Context.
     */
    public FilterDef[] findFilterDefs();


    /**
     * @return the extensions for which MIME mappings are defined.  If there
     * are none, a zero-length array is returned.
     */
    public String[] findMimeMappings();

    /**
     * Remove the MIME mapping for the specified extension, if it exists;
     * otherwise, no action is taken.
     *
     * @param extension Extension to remove the mapping for
     */
    public void removeMimeMapping(String extension);


    /**
     * Remove the context initialization parameter with the specified
     * name, if it exists; otherwise, no action is taken.
     *
     * @param name Name of the parameter to remove
     */
    public void removeParameter(String name);

    /**
     * @return the security roles defined for this application.  If none
     * have been defined, a zero-length array is returned.
     */
    public String[] findSecurityRoles();

    /**
     * Remove any security role with the specified name.
     *
     * @param role Security role to remove
     */
    public void removeSecurityRole(String role);

    /**
     * @return the patterns of all defined servlet mappings for this
     * Context.  If no mappings are defined, a zero-length array is returned.
     */
    public String[] findServletMappings();

    /**
     * Remove any servlet mapping for the specified pattern, if it exists;
     * otherwise, no action is taken.
     *
     * @param pattern URL pattern of the mapping to remove
     */
    public void removeServletMapping(String pattern);

    /**
     * @return the set of welcome files defined for this Context.  If none are
     * defined, a zero-length array is returned.
     */
    public String[] findWelcomeFiles();


    /**
     * Remove the specified welcome file name from the list recognized
     * by this Context.
     *
     * @param name Name of the welcome file to be removed
     */
    public void removeWelcomeFile(String name);


    /**
     * @return the set of LifecycleListener classes that will be added to
     * newly created Wrappers automatically.
     */
    public String[] findWrapperLifecycles();


    /**
     * Remove a class name from the set of LifecycleListener classes that
     * will be added to newly created Wrappers.
     *
     * @param listener Class name of a LifecycleListener class to be removed
     */
    public void removeWrapperLifecycle(String listener);

    /**
     * @return the set of ContainerListener classes that will be added to
     * newly created Wrappers automatically.
     */
    public String[] findWrapperListeners();

    /**
     * Add an error page for the specified error or Java exception.
     *
     * @param errorPage The error page definition to be added
     */
    public void addErrorPage(ErrorPage errorPage);


    /**
     * Add a filter definition to this Context.
     *
     * @param filterDef The filter definition to be added
     */
    public void addFilterDef(FilterDef filterDef);


    /**
     * Add a filter mapping to this Context.
     *
     * @param filterMap The filter mapping to be added
     */
    public void addFilterMap(FilterMap filterMap);

    /**
     * Remove a class name from the set of ContainerListener classes that
     * will be added to newly created Wrappers.
     *
     * @param listener Class name of a ContainerListener class to be removed
     */
    public void removeWrapperListener(String listener);

    /**
     * Set the JspConfigDescriptor for this context.
     * A null value indicates there is not JSP configuration.
     *
     * @param descriptor the new JSP configuration
     */
    public void setJspConfigDescriptor(JspConfigDescriptor descriptor);

    /**
     * Add a Locale Encoding Mapping (see Sec 5.4 of Servlet spec 2.4)
     *
     * @param locale locale to map an encoding for
     * @param encoding encoding to be used for a give locale
     */
    public void addLocaleEncodingMappingParameter(String locale, String encoding);

    /**
     * Add a new Listener class name to the set of Listeners
     * configured for this application.
     *
     * @param listener Java class name of a listener class
     */
    public void addApplicationListener(String listener);

    /**
     * Set the effective major version of the Servlet spec used by this
     * context.
     *
     * @param major Set the version number
     */
    public void setEffectiveMajorVersion(int major);

    /**
     * @return the effective minor version of the Servlet spec used by this
     * context.
     */
    public int getEffectiveMinorVersion();


    /**
     * Set the effective minor version of the Servlet spec used by this
     * context.
     *
     * @param minor Set the version number
     */
    public void setEffectiveMinorVersion(int minor);


    /**
     * Add a new context initialization parameter, replacing any existing
     * value for the specified name.
     *
     * @param name Name of the new parameter
     * @param value Value of the new  parameter
     */
    public void addParameter(String name, String value);

    /**
     * Add a ServletContainerInitializer instance to this web application.
     *
     * @param sci       The instance to add
     * @param classes   The classes in which the initializer expressed an
     *                  interest
     */
    public void addServletContainerInitializer(
            ServletContainerInitializer sci, Set<Class<?>> classes);

    /**
     * Return the deny-uncovered-http-methods flag for this web application.
     *
     * @return The current value of the flag
     */
    public boolean getDenyUncoveredHttpMethods();

    /**
     * Return the display name of this web application.
     *
     * @return The display name
     */
    public String getDisplayName();

    /**
     * Set the distributable flag for this web application.
     *
     * @param distributable The new distributable flag
     */
    public void setDistributable(boolean distributable);

    /**
     * Get the distributable flag for this web application.
     *
     * @return The value of the distributable flag for this web application.
     */
    public boolean getDistributable();

    /**
     * Set the display name of this web application.
     *
     * @param displayName The new display name
     */
    public void setDisplayName(String displayName);

    /**
     * Set the deny-uncovered-http-methods flag for this web application.
     *
     * @param denyUncoveredHttpMethods The new deny-uncovered-http-methods flag
     */
    public void setDenyUncoveredHttpMethods(boolean denyUncoveredHttpMethods);

    /**
     * Will the parsing of web.xml and web-fragment.xml files for this Context
     * be performed by a validating parser?
     *
     * @return true if validation is enabled.
     */
    public boolean getXmlValidation();

    /**
     * Will the parsing of web.xml and web-fragment.xml files for this Context
     * be performed by a namespace aware parser?
     *
     * @return true if namespace awareness is enabled.
     */
    public boolean getXmlNamespaceAware();


    /**
     * Determine if annotations parsing is currently disabled
     *
     * @return {@code true} if annotation parsing is disabled for this web
     *         application
     */
    public boolean getIgnoreAnnotations();

    /**
     * Remove a filter mapping from this Context.
     *
     * @param filterMap The filter mapping to be removed
     */
    public void removeFilterMap(FilterMap filterMap);


    /**
     * @return the set of defined error pages for all specified error codes
     * and exception types.
     */
    public ErrorPage[] findErrorPages();
    /**
     * Remove the specified security constraint from this web application.
     *
     * @param constraint Constraint to be removed
     */
    public void removeConstraint(SecurityConstraint constraint);


    /**
     * Add a new security role for this web application.
     *
     * @param role New security role
     */
    public void addSecurityRole(String role);

    /**
     * @return <code>true</code> if the specified security role is defined
     * for this application; otherwise return <code>false</code>.
     *
     * @param role Security role to verify
     */
    public boolean findSecurityRole(String role);





    /**
     * @return the login configuration descriptor for this web application.
     */
    public LoginConfig getLoginConfig();


    /**
     * Set the login configuration descriptor for this web application.
     *
     * @param config The new login configuration
     */
    public void setLoginConfig(LoginConfig config);

    /**
     * @return the {@link Authenticator} that is used by this context. This is
     *         always non-{@code null} for a started Context
     */
    public Authenticator getAuthenticator();


    /**
     * Will the parsing of web.xml, web-fragment.xml, *.tld, *.jspx, *.tagx and
     * tagplugin.xml files for this Context block the use of external entities?
     *
     * @return true if access to external entities is blocked
     */
    public boolean getXmlBlockExternal();

    /**
     * @return the Servlet context for which this Context is a facade.
     */
    public ServletContext getServletContext();

    /**
     * Add a new welcome file to the set recognized by this Context.
     *
     * @param name New welcome file name
     */
    public void addWelcomeFile(String name);

    /**
     * @return the servlet name mapped by the specified pattern (if any);
     * otherwise return <code>null</code>.
     *
     * @param pattern Pattern for which a mapping is requested
     */
    public String findServletMapping(String pattern);

    /**
     * Add a post construct method definition for the given class, if there is
     * an existing definition for the specified class - IllegalArgumentException
     * will be thrown.
     *
     * @param clazz Fully qualified class name
     * @param method
     *            Post construct method name
     * @throws IllegalArgumentException
     *             if the fully qualified class name or method name are
     *             <code>NULL</code>; if there is already post construct method
     *             definition for the given class
     */
    public void addPostConstructMethod(String clazz, String method);


    /**
     * Add a pre destroy method definition for the given class, if there is an
     * existing definition for the specified class - IllegalArgumentException
     * will be thrown.
     *
     * @param clazz Fully qualified class name
     * @param method
     *            Post construct method name
     * @throws IllegalArgumentException
     *             if the fully qualified class name or method name are
     *             <code>NULL</code>; if there is already pre destroy method
     *             definition for the given class
     */
    public void addPreDestroyMethod(String clazz, String method);

    /**
     * Get the Jar Scanner to be used to scan for JAR resources for this
     * context.
     * @return  The Jar Scanner configured for this context.
     */
    public JarScanner getJarScanner();

    /**
     * @return the value of the parallel annotation scanning flag.  If true,
     * it will dispatch scanning to the utility executor.
     */
    public boolean getParallelAnnotationScanning();

    /**
     * Set the parallel annotation scanning value.
     *
     * @param parallelAnnotationScanning new parallel annotation scanning flag
     */
    public void setParallelAnnotationScanning(boolean parallelAnnotationScanning);


    /**
     * Should the effective web.xml for this context be logged on context start?
     *
     * @return true if the reconstructed web.xml that will be used for the
     *   webapp should be logged
     */
    public boolean getLogEffectiveWebXml();

    /**
     * @return the set of filter mappings for this Context.
     */
    public FilterMap[] findFilterMaps();

    /**
     * @return the Resources with which this Context is associated.
     */
    public WebResourceRoot getResources();

    /**
     * Add a resource which will be watched for reloading by the host auto
     * deployer. Note: this will not be used in embedded mode.
     *
     * @param name Path to the resource, relative to docBase
     */
    public void addWatchedResource(String name);

    /**
     * Set the public identifier of the deployment descriptor DTD that is
     * currently being parsed.
     *
     * @param publicId The public identifier
     */
    public void setPublicId(String publicId);

    /**
     * Set the boolean on the annotations parsing for this web
     * application.
     *
     * @param ignoreAnnotations The boolean on the annotations parsing
     */
    public void setIgnoreAnnotations(boolean ignoreAnnotations);

    /**
     * Add a new MIME mapping, replacing any existing mapping for
     * the specified extension.
     *
     * @param extension Filename extension being mapped
     * @param mimeType Corresponding MIME type
     */
    public void addMimeMapping(String extension, String mimeType);

    /**
     * Set the default request body encoding for this web application.
     *
     * @param encoding The default encoding
     */
    public void setRequestCharacterEncoding(String encoding);

    /**
     * Set the default response body encoding for this web application.
     *
     * @param encoding The default encoding
     */
    public void setResponseCharacterEncoding(String encoding);

    /**
     * Get the default response body encoding for this web application.
     *
     * @return The default response body encoding
     */
    public String getResponseCharacterEncoding();

    /**
     * Factory method to create and return a new Wrapper instance, of
     * the Java implementation class appropriate for this Context
     * implementation.  The constructor of the instantiated Wrapper
     * will have been called, but no properties will have been set.
     *
     * @return a newly created wrapper instance that is used to wrap a Servlet
     */
    public Wrapper createWrapper();

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
     * @return the default session timeout (in minutes) for this
     * web application.
     */
    public int getSessionTimeout();


    /**
     * Set the default session timeout (in minutes) for this
     * web application.
     *
     * @param timeout The new default session timeout
     */
    public void setSessionTimeout(int timeout);

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
     * Obtains the regular expression that specifies which container provided
     * SCIs should be filtered out and not used for this context. Matching uses
     * {@link java.util.regex.Matcher#find()} so the regular expression only has
     * to match a sub-string of the fully qualified class name of the container
     * provided SCI for it to be filtered out.
     *
     * @return The regular expression against which the fully qualified class
     *         name of each container provided SCI will be checked
     */
    public String getContainerSciFilter();

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
