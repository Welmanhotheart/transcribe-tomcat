package org.apache.tomcat.util;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;

public class IntrospectionUtils {
    private static final Log log = LogFactory.getLog(IntrospectionUtils.class);
    private static final StringManager sm = StringManager.getManager(IntrospectionUtils.class);
    public static String replaceProperties(String value,
                                           Hashtable<Object,Object> staticProp, PropertySource dynamicProp[],
                                           ClassLoader classLoader) {
        return replaceProperties(value, staticProp, dynamicProp, classLoader, 0);
    }

    private static String replaceProperties(String value,
                                            Hashtable<Object,Object> staticProp, PropertySource dynamicProp[],
                                            ClassLoader classLoader, int iterationCount) {
        if (value.indexOf("${") < 0) {
            return value;
        }
        if (iterationCount >=20) {
            log.warn("System property failed to update and remains [" + value + "]");
            return value;
        }
        StringBuilder sb = new StringBuilder();
        int prev = 0;
        // assert value!=nil
        int pos;
        while ((pos = value.indexOf('$', prev)) >= 0) {
            if (pos > 0) {
                sb.append(value.substring(prev, pos));
            }
            if (pos == (value.length() - 1)) {
                sb.append('$');
                prev = pos + 1;
            } else if (value.charAt(pos + 1) != '{') {
                sb.append('$');
                prev = pos + 1; // XXX
            } else {
                int endName = value.indexOf('}', pos);
                if (endName < 0) {
                    sb.append(value.substring(pos));
                    prev = value.length();
                    continue;
                }
                String n = value.substring(pos + 2, endName);
                String v = getProperty(n, staticProp, dynamicProp, classLoader);
                if (v == null) {
                    // {name:default}
                    int col = n.indexOf(":-");
                    if (col != -1) {
                        String dV = n.substring(col + 2);
                        n = n.substring(0, col);
                        v = getProperty(n, staticProp, dynamicProp, classLoader);
                        if (v == null) {
                            v = dV;
                        }
                    } else {
                        v = "${" + n + "}";
                    }
                }
                sb.append(v);
                prev = endName + 1;
            }
        }
        if (prev < value.length()) {
            sb.append(value.substring(prev));
        }
        String newval = sb.toString();
        if (newval.indexOf("${") < 0) {
            return newval;
        }
        if (newval.equals(value)) {
            return value;
        }
        if (log.isDebugEnabled()) {
            log.debug("IntrospectionUtils.replaceProperties iter on: " + newval);
        }
        return replaceProperties(newval, staticProp, dynamicProp, classLoader, iterationCount+1);
    }

    private static String getProperty(String name, Hashtable<Object, Object> staticProp,
                                      PropertySource[] dynamicProp, ClassLoader classLoader) {
        String v = null;
        if (staticProp != null) {
            v = (String) staticProp.get(name);
        }
        if (v == null && dynamicProp != null) {
            for (PropertySource propertySource : dynamicProp) {
                if (propertySource instanceof SecurePropertySource) {
                    v = ((SecurePropertySource) propertySource).getProperty(name, classLoader);
                } else {
                    v = propertySource.getProperty(name);
                }
                if (v != null) {
                    break;
                }
            }
        }
        return v;
    }


    public static Object getProperty(Object o, String name) {
        if (XReflectionIntrospectionUtils.isEnabled()) {
            return XReflectionIntrospectionUtils.getPropertyInternal(o, name);
        }
        String getter = "get" + capitalize(name);
        String isGetter = "is" + capitalize(name);

        try {
            Method methods[] = findMethods(o.getClass());
            Method getPropertyMethod = null;

            // First, the ideal case - a getFoo() method
            for (Method method : methods) {
                Class<?> paramT[] = method.getParameterTypes();
                if (getter.equals(method.getName()) && paramT.length == 0) {
                    return method.invoke(o, (Object[]) null);
                }
                if (isGetter.equals(method.getName()) && paramT.length == 0) {
                    return method.invoke(o, (Object[]) null);
                }

                if ("getProperty".equals(method.getName())) {
                    getPropertyMethod = method;
                }
            }

            // Ok, no setXXX found, try a getProperty("name")
            if (getPropertyMethod != null) {
                Object params[] = new Object[1];
                params[0] = name;
                return getPropertyMethod.invoke(o, params);
            }

        } catch (IllegalArgumentException | SecurityException | IllegalAccessException e) {
            log.warn(sm.getString("introspectionUtils.getPropertyError", name, o.getClass()), e);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof NullPointerException) {
                // Assume the underlying object uses a storage to represent an unset property
                return null;
            }
            ExceptionUtils.handleThrowable(e.getCause());
            log.warn(sm.getString("introspectionUtils.getPropertyError", name, o.getClass()), e);
        }
        return null;
    }

    /**
     * Find a method with the right name If found, call the method ( if param is
     * int or boolean we'll convert value to the right type before) - that means
     * you can have setDebug(1).
     * @param o The object to set a property on
     * @param name The property name
     * @param value The property value
     * @return <code>true</code> if operation was successful
     */
    public static boolean setProperty(Object o, String name, String value) {
        return setProperty(o, name, value, true, null);
    }


    @SuppressWarnings("null") // setPropertyMethodVoid is not null when used
    public static boolean setProperty(Object o, String name, String value,
                                      boolean invokeSetProperty, StringBuilder actualMethod) {
        if (log.isDebugEnabled()) {
            log.debug("IntrospectionUtils: setProperty(" +
                    o.getClass() + " " + name + "=" + value + ")");
        }

        if (actualMethod == null && XReflectionIntrospectionUtils.isEnabled()) {
            return XReflectionIntrospectionUtils.setPropertyInternal(o, name, value, invokeSetProperty);
        }

        String setter = "set" + capitalize(name);

        try {
            Method methods[] = findMethods(o.getClass());
            Method setPropertyMethodVoid = null;
            Method setPropertyMethodBool = null;

            // First, the ideal case - a setFoo( String ) method
            for (Method item : methods) {
                Class<?> paramT[] = item.getParameterTypes();
                if (setter.equals(item.getName()) && paramT.length == 1
                        && "java.lang.String".equals(paramT[0].getName())) {
                    item.invoke(o, new Object[]{value});
                    if (actualMethod != null) {
                        actualMethod.append(item.getName()).append("(\"").append(escape(value)).append("\")");
                    }
                    return true;
                }
            }

            // Try a setFoo ( int ) or ( boolean )
            for (Method method : methods) {
                boolean ok = true;
                if (setter.equals(method.getName())
                        && method.getParameterTypes().length == 1) {

                    // match - find the type and invoke it
                    Class<?> paramType = method.getParameterTypes()[0];
                    Object params[] = new Object[1];

                    // Try a setFoo ( int )
                    if ("java.lang.Integer".equals(paramType.getName())
                            || "int".equals(paramType.getName())) {
                        try {
                            params[0] = Integer.valueOf(value);
                        } catch (NumberFormatException ex) {
                            ok = false;
                        }
                        if (actualMethod != null) {
                            actualMethod.append(method.getName()).append("(Integer.valueOf(\"").append(value).append("\"))");
                        }
                        // Try a setFoo ( long )
                    } else if ("java.lang.Long".equals(paramType.getName())
                            || "long".equals(paramType.getName())) {
                        try {
                            params[0] = Long.valueOf(value);
                        } catch (NumberFormatException ex) {
                            ok = false;
                        }
                        if (actualMethod != null) {
                            actualMethod.append(method.getName()).append("(Long.valueOf(\"").append(value).append("\"))");
                        }
                        // Try a setFoo ( boolean )
                    } else if ("java.lang.Boolean".equals(paramType.getName())
                            || "boolean".equals(paramType.getName())) {
                        params[0] = Boolean.valueOf(value);
                        if (actualMethod != null) {
                            actualMethod.append(method.getName()).append("(Boolean.valueOf(\"").append(value).append("\"))");
                        }
                        // Try a setFoo ( InetAddress )
                    } else if ("java.net.InetAddress".equals(paramType
                            .getName())) {
                        try {
                            params[0] = InetAddress.getByName(value);
                        } catch (UnknownHostException exc) {
                            if (log.isDebugEnabled()) {
                                log.debug("IntrospectionUtils: Unable to resolve host name:" + value);
                            }
                            ok = false;
                        }
                        if (actualMethod != null) {
                            actualMethod.append(method.getName()).append("(InetAddress.getByName(\"").append(value).append("\"))");
                        }
                        // Unknown type
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("IntrospectionUtils: Unknown type " +
                                    paramType.getName());
                        }
                    }

                    if (ok) {
                        method.invoke(o, params);
                        return true;
                    }
                }

                // save "setProperty" for later
                if ("setProperty".equals(method.getName())) {
                    if (method.getReturnType() == Boolean.TYPE) {
                        setPropertyMethodBool = method;
                    } else {
                        setPropertyMethodVoid = method;
                    }

                }
            }

            // Ok, no setXXX found, try a setProperty("name", "value")
            if (invokeSetProperty && (setPropertyMethodBool != null ||
                    setPropertyMethodVoid != null)) {
                if (actualMethod != null) {
                    actualMethod.append("setProperty(\"").append(name).append("\", \"").append(escape(value)).append("\")");
                }
                Object params[] = new Object[2];
                params[0] = name;
                params[1] = value;
                if (setPropertyMethodBool != null) {
                    try {
                        return ((Boolean) setPropertyMethodBool.invoke(o,
                                params)).booleanValue();
                    }catch (IllegalArgumentException biae) {
                        //the boolean method had the wrong
                        //parameter types. lets try the other
                        if (setPropertyMethodVoid!=null) {
                            setPropertyMethodVoid.invoke(o, params);
                            return true;
                        }else {
                            throw biae;
                        }
                    }
                } else {
                    setPropertyMethodVoid.invoke(o, params);
                    return true;
                }
            }

        } catch (IllegalArgumentException | SecurityException | IllegalAccessException e) {
            log.warn(sm.getString("introspectionUtils.setPropertyError", name, value, o.getClass()), e);
        } catch (InvocationTargetException e) {
            ExceptionUtils.handleThrowable(e.getCause());
            log.warn(sm.getString("introspectionUtils.setPropertyError", name, value, o.getClass()), e);
        }
        return false;
    }

    /**
     * Reverse of Introspector.decapitalize.
     * @param name The name
     * @return the capitalized string
     */
    public static String capitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        char chars[] = name.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }

    private static final Hashtable<Class<?>,Method[]> objectMethods = new Hashtable<>();

    public static Method[] findMethods(Class<?> c) {
        Method methods[] = objectMethods.get(c);
        if (methods != null) {
            return methods;
        }

        methods = c.getMethods();
        objectMethods.put(c, methods);
        return methods;
    }



    public static String escape(String s) {

        if (s == null) {
            return "";
        }

        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"') {
                b.append('\\').append('"');
            } else if (c == '\\') {
                b.append('\\').append('\\');
            } else if (c == '\n') {
                b.append('\\').append('n');
            } else if (c == '\r') {
                b.append('\\').append('r');
            } else {
                b.append(c);
            }
        }
        return b.toString();
    }


    public static interface SecurePropertySource extends PropertySource {

        /**
         * Obtain a property value, checking that code associated with the
         * provided class loader has permission to access the property. If the
         * {@code classLoader} is {@code null} or if {@code classLoader} does
         * not implement {@link PermissionCheck} then the property value will be
         * looked up <b>without</b> a call to
         * {@link PermissionCheck#check(java.security.Permission)}
         *
         * @param key           The key of the requested property
         * @param classLoader   The class loader associated with the code that
         *                      trigger the property lookup
         * @return The property value or {@code null} if it could not be found
         *         or if {@link PermissionCheck#check(java.security.Permission)}
         *         fails
         */
        public String getProperty(String key, ClassLoader classLoader);
    }

    @SuppressWarnings("null") // params cannot be null when comparing lengths
    public static Method findMethod(Class<?> c, String name,
                                    Class<?> params[]) {
        Method methods[] = findMethods(c);
        for (Method method : methods) {
            if (method.getName().equals(name)) {
                Class<?> methodParams[] = method.getParameterTypes();
                if (params == null && methodParams.length == 0) {
                    return method;
                }
                if (params.length != methodParams.length) {
                    continue;
                }
                boolean found = true;
                for (int j = 0; j < params.length; j++) {
                    if (params[j] != methodParams[j]) {
                        found = false;
                        break;
                    }
                }
                if (found) {
                    return method;
                }
            }
        }
        return null;
    }

    public static Object callMethod1(Object target, String methodN,
                                     Object param1, String typeParam1, ClassLoader cl) throws Exception {
        if (target == null || methodN == null || param1 == null) {
            throw new IllegalArgumentException(sm.getString("introspectionUtils.nullParameter"));
        }
        if (log.isDebugEnabled()) {
            log.debug("IntrospectionUtils: callMethod1 " +
                    target.getClass().getName() + " " +
                    param1.getClass().getName() + " " + typeParam1);
        }

        Class<?> params[] = new Class[1];
        if (typeParam1 == null) {
            params[0] = param1.getClass();
        } else {
            params[0] = cl.loadClass(typeParam1);
        }
        Method m = findMethod(target.getClass(), methodN, params);
        if (m == null) {
            throw new NoSuchMethodException(target.getClass().getName() + " "
                    + methodN);
        }
        try {
            return m.invoke(target, new Object[] { param1 });
        } catch (InvocationTargetException ie) {
            ExceptionUtils.handleThrowable(ie.getCause());
            throw ie;
        }
    }


    /**
     * why it is here TODO
     */
    public static interface PropertySource {
        public String getProperty(String key);
    }
}
