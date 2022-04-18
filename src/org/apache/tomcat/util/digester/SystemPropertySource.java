package org.apache.tomcat.util.digester;

import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.tomcat.util.security.PermissionCheck;

import java.security.Permission;
import java.util.PropertyPermission;

public class SystemPropertySource implements IntrospectionUtils.SecurePropertySource{
    @Override
    public String getProperty(String key) {
        return getProperty(key, null);
    }

    @Override
    public String getProperty(String key, ClassLoader classLoader) {
        if (classLoader instanceof PermissionCheck) {
            Permission p = new PropertyPermission(key, "read");
            if (!((PermissionCheck) classLoader).check(p)) {
                return null;
            }
        }
        return System.getProperty(key);
    }
}
