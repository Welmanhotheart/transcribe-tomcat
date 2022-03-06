package org.apache.tomcat.util.digester;

import org.apache.catalina.startup.Catalina;
import org.xml.sax.InputSource;
import org.xml.sax.ext.DefaultHandler2;

import java.util.HashMap;
import java.util.List;

public class Digester extends DefaultHandler2 {
    private static GeneratedCodeLoader generatedCodeLoader;
    private ArrayStack<Object> stack;
    private Object root = null;
    private Rules rules;

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
     * what does here mean?
     * @param b
     */
    public void setValidating(boolean b) {

    }

    /**
     * what does here mean? TODO
     * @param b
     */
    public void setRulesValidation(boolean b) {

    }

    public void setFakeAttributes(HashMap<Class<?>, List<String>> fakeAttributes) {

    }

    public void setUseContextClassLoader(boolean b) {

    }

    public void addObjectCreate(String patter, String className, String attributeName) {

    }

    public void addSetProperties(String pattern) {
        addRule(pattern, new SetPropertiesRule());
    }

    private void addRule(String pattern, Rule rule) {
        rule.setDigester(this);
        getRules().add(pattern, rule);
    }

    private Rules getRules() {
        if (this.rules == null) {
            this.rules = new RulesBase();
            this.rules.setDigester(this);
        }
        return this.rules;
    }


    /**
     * TODO what does here mean?
     */
    public interface GeneratedCodeLoader {
        Object loadGeneratedCode(String className);
    }
}
