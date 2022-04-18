package org.apache.tomcat.util.security;

import java.security.Permission;

public interface PermissionCheck {
    /**
     * Does this component have the given permission?
     *
     * @param permission The permission to test
     *
     * @return {@code false} if a SecurityManager is enabled and the component
     *         does not have the given permission, otherwise {@code true}
     */
    boolean check(Permission permission);
}
