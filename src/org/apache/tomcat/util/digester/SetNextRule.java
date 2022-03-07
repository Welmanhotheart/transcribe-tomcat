package org.apache.tomcat.util.digester;

public class SetNextRule extends Rule{
    protected String paramType = null;
    protected String methodName = null;

    public SetNextRule(String methodName, String paramType) {
        this.methodName = methodName;
        this.paramType = paramType;
    }
}
