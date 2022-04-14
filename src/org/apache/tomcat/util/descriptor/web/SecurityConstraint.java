package org.apache.tomcat.util.descriptor.web;

import java.io.Serializable;
import java.util.Arrays;

public class SecurityConstraint  extends XmlEncodingBase implements Serializable {

    public static final String ROLE_ALL_AUTHENTICATED_USERS = "**";


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
     * Was the "all authenticated users" wildcard -
     * {@link #ROLE_ALL_AUTHENTICATED_USERS} - included in the authorization
     * constraints for this security constraint?
     */
    private boolean authenticatedUsers = false;

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

    /**
     * The user data constraint for this security constraint.  Must be NONE,
     * INTEGRAL, or CONFIDENTIAL.
     */
    private String userConstraint = "NONE";

    /**
     * The display name of this security constraint.
     */
    private String displayName = null;


    /**
     * @return the display name of this security constraint.
     */
    public String getDisplayName() {

        return this.displayName;

    }


    /**
     * Set the display name of this security constraint.
     * @param displayName The new value
     */
    public void setDisplayName(String displayName) {

        this.displayName = displayName;

    }

    /**
     * Return the user data constraint for this security constraint.
     * @return the user constraint
     */
    public String getUserConstraint() {

        return userConstraint;

    }


    /**
     * Set the user data constraint for this security constraint.
     *
     * @param userConstraint The new user data constraint
     */
    public void setUserConstraint(String userConstraint) {

        if (userConstraint != null) {
            this.userConstraint = userConstraint;
        }

    }

    /**
     * Called in the unlikely event that an application defines a role named
     * "**".
     */
    public void treatAllAuthenticatedUsersAsApplicationRole() {
        if (authenticatedUsers) {
            authenticatedUsers = false;

            String[] results = Arrays.copyOf(authRoles, authRoles.length + 1);
            results[authRoles.length] = ROLE_ALL_AUTHENTICATED_USERS;
            authRoles = results;
            authConstraint = true;
        }
    }


}
