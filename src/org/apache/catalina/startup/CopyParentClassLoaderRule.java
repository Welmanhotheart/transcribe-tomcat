package org.apache.catalina.startup;

import org.apache.catalina.Container;
import org.apache.tomcat.util.digester.Rule;
import org.xml.sax.Attributes;

import java.lang.reflect.Method;

public class CopyParentClassLoaderRule extends Rule {


    /**
     * Handle the beginning of an XML element.
     *
     * @param attributes The attributes of this element
     *
     * @exception Exception if a processing error occurs
     */
    @Override
    public void begin(String namespace, String name, Attributes attributes)
            throws Exception {

        if (digester.getLogger().isDebugEnabled()) {
            digester.getLogger().debug("Copying parent class loader");
        }
        Container child = (Container) digester.peek(0);
        Object parent = digester.peek(1);
        Method method =
                parent.getClass().getMethod("getParentClassLoader", new Class[0]);
        ClassLoader classLoader =
                (ClassLoader) method.invoke(parent, new Object[0]);
        child.setParentClassLoader(classLoader);

        StringBuilder code = digester.getGeneratedCode();
        if (code != null) {
            code.append(digester.toVariableName(child)).append(".setParentClassLoader(");
            code.append(digester.toVariableName(parent)).append(".getParentClassLoader());");
            code.append(System.lineSeparator());
        }
    }

}
