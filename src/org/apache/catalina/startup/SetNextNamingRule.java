package org.apache.catalina.startup;

import org.apache.catalina.Context;
import org.apache.catalina.deploy.NamingResourcesImpl;
import org.apache.tomcat.util.IntrospectionUtils;
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

    /**
     * Process the end of this element.
     *
     * @param namespace the namespace URI of the matching element, or an
     *   empty string if the parser is not namespace aware or the element has
     *   no namespace
     * @param name the local name if the parser is namespace aware, or just
     *   the element name otherwise
     */
    @Override
    public void end(String namespace, String name) throws Exception {

        // Identify the objects to be used
        Object child = digester.peek(0);
        Object parent = digester.peek(1);
        boolean context = false;

        NamingResourcesImpl namingResources = null;
        if (parent instanceof Context) {
            namingResources = ((Context) parent).getNamingResources();
            context = true;
        } else {
            namingResources = (NamingResourcesImpl) parent;
        }

        // Call the specified method
        IntrospectionUtils.callMethod1(namingResources, methodName,
                child, paramType, digester.getClassLoader());

        StringBuilder code = digester.getGeneratedCode();
        if (code != null) {
            if (context) {
                code.append(digester.toVariableName(parent)).append(".getNamingResources()");
            } else {
                code.append(digester.toVariableName(namingResources));
            }
            code.append(".").append(methodName).append('(');
            code.append(digester.toVariableName(child)).append(");");
            code.append(System.lineSeparator());
        }
    }


    /**
     * Render a printable version of this Rule.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SetNextRule[");
        sb.append("methodName=");
        sb.append(methodName);
        sb.append(", paramType=");
        sb.append(paramType);
        sb.append(']');
        return sb.toString();
    }


}
