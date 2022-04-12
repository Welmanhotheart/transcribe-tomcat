package org.apache.tomcat.util.descriptor.web;

public interface NamingResources {

    void addEnvironment(ContextEnvironment ce);
    Object getContainer();
    void addResource(ContextResource cr);
    void removeResourceLink(String name);
    void removeEnvironment(String name);
}
