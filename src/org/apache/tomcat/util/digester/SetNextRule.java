package org.apache.tomcat.util.digester;

import org.apache.tomcat.util.IntrospectionUtils;

public class SetNextRule extends Rule{
    protected String paramType = null;
    protected String methodName = null;

    public SetNextRule(String methodName, String paramType) {
        this.methodName = methodName;
        this.paramType = paramType;
    }


    @Override
    public void end(String namespace, String name) throws Exception {

        // Identify the objects to be used
        Object child = digester.peek(0);
        Object parent = digester.peek(1);
        if (digester.log.isDebugEnabled()) {
            if (parent == null) {
                digester.log.debug("[SetNextRule]{" + digester.match +
                        "} Call [NULL PARENT]." +
                        methodName + "(" + child + ")");
            } else {
                digester.log.debug("[SetNextRule]{" + digester.match +
                        "} Call " + parent.getClass().getName() + "." +
                        methodName + "(" + child + ")");
            }
        }

        // Call the specified method
        IntrospectionUtils.callMethod1(parent, methodName,
                child, paramType, digester.getClassLoader());

        StringBuilder code = digester.getGeneratedCode();
        if (code != null) {
            code.append(digester.toVariableName(parent)).append('.');
            code.append(methodName).append('(').append(digester.toVariableName(child)).append(");");
            code.append(System.lineSeparator());
        }
    }

}
