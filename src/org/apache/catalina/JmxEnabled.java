package org.apache.catalina;

import javax.management.MBeanRegistration;

public interface JmxEnabled extends MBeanRegistration {

    /**
     * @return the domain under which this component will be / has been
     * registered.
     */
    String getDomain();

}
