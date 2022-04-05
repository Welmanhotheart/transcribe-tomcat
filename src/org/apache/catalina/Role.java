package org.apache.catalina;

public interface Role {
    /**
     * @return the role name of this role, which must be unique
     * within the scope of a {@link UserDatabase}.
     */
    public String getRolename();


    /**
     * Set the role name of this role, which must be unique
     * within the scope of a {@link UserDatabase}.
     *
     * @param rolename The new role name
     */
    public void setRolename(String rolename);



    /**
     * @return the {@link UserDatabase} within which this Role is defined.
     */
    public UserDatabase getUserDatabase();

}
