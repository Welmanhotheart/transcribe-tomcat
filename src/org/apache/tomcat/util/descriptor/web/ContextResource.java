package org.apache.tomcat.util.descriptor.web;

public class ContextResource extends ResourceBase{

    /**
     * The authorization requirement for this resource
     * (<code>Application</code> or <code>Container</code>).
     */
    private String auth = null;

    public String getAuth() {
        return this.auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    /**
     * The sharing scope of this resource factory (<code>Shareable</code>
     * or <code>Unshareable</code>).
     */
    private String scope = "Shareable";

    public String getScope() {
        return this.scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }


}
