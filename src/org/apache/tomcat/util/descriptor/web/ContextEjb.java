package org.apache.tomcat.util.descriptor.web;

public class ContextEjb extends ResourceBase{

    /**
     * The link to a Jakarta EE EJB definition.
     */
    private String link = null;

    public String getLink() {
        return this.link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    /**
     * The name of the EJB home implementation class.
     */
    private String home = null;

    public String getHome() {
        return this.home;
    }

    public void setHome(String home) {
        this.home = home;
    }

    /**
     * The name of the EJB remote implementation class.
     */
    private String remote = null;

    public String getRemote() {
        return this.remote;
    }

    public void setRemote(String remote) {
        this.remote = remote;
    }

}
