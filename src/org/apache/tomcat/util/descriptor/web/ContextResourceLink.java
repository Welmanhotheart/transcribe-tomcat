package org.apache.tomcat.util.descriptor.web;

public class ContextResourceLink extends ResourceBase {

    private static final long serialVersionUID = 1L;

    // ------------------------------------------------------------- Properties

    /**
     * The global name of this resource.
     */
    private String global = null;
    /**
     * The factory to be used for creating the object
     */
    private String factory = null;

    public String getGlobal() {
        return this.global;
    }

    public void setGlobal(String global) {
        this.global = global;
    }


}
