package org.apache.tomcat.util.descriptor.web;

public class ContextLocalEjb extends ResourceBase{

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
     * The name of the EJB local implementation class.
     */
    private String local = null;

    public String getLocal() {
        return this.local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

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


}
