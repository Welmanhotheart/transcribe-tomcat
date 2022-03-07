package org.apache.tomcat.util;

import java.util.Hashtable;

public class IntrospectionUtils {

    public static String replaceProperties(String input, Hashtable<Object,Object> staticProp, PropertySource[] source, ClassLoader classLoader) {
        return null;
    }

    /**
     * why it is here TODO
     */
    public static interface PropertySource {
        public String getProperty(String key);
    }
}
