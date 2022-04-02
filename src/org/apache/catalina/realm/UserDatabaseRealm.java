package org.apache.catalina.realm;

public class UserDatabaseRealm extends RealmBase{


    /**
     * The global JNDI name of the <code>UserDatabase</code> resource we will be
     * utilizing.
     */
    protected String resourceName = "UserDatabase";


    /**
     * Set the global JNDI name of the <code>UserDatabase</code> resource we
     * will be using.
     *
     * @param resourceName The new global JNDI name
     */
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }


}
