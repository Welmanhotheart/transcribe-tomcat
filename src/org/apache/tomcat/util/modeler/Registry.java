package org.apache.tomcat.util.modeler;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.modeler.modules.ModelerSource;
import org.apache.tomcat.util.res.StringManager;

import javax.management.DynamicMBean;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Registry {


    /**
     * The Log instance to which we will write our log messages.
     */
    private static final Log log = LogFactory.getLog(Registry.class);
    private static final StringManager sm = StringManager.getManager(Registry.class);

    /**
     * The set of ManagedBean instances for the beans this registry knows about,
     * keyed by name.
     */
    private Map<String, ManagedBean> descriptors = new HashMap<>();

    /**
     * List of managed beans, keyed by class name
     */
    private Map<String, ManagedBean> descriptorsByClass = new HashMap<>();

    // map to avoid duplicated searching or loading descriptors
    private Map<String, URL> searchedPaths = new HashMap<>();


    /**
     * The registry instance created by our factory method the first time it is
     * called.
     */
    private static Registry registry = null;

    private Object guard;


    /**
     * The <code>MBeanServer</code> instance that we will use to register
     * management beans.
     */
    private volatile MBeanServer server = null;
    private final Object serverLock = new Object();


    public static synchronized Registry getRegistry(Object key, Object guard) {
        if (registry == null) {
            registry = new Registry();
            registry.guard = guard;
        }
        if (registry.guard != null && registry.guard != guard) {
            return null;
        }
        return registry;
    }

    /**
     * Register a component
     *
     * @param bean The bean
     * @param oname The object name
     * @param type The registry type
     * @throws Exception Error registering component
     */
    public void registerComponent(Object bean, ObjectName oname, String type) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Managed= " + oname);
        }

        if (bean == null) {
            log.error(sm.getString("registry.nullBean", oname));
            return;
        }

        try {
            if (type == null) {
                type = bean.getClass().getName();
            }

            ManagedBean managed = findManagedBean(null, bean.getClass(), type);

            // The real mbean is created and registered
            DynamicMBean mbean = managed.createMBean(bean);

            if (getMBeanServer().isRegistered(oname)) {
                if (log.isDebugEnabled()) {
                    log.debug("Unregistering existing component " + oname);
                }
                getMBeanServer().unregisterMBean(oname);
            }

            getMBeanServer().registerMBean(mbean, oname);
        } catch (Exception ex) {
            log.error(sm.getString("registry.registerError", oname), ex);
            throw ex;
        }
    }

    /**
     * Factory method to create (if necessary) and return our
     * <code>MBeanServer</code> instance.
     *
     * @return the MBean server
     */
    public MBeanServer getMBeanServer() {
        if (server == null) {
            synchronized (serverLock) {
                if (server == null) {
                    long t1 = System.currentTimeMillis();
                    if (MBeanServerFactory.findMBeanServer(null).size() > 0) {
                        server = MBeanServerFactory.findMBeanServer(null).get(0);
                        if (log.isDebugEnabled()) {
                            log.debug("Using existing MBeanServer " + (System.currentTimeMillis() - t1));
                        }
                    } else {
                        server = ManagementFactory.getPlatformMBeanServer();
                        if (log.isDebugEnabled()) {
                            log.debug("Creating MBeanServer" + (System.currentTimeMillis() - t1));
                        }
                    }
                }
            }
        }
        return server;
    }

    public ManagedBean findManagedBean(String name) {
        // XXX Group ?? Use Group + Type
        ManagedBean mb = descriptors.get(name);
        if (mb == null) {
            mb = descriptorsByClass.get(name);
        }
        return mb;
    }

    /**
     * Experimental. Load descriptors.
     *
     * @param sourceType The source type
     * @param source The bean
     * @param param A type to load
     * @return List of descriptors
     * @throws Exception Error loading descriptors
     */
    public List<ObjectName> load(String sourceType, Object source, String param) throws Exception {
        if (log.isTraceEnabled()) {
            log.trace("load " + source);
        }
        String location = null;
        String type = null;
        Object inputsource = null;

        if (source instanceof URL) {
            URL url = (URL) source;
            location = url.toString();
            type = param;
            inputsource = url.openStream();
            if (sourceType == null && location.endsWith(".xml")) {
                sourceType = "MbeansDescriptorsDigesterSource";
            }
        } else if (source instanceof File) {
            location = ((File) source).getAbsolutePath();
            inputsource = new FileInputStream((File) source);
            type = param;
            if (sourceType == null && location.endsWith(".xml")) {
                sourceType = "MbeansDescriptorsDigesterSource";
            }
        } else if (source instanceof InputStream) {
            type = param;
            inputsource = source;
        } else if (source instanceof Class<?>) {
            location = ((Class<?>) source).getName();
            type = param;
            inputsource = source;
            if (sourceType == null) {
                sourceType = "MbeansDescriptorsIntrospectionSource";
            }
        }

        if (sourceType == null) {
            sourceType = "MbeansDescriptorsDigesterSource";
        }
        ModelerSource ds = getModelerSource(sourceType);
        List<ObjectName> mbeans = ds.loadDescriptors(this, type, inputsource);

        return mbeans;
    }


    private ModelerSource getModelerSource(String type) throws Exception {
        if (type == null) {
            type = "MbeansDescriptorsDigesterSource";
        }
        if (!type.contains(".")) {
            type = "org.apache.tomcat.util.modeler.modules." + type;
        }

        Class<?> c = Class.forName(type);
        ModelerSource ds = (ModelerSource) c.getConstructor().newInstance();
        return ds;
    }


    /**
     * Find or load metadata.
     *
     * @param bean The bean
     * @param beanClass The bean class
     * @param type The registry type
     * @return the managed bean
     * @throws Exception An error occurred
     */
    public ManagedBean findManagedBean(Object bean, Class<?> beanClass, String type)
            throws Exception {

        if (bean != null && beanClass == null) {
            beanClass = bean.getClass();
        }

        if (type == null) {
            type = beanClass.getName();
        }

        // first look for existing descriptor
        ManagedBean managed = findManagedBean(type);

        // Search for a descriptor in the same package
        if (managed == null) {
            // check package and parent packages
            if (log.isDebugEnabled()) {
                log.debug("Looking for descriptor ");
            }
            findDescriptor(beanClass, type);

            managed = findManagedBean(type);
        }

        // Still not found - use introspection
        if (managed == null) {
            if (log.isDebugEnabled()) {
                log.debug("Introspecting ");
            }

            // introspection
            load("MbeansDescriptorsIntrospectionSource", beanClass, type);

            managed = findManagedBean(type);
            if (managed == null) {
                log.warn(sm.getString("registry.noTypeMetadata", type));
                return null;
            }
            managed.setName(type);
            addManagedBean(managed);
        }
        return managed;
    }


    /**
     * Add a new bean metadata to the set of beans known to this registry. This
     * is used by internal components.
     *
     * @param bean The managed bean to be added
     * @since 1.0
     */
    public void addManagedBean(ManagedBean bean) {
        // XXX Use group + name
        descriptors.put(bean.getName(), bean);
        if (bean.getType() != null) {
            descriptorsByClass.put(bean.getType(), bean);
        }
    }

    /**
     * Lookup the component descriptor in the package and in the parent
     * packages.
     */
    private void findDescriptor(Class<?> beanClass, String type) {
        if (type == null) {
            type = beanClass.getName();
        }
        ClassLoader classLoader = null;
        if (beanClass != null) {
            classLoader = beanClass.getClassLoader();
        }
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        if (classLoader == null) {
            classLoader = this.getClass().getClassLoader();
        }

        String className = type;
        String pkg = className;
        while (pkg.indexOf('.') > 0) {
            int lastComp = pkg.lastIndexOf('.');
            if (lastComp <= 0) {
                return;
            }
            pkg = pkg.substring(0, lastComp);
            if (searchedPaths.get(pkg) != null) {
                return;
            }
            loadDescriptors(pkg, classLoader);
        }
    }


    /**
     * Lookup the component descriptor in the package and in the parent
     * packages.
     *
     * @param packageName The package name
     * @param classLoader The class loader
     */
    public void loadDescriptors(String packageName, ClassLoader classLoader) {
        String res = packageName.replace('.', '/');

        if (log.isTraceEnabled()) {
            log.trace("Finding descriptor " + res);
        }

        if (searchedPaths.get(packageName) != null) {
            return;
        }

        String descriptors = res + "/mbeans-descriptors.xml";
        URL dURL = classLoader.getResource(descriptors);

        if (dURL == null) {
            return;
        }

        log.debug("Found " + dURL);
        searchedPaths.put(packageName, dURL);
        try {
            load("MbeansDescriptorsDigesterSource", dURL, null);
        } catch (Exception ex) {
            log.error(sm.getString("registry.loadError", dURL));
        }
    }

}
