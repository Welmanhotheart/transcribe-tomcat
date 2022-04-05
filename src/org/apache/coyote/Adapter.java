package org.apache.coyote;

public interface Adapter {
    /**
     * Provide the name of the domain to use to register MBeans for components
     * associated with the connector.
     *
     * @return  The MBean domain name
     */
    public String getDomain();
}
