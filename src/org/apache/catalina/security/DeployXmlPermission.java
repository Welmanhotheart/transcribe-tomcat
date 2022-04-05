package org.apache.catalina.security;

import java.security.BasicPermission;

public class DeployXmlPermission extends BasicPermission {
    public DeployXmlPermission(String name) {
        super(name);
    }
}
