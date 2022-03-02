package org.apache.tomcat.util.digester;

import org.apache.catalina.startup.Catalina;
import org.xml.sax.InputSource;
import org.xml.sax.ext.DefaultHandler2;

public class Digester extends DefaultHandler2 {
    private static GeneratedCodeLoader generatedCodeLoader;
    private ArrayStack<Object> stack;
    private Object root = null;

    public static boolean isGeneratedCodeLoaderSet() {
        return false;

    }


    public static void setGeneratedCodeLoader(GeneratedCodeLoader loader) {

    }

    public static Object loadGeneratedClass(String className) {
        if (generatedCodeLoader != null) {
            return generatedCodeLoader.loadGeneratedCode(className);
        }
        return null;

    }

    public void push(Object object) {
        if (stack.size() == 0) {
            root = object;
        }
        stack.push(object);
    }

    public void startGeneratingCode() {

    }

    public void parse(InputSource inputSource) {

    }


    /**
     * TODO what does here mean?
     */
    public interface GeneratedCodeLoader {
        Object loadGeneratedCode(String className);
    }
}
