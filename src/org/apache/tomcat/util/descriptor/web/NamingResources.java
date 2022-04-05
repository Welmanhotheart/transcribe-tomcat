package org.apache.tomcat.util.descriptor.web;

public interface NamingResources {

    Object getContainer();
    void addResource(ContextResource cr);
}
