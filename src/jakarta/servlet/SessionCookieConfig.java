package jakarta.servlet;

public interface SessionCookieConfig {


    /**
     * Sets the value for the given session cookie attribute. When a value is
     * set via this method, the value returned by the attribute specific getter
     * (if any) must be consistent with the value set via this method.
     *
     * @param name  Name of attribute to set
     * @param value Value of attribute
     *
     * @throws IllegalStateException if the associated ServletContext has
     *         already been initialised
     *
     * @throws IllegalArgumentException If the attribute name is null or
     *         contains any characters not permitted for use in Cookie names.
     *
     * @throws NumberFormatException If the attribute is known to be numerical
     *         but the provided value cannot be parsed to a number.
     *
     * @since Servlet 6.0
     */
    public void setAttribute(String name, String value);

    /**
     * Obtain the value for a sesison cookie given attribute. Values returned
     * from this method must be consistent with the values set and returned by
     * the attribute specific getters and setters in this class.
     *
     * @param name  Name of attribute to return
     *
     * @return Value of specified attribute
     *
     * @since Servlet 6.0
     */
    public String getAttribute(String name);

    /**
     * Sets the session cookie name.
     *
     * @param name The name of the session cookie
     *
     * @throws IllegalStateException if the associated ServletContext has
     *         already been initialised
     */
    public void setName(String name);

    /**
     * Obtain the name to use for the session cookies.
     *
     * @return the name to use for session cookies.
     */
    public String getName();

}
