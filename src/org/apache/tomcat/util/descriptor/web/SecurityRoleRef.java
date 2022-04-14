package org.apache.tomcat.util.descriptor.web;

import java.io.Serializable;

public class SecurityRoleRef implements Serializable {

    /**
     * The (required) role name.
     */
    private String name = null;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The optional role link.
     */
    private String link = null;

    public String getLink() {
        return this.link;
    }

    public void setLink(String link) {
        this.link = link;
    }


}
