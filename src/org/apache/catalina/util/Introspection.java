package org.apache.catalina.util;

import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.juli.logging.Log;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.res.StringManager;

import java.beans.Introspector;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class Introspection {

    private static final StringManager sm =
            StringManager.getManager("org.apache.catalina.util");

    /**
     * Attempt to load a class using the given Container's class loader. If the
     * class cannot be loaded, a debug level log message will be written to the
     * Container's log and null will be returned.
     * @param context The class loader of this context will be used to attempt
     *  to load the class
     * @param className The class name
     * @return the loaded class or <code>null</code> if loading failed
     */
    public static Class<?> loadClass(Context context, String className) {
        ClassLoader cl = context.getLoader().getClassLoader();
        Log log = context.getLogger();
        Class<?> clazz = null;
        try {
            clazz = cl.loadClass(className);
        } catch (ClassNotFoundException | NoClassDefFoundError | ClassFormatError e) {
            log.debug(sm.getString("introspection.classLoadFailed", className), e);
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            log.debug(sm.getString("introspection.classLoadFailed", className), t);
        }
        return clazz;
    }


    /**
     * Obtain the declared methods for a class taking account of any security
     * manager that may be configured.
     * @param clazz The class to introspect
     * @return the class methods as an array
     */
    public static Method[] getDeclaredMethods(final Class<?> clazz) {
        Method[] methods = null;
        if (Globals.IS_SECURITY_ENABLED) {
            methods = AccessController.doPrivileged((PrivilegedAction<Method[]>) clazz::getDeclaredMethods);
        } else {
            methods = clazz.getDeclaredMethods();
        }
        return methods;
    }

    /**
     * Converts the primitive type to its corresponding wrapper.
     *
     * @param clazz
     *            Class that will be evaluated
     * @return if the parameter is a primitive type returns its wrapper;
     *         otherwise returns the same class
     */
    public static Class<?> convertPrimitiveType(Class<?> clazz) {
        if (clazz.equals(char.class)) {
            return Character.class;
        } else if (clazz.equals(int.class)) {
            return Integer.class;
        } else if (clazz.equals(boolean.class)) {
            return Boolean.class;
        } else if (clazz.equals(double.class)) {
            return Double.class;
        } else if (clazz.equals(byte.class)) {
            return Byte.class;
        } else if (clazz.equals(short.class)) {
            return Short.class;
        } else if (clazz.equals(long.class)) {
            return Long.class;
        } else if (clazz.equals(float.class)) {
            return Float.class;
        } else {
            return clazz;
        }
    }

    /**
     * Determines if a method has a valid name and signature for a Java Bean
     * setter.
     *
     * @param method    The method to test
     *
     * @return  <code>true</code> if the method does have a valid name and
     *          signature, else <code>false</code>
     */
    public static boolean isValidSetter(Method method) {
        if (method.getName().startsWith("set")
                && method.getName().length() > 3
                && method.getParameterTypes().length == 1
                && method.getReturnType().getName().equals("void")) {
            return true;
        }
        return false;
    }

    /**
     * Extract the Java Bean property name from the setter name.
     *
     * Note: This method assumes that the method name has already been checked
     *       for correctness.
     * @param setter The setter method
     * @return the bean property name
     */
    public static String getPropertyName(Method setter) {
        return Introspector.decapitalize(setter.getName().substring(3));
    }

    /**
     * Obtain the declared fields for a class taking account of any security
     * manager that may be configured.
     * @param clazz The class to introspect
     * @return the class fields as an array
     */
    public static Field[] getDeclaredFields(final Class<?> clazz) {
        Field[] fields = null;
        if (Globals.IS_SECURITY_ENABLED) {
            fields = AccessController.doPrivileged((PrivilegedAction<Field[]>) clazz::getDeclaredFields);
        } else {
            fields = clazz.getDeclaredFields();
        }
        return fields;
    }

}
