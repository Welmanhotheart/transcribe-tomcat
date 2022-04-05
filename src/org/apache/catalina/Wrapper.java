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


}
