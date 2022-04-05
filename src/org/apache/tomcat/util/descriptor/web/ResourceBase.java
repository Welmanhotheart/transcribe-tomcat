package org.apache.tomcat.util.descriptor.web;

import java.io.Serializable;
import java.util.List;

public class ResourceBase implements Serializable, Injectable {
    @Override
    public String getName() {
        return null;
    }

    @Override
    public void addInjectionTarget(String injectionTargetName, String jndiName) {

    }

    /**
     * The name of the resource implementation class.
     */
    private String type = null;

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }


    @Override
    public List<InjectionTarget> getInjectionTargets() {
        return null;
    }


    /**
     * The NamingResources with which we are associated (if any).
     */
    private NamingResources resources = null;

    public NamingResources getNamingResources() {
        return this.resources;
    }

    public void setNamingResources(NamingResources resources) {
        this.resources = resources;
    }
}
