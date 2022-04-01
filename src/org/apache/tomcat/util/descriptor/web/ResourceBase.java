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

    @Override
    public List<InjectionTarget> getInjectionTargets() {
        return null;
    }
}
