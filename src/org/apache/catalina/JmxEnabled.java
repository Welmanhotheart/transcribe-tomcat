package org.apache.catalina;

import javax.management.MBeanRegistration;

public interface JmxEnabled extends MBeanRegistration {

    /**
     * @return the domain under which this component will be / has been
     * registered.
     */
    String getDomain();

    /**
     * Specify the domain under which this component should be registered. Used
     * with components that cannot (easily) navigate the component hierarchy to
     * determine the correct domain to use.
     *
     * @param domain The name of the domain under which this component should be
     *               registered
     */
    void setDomain(String domain);

}
