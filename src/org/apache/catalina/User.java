package org.apache.catalina;

public interface User {

    /**
     * @return the {@link UserDatabase} within which this User is defined.
     */
    public UserDatabase getUserDatabase();


    /**
     * @return the logon username of this user, which must be unique
     * within the scope of a {@link UserDatabase}.
     */
    public String getUsername();


    /**
     * Set the logon username of this user, which must be unique within
     * the scope of a {@link UserDatabase}.
     *
     * @param username The new logon username
     */
    public void setUsername(String username);


}
