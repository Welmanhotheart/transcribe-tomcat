package jakarta.servlet;

import java.net.MalformedURLException;
import java.net.URL;

public interface ServletContext {

    /**
     * The name of the ServletContext attribute that holds the ordered list of
     * web fragments for this web application.
     *
     * @since Servlet 3.0
     */
    public static final String ORDERED_LIBS = "jakarta.servlet.context.orderedLibs";



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

}
