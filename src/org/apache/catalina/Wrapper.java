package org.apache.catalina;

public interface Wrapper extends Container {


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

}
