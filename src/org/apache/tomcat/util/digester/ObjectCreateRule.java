package org.apache.tomcat.util.digester;

import org.apache.tomcat.util.res.StringManager;
import org.xml.sax.Attributes;

public class ObjectCreateRule extends Rule{

    protected static final StringManager sm = StringManager.getManager(Rule.class);


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

    @Override
    public void begin(String namespace, String name, Attributes attributes)
            throws Exception {

        String realClassName = getRealClassName(attributes);

        if (realClassName == null) {
            throw new NullPointerException(sm.getString("rule.noClassName", namespace, name));
        }

        // Instantiate the new object and push it on the context stack
        Class<?> clazz = digester.getClassLoader().loadClass(realClassName);
        Object instance = clazz.getConstructor().newInstance();
        digester.push(instance);//TODO, here have to understand it logic

        StringBuilder code = digester.getGeneratedCode();
        if (code != null) {
            code.append(System.lineSeparator());
            code.append(System.lineSeparator());
            code.append(realClassName).append(' ').append(digester.toVariableName(instance)).append(" = new ");
            code.append(realClassName).append("();").append(System.lineSeparator());
        }
    }

    @Override
    public void end(String namespace, String name) throws Exception {

        Object top = digester.pop();
        if (digester.log.isDebugEnabled()) {
            digester.log.debug("[ObjectCreateRule]{" + digester.match +
                    "} Pop " + top.getClass().getName());
        }

    }

    /**
     * Return the actual class name of the class to be instantiated.
     * @param attributes The attribute list for this element
     * @return the class name
     */
    protected String getRealClassName(Attributes attributes) {
        // Identify the name of the class to instantiate
        String realClassName = className;
        if (attributeName != null) {
            String value = attributes.getValue(attributeName);
            if (value != null) {
                realClassName = value;
            }
        }
        return realClassName;
    }



}
