package org.apache.catalina.core;

import org.apache.juli.logging.Log;
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.util.collections.ManagedConcurrentWeakHashMap;
import org.apache.tomcat.util.res.StringManager;

import javax.naming.Context;
import javax.naming.NamingException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/***
 * TODO
 * @author <a href="zhiwei.wei@bintools.cn">zhiwei.wei</a>
 * @version 1.0.0 2022-04-2022/4/28-下午8:39
 */
public class DefaultInstanceManager implements InstanceManager {

    /**
     * The string manager for this package.
     */
    protected static final StringManager sm = StringManager.getManager(DefaultInstanceManager.class);


    private final Context context;
    private final Map<String, Map<String, String>> injectionMap;
    protected final ClassLoader classLoader;
    protected final ClassLoader containerClassLoader;
    protected final boolean privileged;
    protected final boolean ignoreAnnotations;
    private final Set<String> restrictedClasses;
    private final ManagedConcurrentWeakHashMap<Class<?>, AnnotationCacheEntry[]> annotationCache =
            new ManagedConcurrentWeakHashMap<>();
    private final Map<String, String> postConstructMethods;
    private final Map<String, String> preDestroyMethods;


    public DefaultInstanceManager(Context context,
                                  Map<String, Map<String, String>> injectionMap,
                                  org.apache.catalina.Context catalinaContext,
                                  ClassLoader containerClassLoader) {
        classLoader = catalinaContext.getLoader().getClassLoader();
        privileged = catalinaContext.getPrivileged();
        this.containerClassLoader = containerClassLoader;
        ignoreAnnotations = catalinaContext.getIgnoreAnnotations();
        Log log = catalinaContext.getLogger();
        Set<String> classNames = new HashSet<>();
        loadProperties(classNames,
                "org/apache/catalina/core/RestrictedServlets.properties",
                "defaultInstanceManager.restrictedServletsResource", log);
        loadProperties(classNames,
                "org/apache/catalina/core/RestrictedListeners.properties",
                "defaultInstanceManager.restrictedListenersResource", log);
        loadProperties(classNames,
                "org/apache/catalina/core/RestrictedFilters.properties",
                "defaultInstanceManager.restrictedFiltersResource", log);
        restrictedClasses = Collections.unmodifiableSet(classNames);
        this.context = context;
        this.injectionMap = injectionMap;
        this.postConstructMethods = catalinaContext.findPostConstructMethods();
        this.preDestroyMethods = catalinaContext.findPreDestroyMethods();
    }


    private static void loadProperties(Set<String> classNames, String resourceName,
                                       String messageKey, Log log) {
        Properties properties = new Properties();
        ClassLoader cl = DefaultInstanceManager.class.getClassLoader();
        try (InputStream is = cl.getResourceAsStream(resourceName)) {
            if (is == null) {
                log.error(sm.getString(messageKey, resourceName));
            } else {
                properties.load(is);
            }
        } catch (IOException ioe) {
            log.error(sm.getString(messageKey, resourceName), ioe);
        }
        if (properties.isEmpty()) {
            return;
        }
        for (Map.Entry<Object, Object> e : properties.entrySet()) {
            if ("restricted".equals(e.getValue())) {
                classNames.add(e.getKey().toString());
            } else {
                log.warn(sm.getString(
                        "defaultInstanceManager.restrictedWrongValue",
                        resourceName, e.getKey(), e.getValue()));
            }
        }
    }

    private Map<String, String> assembleInjectionsFromClassHierarchy(Class<?> clazz) {
        Map<String, String> injections = new HashMap<>();
        Map<String, String> currentInjections = null;
        while (clazz != null) {
            currentInjections = this.injectionMap.get(clazz.getName());
            if (currentInjections != null) {
                injections.putAll(currentInjections);
            }
            clazz = clazz.getSuperclass();
        }
        return injections;
    }

    @Override
    public Object newInstance(Class<?> clazz) throws IllegalAccessException,
            InvocationTargetException, NamingException, InstantiationException,
            IllegalArgumentException, NoSuchMethodException, SecurityException {
        return newInstance(clazz.getConstructor().newInstance(), clazz);
    }

    @Override
    public Object newInstance(String className) throws IllegalAccessException,
            InvocationTargetException, NamingException, InstantiationException,
            ClassNotFoundException, IllegalArgumentException, NoSuchMethodException, SecurityException {
        Class<?> clazz = loadClassMaybePrivileged(className, classLoader);
        return newInstance(clazz.getConstructor().newInstance(), clazz);
    }

    @Override
    public Object newInstance(final String className, final ClassLoader classLoader)
            throws IllegalAccessException, NamingException, InvocationTargetException,
            InstantiationException, ClassNotFoundException, IllegalArgumentException,
            NoSuchMethodException, SecurityException {
        Class<?> clazz = classLoader.loadClass(className);
        return newInstance(clazz.getConstructor().newInstance(), clazz);
    }

    @Override
    public void newInstance(Object o)
            throws IllegalAccessException, InvocationTargetException, NamingException {
        newInstance(o, o.getClass());
    }

    private Object newInstance(Object instance, Class<?> clazz)
            throws IllegalAccessException, InvocationTargetException, NamingException {
        if (!ignoreAnnotations) {
            Map<String, String> injections = assembleInjectionsFromClassHierarchy(clazz);
            populateAnnotationsCache(clazz, injections);
            processAnnotations(instance, injections);
            postConstruct(instance, clazz);
        }
        return instance;
    }



    private static final class AnnotationCacheEntry {
        private final String accessibleObjectName;
        private final Class<?>[] paramTypes;
        private final String name;
        private final AnnotationCacheEntryType type;

        public AnnotationCacheEntry(String accessibleObjectName,
                                    Class<?>[] paramTypes, String name,
                                    AnnotationCacheEntryType type) {
            this.accessibleObjectName = accessibleObjectName;
            this.paramTypes = paramTypes;
            this.name = name;
            this.type = type;
        }

        public String getAccessibleObjectName() {
            return accessibleObjectName;
        }

        public Class<?>[] getParamTypes() {
            return paramTypes;
        }

        public String getName() {
            return name;
        }
        public AnnotationCacheEntryType getType() {
            return type;
        }
    }


    private enum AnnotationCacheEntryType {
        FIELD, SETTER, POST_CONSTRUCT, PRE_DESTROY
    }


}