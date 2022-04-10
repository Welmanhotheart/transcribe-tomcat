package jakarta.servlet;

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
}
