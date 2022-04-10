package org.apache.tomcat.util.descriptor.web;

import java.io.Serializable;

public class SecurityConstraint  extends XmlEncodingBase implements Serializable {

    /**
     * The set of roles permitted to access resources protected by this
     * security constraint.
     */
    private String authRoles[] = new String[0];

    /**
     * Was an authorization constraint included in this security constraint?
     * This is necessary to distinguish the case where an auth-constraint with
     * no roles (signifying no direct access at all) was requested, versus
     * a lack of auth-constraint which implies no access control checking.
     */
    private boolean authConstraint = false;

    /**
     * The set of web resource collections protected by this security
     * constraint.
     */
    private SecurityCollection collections[] = new SecurityCollection[0];



    /**
     * Return the set of roles that are permitted access to the resources
     * protected by this security constraint.  If none have been defined,
     * a zero-length array is returned (which implies that all authenticated
     * users are permitted access).
     * @return the roles array
     */
    public String[] findAuthRoles() {

        return authRoles;
    }

    /**
     * Return the authorization constraint present flag for this security
     * constraint.
     * @return <code>true</code> if this needs authorization
     */
    public boolean getAuthConstraint() {

        return this.authConstraint;

    }


    /**
     * Set the authorization constraint present flag for this security
     * constraint.
     * @param authConstraint The new value
     */
    public void setAuthConstraint(boolean authConstraint) {

        this.authConstraint = authConstraint;

    }

    /**
     * Return all of the web resource collections protected by this
     * security constraint.  If there are none, a zero-length array is
     * returned.
     * @return the collections array
     */
    public SecurityCollection[] findCollections() {
        return collections;
    }


}
