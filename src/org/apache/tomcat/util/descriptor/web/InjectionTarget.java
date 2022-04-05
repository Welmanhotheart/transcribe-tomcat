package org.apache.tomcat.util.descriptor.web;

import java.io.Serializable;

public class InjectionTarget implements Serializable {

    private static final long serialVersionUID = 1L;

    private String targetClass;
    private String targetName;


    public String getTargetClass() {
        return targetClass;
    }

    public String getTargetName() {
        return targetName;
    }
}
