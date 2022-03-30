package org.apache.catalina.startup;

import org.apache.tomcat.util.digester.Rule;

public class SetNextNamingRule extends Rule {
    public SetNextNamingRule(String methodName,
                             String paramType) {

        this.methodName = methodName;
        this.paramType = paramType;

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The method name to call on the parent object.
     */
    protected final String methodName;


    /**
     * The Java class name of the parameter type expected by the method.
     */
    protected final String paramType;


}
