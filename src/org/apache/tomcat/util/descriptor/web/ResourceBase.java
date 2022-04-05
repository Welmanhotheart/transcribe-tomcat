package org.apache.tomcat.util.descriptor.web;

import java.io.Serializable;
import java.util.List;

public class ResourceBase implements Serializable, Injectable {
    /**
     * The name of this resource.
     */
    private String name = null;

    @Override
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
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
