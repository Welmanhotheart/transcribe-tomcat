package org.apache.tomcat.util.digester;

public class ObjectCreateRule extends Rule{
    protected String attributeName;
    protected String className;

    /**
     * Construct an object create rule with the specified class name.
     *
     * @param className Java class name of the object to be created
     */
    public ObjectCreateRule(String className) {

        this(className, null);

    }

    public ObjectCreateRule(String className,
                            String attributeName) {

        this.className = className;
        this.attributeName = attributeName;

    }

}
