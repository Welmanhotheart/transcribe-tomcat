package org.apache.tomcat.util.descriptor.web;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

    /**
     * Holder for our configured properties.
     */
    private final Map<String, Object> properties = new HashMap<>();

    /**
     * @param name The property name
     * @return a configured property.
     */
    public Object getProperty(String name) {
        return properties.get(name);
    }

    /**
     * Set a configured property.
     * @param name The property name
     * @param value The property value
     */
    public void setProperty(String name, Object value) {
        properties.put(name, value);
    }

    /**
     * Remove a configured property.
     * @param name The property name
     */
    public void removeProperty(String name) {
        properties.remove(name);
    }

    /**
     * List properties.
     * @return the property names iterator
     */
    public Iterator<String> listProperties() {
        return properties.keySet().iterator();
    }

    /**
     * The description of this resource.
     */
    private String description = null;

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private String lookupName = null;

    public String getLookupName() {
        return lookupName;
    }

    public void setLookupName(String lookupName) {
        if (lookupName == null || lookupName.length() == 0) {
            this.lookupName = null;
            return;
        }
        this.lookupName = lookupName;
    }
}
