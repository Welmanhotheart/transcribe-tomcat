package org.apache.catalina.deploy;

import org.apache.catalina.util.LifecycleMBeanBase;
import org.apache.tomcat.util.descriptor.web.NamingResources;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.Serializable;

public class NamingResourcesImpl extends LifecycleMBeanBase
        implements Serializable, NamingResources {

    /**
     * Associated container object.
     */
    private Object container = null;



    @Override
    protected String getObjectNameKeyProperties() {
        return null;
    }

    @Override
    protected String getDomainInternal() {
        return null;
    }

    /**
     * Set the container with which the naming resources are associated.
     * @param container the associated with the resources
     */
    public void setContainer(Object container) {
        this.container = container;
    }

}
