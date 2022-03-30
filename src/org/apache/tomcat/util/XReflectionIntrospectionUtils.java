package org.apache.tomcat.util;

public class XReflectionIntrospectionUtils {

    static boolean isEnabled() {
        return false;
    }

    /**
     * Always throws {@link UnsupportedOperationException}
     *
     * @param o                 Unused
     * @param name              Unused
     * @param value             Unused
     * @param invokeSetProperty Unused
     *
     * @return Never returns normally
     */
    static boolean setPropertyInternal(Object o, String name, String value, boolean invokeSetProperty) {
        throw new UnsupportedOperationException();
    }

    /**
     * Always throws {@link UnsupportedOperationException}
     *
     * @param o     Unused
     * @param name  Unused
     *
     * @return Never returns normally
     */
    static Object getPropertyInternal(Object o, String name) {
        throw new UnsupportedOperationException();
    }

}
