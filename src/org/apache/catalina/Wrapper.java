package org.apache.catalina;

import jakarta.servlet.MultipartConfigElement;

public interface Wrapper extends Container {

    /**
     * Sets the overridable attribute for this Servlet.
     *
     * @param overridable the new value
     */
    public void setOverridable(boolean overridable);

    /**
     * Set the async support for the associated Servlet.
     *
     * @param asyncSupport the new value
     */
    public void setAsyncSupported(boolean asyncSupport);

    /**
     * @return the mappings associated with this wrapper.
     */
    public String[] findMappings();

    /**
     * Remove a mapping associated with the wrapper.
     *
     * @param mapping The pattern to remove
     */
    public void removeMapping(String mapping);

    /**
     * Add a mapping associated with the Wrapper.
     *
     * @param mapping The new wrapper mapping
     */
    public void addMapping(String mapping);


    /**
     * @return the security role link for the specified security role
     * reference name, if any; otherwise return <code>null</code>.
     *
     * @param name Security role reference used within this servlet
     */
    public String findSecurityReference(String name);


    /**
     * @return the set of security role reference names associated with
     * this servlet, if any; otherwise return a zero-length array.
     */
    public String[] findSecurityReferences();

    /**
     * @return the run-as identity for this servlet.
     */
    public String getRunAs();

    /**
     * @return the fully qualified servlet class name for this servlet.
     */
    public String getServletClass();

    /**
     * @return the names of all defined initialization parameters for this
     * servlet.
     */
    public String[] findInitParameters();

    /**
     * @return the value for the specified initialization parameter name,
     * if any; otherwise return <code>null</code>.
     *
     * @param name Name of the requested initialization parameter
     */
    public String findInitParameter(String name);

    /**
     * Set the run-as identity for this servlet.
     *
     * @param runAs New run-as identity value
     */
    public void setRunAs(String runAs);

    /**
     * Set the load-on-startup order value (negative value means
     * load on first call).
     *
     * @param value New load-on-startup value
     */
    public void setLoadOnStartup(int value);

    /**
     * Sets the enabled attribute for the associated servlet.
     *
     * @param enabled the new value
     */
    public void setEnabled(boolean enabled);

    /**
     * Add a new servlet initialization parameter for this servlet.
     *
     * @param name Name of this initialization parameter to add
     * @param value Value of this initialization parameter to add
     */
    public void addInitParameter(String name, String value);

    /**
     * Add a new security role reference record to the set of records for
     * this servlet.
     *
     * @param name Role name used within this servlet
     * @param link Role name used within the web application
     */
    public void addSecurityReference(String name, String link);

    /**
     * Set the fully qualified servlet class name for this servlet.
     *
     * @param servletClass Servlet class name
     */
    public void setServletClass(String servletClass);

    /**
     * Set the multi-part configuration for the associated Servlet. To clear the
     * multi-part configuration specify <code>null</code> as the new value.
     *
     * @param multipartConfig The configuration associated with the Servlet
     */
    public void setMultipartConfigElement(
            MultipartConfigElement multipartConfig);

}
