package jakarta.servlet;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

public interface ServletContext {

    /**
     * The name of the ServletContext attribute that holds the ordered list of
     * web fragments for this web application.
     *
     * @since Servlet 3.0
     */
    public static final String ORDERED_LIBS = "jakarta.servlet.context.orderedLibs";

    /**
     * The name of the ServletContext attribute that holds the temporary file
     * location for the web application.
     */
    public static final String TEMPDIR = "jakarta.servlet.context.tempdir";


    /**
     * Binds an object to a given attribute name in this servlet context. If the
     * name specified is already used for an attribute, this method will replace
     * the attribute with the new to the new attribute.
     * <p>
     * If listeners are configured on the <code>ServletContext</code> the
     * container notifies them accordingly.
     * <p>
     * If a null value is passed, the effect is the same as calling
     * <code>removeAttribute()</code>.
     * <p>
     * Attribute names should follow the same convention as package names. The
     * Java Servlet API specification reserves names matching
     * <code>java.*</code>, <code>javax.*</code>, and <code>sun.*</code>.
     *
     * @param name
     *            a <code>String</code> specifying the name of the attribute
     * @param object
     *            an <code>Object</code> representing the attribute to be bound
     * @throws NullPointerException If the provided attribute name is
     *         <code>null</code>
     */
    public void setAttribute(String name, Object object);

    /**
     * Returns a URL to the resource that is mapped to a specified path. The
     * path must begin with a "/" and is interpreted as relative to the current
     * context root.
     * <p>
     * This method allows the servlet container to make a resource available to
     * servlets from any source. Resources can be located on a local or remote
     * file system, in a database, or in a <code>.war</code> file.
     * <p>
     * The servlet container must implement the URL handlers and
     * <code>URLConnection</code> objects that are necessary to access the
     * resource.
     * <p>
     * This method returns <code>null</code> if no resource is mapped to the
     * pathname.
     * <p>
     * Some containers may allow writing to the URL returned by this method
     * using the methods of the URL class.
     * <p>
     * The resource content is returned directly, so be aware that requesting a
     * <code>.jsp</code> page returns the JSP source code. Use a
     * <code>RequestDispatcher</code> instead to include results of an
     * execution.
     * <p>
     * This method has a different purpose than
     * <code>java.lang.Class.getResource</code>, which looks up resources based
     * on a class loader. This method does not use class loaders.
     *
     * @param path
     *            a <code>String</code> specifying the path to the resource
     * @return the resource located at the named path, or <code>null</code> if
     *         there is no resource at that path
     * @exception MalformedURLException
     *                if the pathname is not given in the correct form
     */
    public URL getResource(String path) throws MalformedURLException;

    /**
     * Get the web application class loader associated with this ServletContext.
     *
     * @return The associated web application class loader
     *
     * @throws SecurityException if access to the class loader is prevented by a
     *         SecurityManager
     *
     * @since Servlet 3.0
     */
    public ClassLoader getClassLoader();

    /**
     * Returns the servlet container attribute with the given name, or
     * <code>null</code> if there is no attribute by that name. An attribute
     * allows a servlet container to give the servlet additional information not
     * already provided by this interface. See your server documentation for
     * information about its attributes. A list of supported attributes can be
     * retrieved using <code>getAttributeNames</code>.
     * <p>
     * The attribute is returned as a <code>java.lang.Object</code> or some
     * subclass. Attribute names should follow the same convention as package
     * names. The Java Servlet API specification reserves names matching
     * <code>java.*</code>, <code>javax.*</code>, and <code>sun.*</code>.
     *
     * @param name
     *            a <code>String</code> specifying the name of the attribute
     * @return an <code>Object</code> containing the value of the attribute, or
     *         <code>null</code> if no attribute exists matching the given name
     * @throws NullPointerException If the provided attribute name is
     *         <code>null</code>
     * @see ServletContext#getAttributeNames
     */
    public Object getAttribute(String name);

    /**
     * @return TODO
     * @throws UnsupportedOperationException    If called from a
     *    {@link ServletContextListener#contextInitialized(ServletContextEvent)}
     *    method of a {@link ServletContextListener} that was not defined in a
     *    web.xml file, a web-fragment.xml file nor annotated with
     *    {@link jakarta.servlet.annotation.WebListener}. For example, a
     *    {@link ServletContextListener} defined in a TLD would not be able to
     *    use this method.
     * @since Servlet 3.0 TODO SERVLET3 - Add comments
     */
    public SessionCookieConfig getSessionCookieConfig();

    /**
     * Configures the available session tracking modes for this web application.
     * @param sessionTrackingModes The session tracking modes to use for this
     *        web application
     * @throws IllegalArgumentException
     *             If sessionTrackingModes specifies
     *             {@link SessionTrackingMode#SSL} in combination with any other
     *             {@link SessionTrackingMode}
     * @throws IllegalStateException
     *             If the context has already been initialised
     * @throws UnsupportedOperationException    If called from a
     *    {@link ServletContextListener#contextInitialized(ServletContextEvent)}
     *    method of a {@link ServletContextListener} that was not defined in a
     *    web.xml file, a web-fragment.xml file nor annotated with
     *    {@link jakarta.servlet.annotation.WebListener}. For example, a
     *    {@link ServletContextListener} defined in a TLD would not be able to
     *    use this method.
     * @since Servlet 3.0
     */
    public void setSessionTrackingModes(
            Set<SessionTrackingMode> sessionTrackingModes);

    /**
     * Returns the resource located at the named path as an
     * <code>InputStream</code> object.
     * <p>
     * The data in the <code>InputStream</code> can be of any type or length.
     * The path must be specified according to the rules given in
     * <code>getResource</code>. This method returns <code>null</code> if no
     * resource exists at the specified path.
     * <p>
     * Meta-information such as content length and content type that is
     * available via <code>getResource</code> method is lost when using this
     * method.
     * <p>
     * The servlet container must implement the URL handlers and
     * <code>URLConnection</code> objects necessary to access the resource.
     * <p>
     * This method is different from
     * <code>java.lang.Class.getResourceAsStream</code>, which uses a class
     * loader. This method allows servlet containers to make a resource
     * available to a servlet from any location, without using a class loader.
     *
     * @param path
     *            a <code>String</code> specifying the path to the resource
     * @return the <code>InputStream</code> returned to the servlet, or
     *         <code>null</code> if no resource exists at the specified path
     */
    public InputStream getResourceAsStream(String path);

    /**
     * Return the main path associated with this context.
     *
     * @return The main context path
     *
     * @since Servlet 2.5
     */
    public String getContextPath();


    /**
     * Returns a <code>ServletContext</code> object that corresponds to a
     * specified URL on the server.
     * <p>
     * This method allows servlets to gain access to the context for various
     * parts of the server, and as needed obtain {@link RequestDispatcher}
     * objects from the context. The given path must be begin with "/", is
     * interpreted relative to the server's document root and is matched against
     * the context roots of other web applications hosted on this container.
     * <p>
     * In a security conscious environment, the servlet container may return
     * <code>null</code> for a given URL.
     *
     * @param uripath
     *            a <code>String</code> specifying the context path of another
     *            web application in the container.
     * @return the <code>ServletContext</code> object that corresponds to the
     *         named URL, or null if either none exists or the container wishes
     *         to restrict this access.
     * @see RequestDispatcher
     */
    public ServletContext getContext(String uripath);


    /**
     * Returns the major version of the Java Servlet API that this servlet
     * container supports. All implementations that comply with Version 6.0 must
     * have this method return the integer 6.
     *
     * @return 6
     */
    public int getMajorVersion();

    /**
     * Returns the minor version of the Servlet API that this servlet container
     * supports. All implementations that comply with Version 6.0 must have this
     * method return the integer 0.
     *
     * @return 0
     */
    public int getMinorVersion();

    /**
     * Obtain the major version of the servlet specification for which this web
     * application is implemented.
     *
     * @return The major version declared in web.xml
     *
     * @since Servlet 3.0
     */
    public int getEffectiveMajorVersion();

    /**
     * Obtain the minor version of the servlet specification for which this web
     * application is implemented.
     *
     * @return The minor version declared in web.xml
     *
     * @since Servlet 3.0
     */
    public int getEffectiveMinorVersion();

    /**
     * Returns the MIME type of the specified file, or <code>null</code> if the
     * MIME type is not known. The MIME type is determined by the configuration
     * of the servlet container, and may be specified in a web application
     * deployment descriptor. Common MIME types are <code>"text/html"</code> and
     * <code>"image/gif"</code>.
     *
     * @param file
     *            a <code>String</code> specifying the name of a file
     * @return a <code>String</code> specifying the file's MIME type
     */
    public String getMimeType(String file);


    /**
     * Returns a directory-like listing of all the paths to resources within the
     * web application whose longest sub-path matches the supplied path
     * argument. Paths indicating subdirectory paths end with a '/'. The
     * returned paths are all relative to the root of the web application and
     * have a leading '/'. For example, for a web application containing<br>
     * <br>
     * /welcome.html<br>
     * /catalog/index.html<br>
     * /catalog/products.html<br>
     * /catalog/offers/books.html<br>
     * /catalog/offers/music.html<br>
     * /customer/login.jsp<br>
     * /WEB-INF/web.xml<br>
     * /WEB-INF/classes/com.acme.OrderServlet.class,<br>
     * <br>
     * getResourcePaths("/") returns {"/welcome.html", "/catalog/",
     * "/customer/", "/WEB-INF/"}<br>
     * getResourcePaths("/catalog/") returns {"/catalog/index.html",
     * "/catalog/products.html", "/catalog/offers/"}.<br>
     *
     * @param path
     *            the partial path used to match the resources, which must start
     *            with a /
     * @return a Set containing the directory listing, or null if there are no
     *         resources in the web application whose path begins with the
     *         supplied path.
     * @since Servlet 2.3
     */
    public Set<String> getResourcePaths(String path);


    /**
     * Returns a {@link RequestDispatcher} object that acts as a wrapper for the
     * resource located at the given path. A <code>RequestDispatcher</code>
     * object can be used to forward a request to the resource or to include the
     * resource in a response. The resource can be dynamic or static.
     * <p>
     * The pathname must begin with a "/" and is interpreted as relative to the
     * current context root. Use <code>getContext</code> to obtain a
     * <code>RequestDispatcher</code> for resources in foreign contexts. This
     * method returns <code>null</code> if the <code>ServletContext</code>
     * cannot return a <code>RequestDispatcher</code>.
     *
     * @param path
     *            a <code>String</code> specifying the pathname to the resource
     * @return a <code>RequestDispatcher</code> object that acts as a wrapper for
     *         the resource at the specified path, or <code>null</code> if the
     *         <code>ServletContext</code> cannot return a
     *         <code>RequestDispatcher</code>
     * @see RequestDispatcher
     * @see ServletContext#getContext
     */
    public RequestDispatcher getRequestDispatcher(String path);

    /**
     * Returns a {@link RequestDispatcher} object that acts as a wrapper for the
     * named servlet.
     * <p>
     * Servlets (and JSP pages also) may be given names via server
     * administration or via a web application deployment descriptor. A servlet
     * instance can determine its name using
     * {@link ServletConfig#getServletName}.
     * <p>
     * This method returns <code>null</code> if the <code>ServletContext</code>
     * cannot return a <code>RequestDispatcher</code> for any reason.
     *
     * @param name
     *            a <code>String</code> specifying the name of a servlet to wrap
     * @return a <code>RequestDispatcher</code> object that acts as a wrapper for
     *         the named servlet, or <code>null</code> if the
     *         <code>ServletContext</code> cannot return a
     *         <code>RequestDispatcher</code>
     * @see RequestDispatcher
     * @see ServletContext#getContext
     * @see ServletConfig#getServletName
     */
    public RequestDispatcher getNamedDispatcher(String name);

    /**
     * Writes the specified message to a servlet log file, usually an event log.
     * The name and type of the servlet log file is specific to the servlet
     * container.
     *
     * @param msg
     *            a <code>String</code> specifying the message to be written to
     *            the log file
     */
    public void log(String msg);

    /**
     * Writes an explanatory message and a stack trace for a given
     * <code>Throwable</code> exception to the servlet log file. The name and
     * type of the servlet log file is specific to the servlet container,
     * usually an event log.
     *
     * @param message
     *            a <code>String</code> that describes the error or exception
     * @param throwable
     *            the <code>Throwable</code> error or exception
     */
    public void log(String message, Throwable throwable);

    /**
     * Returns a <code>String</code> containing the real path for a given
     * virtual path. For example, the path "/index.html" returns the absolute
     * file path on the server's filesystem would be served by a request for
     * "http://host/contextPath/index.html", where contextPath is the context
     * path of this ServletContext..
     * <p>
     * The real path returned will be in a form appropriate to the computer and
     * operating system on which the servlet container is running, including the
     * proper path separators. This method returns <code>null</code> if the
     * servlet container cannot translate the virtual path to a real path for
     * any reason (such as when the content is being made available from a
     * <code>.war</code> archive).
     *
     * @param path
     *            a <code>String</code> specifying a virtual path
     * @return a <code>String</code> specifying the real path, or null if the
     *         translation cannot be performed
     */
    public String getRealPath(String path);

}
