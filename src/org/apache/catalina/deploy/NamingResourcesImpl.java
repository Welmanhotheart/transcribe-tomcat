package org.apache.catalina.deploy;

import org.apache.catalina.*;
import org.apache.catalina.mbeans.MBeanUtils;
import org.apache.catalina.util.Introspection;
import org.apache.catalina.util.LifecycleMBeanBase;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.descriptor.web.*;
import org.apache.tomcat.util.res.StringManager;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class NamingResourcesImpl extends LifecycleMBeanBase
        implements Serializable, NamingResources {

    /**
     * Add a local EJB resource reference for this web application.
     *
     * @param ejb New EJB resource reference
     */
    public void addLocalEjb(ContextLocalEjb ejb) {

        if (entries.contains(ejb.getName())) {
            return;
        } else {
            entries.add(ejb.getName());
        }

        synchronized (localEjbs) {
            ejb.setNamingResources(this);
            localEjbs.put(ejb.getName(), ejb);
        }
        support.firePropertyChange("localEjb", null, ejb);

    }

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(NamingResourcesImpl.class);

    private static final StringManager sm = StringManager.getManager(NamingResourcesImpl.class);


    /**
     * Associated container object.
     */
    private Object container = null;

    /**
     * Set of naming entries, keyed by name.
     */
    private final Set<String> entries = new HashSet<>();

    /**
     * Add an EJB resource reference for this web application.
     *
     * @param ejb New EJB resource reference
     */
    public void addEjb(ContextEjb ejb) {

        // Entries with lookup-name and ejb-link are an error (EE.5.5.2 / EE.5.5.3)
        String ejbLink = ejb.getLink();
        String lookupName = ejb.getLookupName();

        if (ejbLink != null && ejbLink.length() > 0 && lookupName != null && lookupName.length() > 0) {
            throw new IllegalArgumentException(
                    sm.getString("namingResources.ejbLookupLink", ejb.getName()));
        }

        if (entries.contains(ejb.getName())) {
            return;
        } else {
            entries.add(ejb.getName());
        }

        synchronized (ejbs) {
            ejb.setNamingResources(this);
            ejbs.put(ejb.getName(), ejb);
        }
        support.firePropertyChange("ejb", null, ejb);

    }

    /**
     * The local  EJB resource references for this web application, keyed by
     * name.
     */
    private final Map<String, ContextLocalEjb> localEjbs = new HashMap<>();

    /**
     * The resource environment references for this web application,
     * keyed by name.
     */
    private final HashMap<String, ContextResourceEnvRef> resourceEnvRefs =
            new HashMap<>();


    /**
     * The web service references for this web application, keyed by name.
     */
    private final HashMap<String, ContextService> services =
            new HashMap<>();

    /**
     * The message destination references for this web application,
     * keyed by name.
     */
    private final Map<String, MessageDestinationRef> mdrs = new HashMap<>();


    /**
     * The resource references for this web application, keyed by name.
     */
    private final HashMap<String, ContextResource> resources =
            new HashMap<>();


    /**
     * The EJB resource references for this web application, keyed by name.
     */
    private final Map<String, ContextEjb> ejbs = new HashMap<>();

    /**
     * The property change support for this component.
     */
    protected final PropertyChangeSupport support =
            new PropertyChangeSupport(this);

    private volatile boolean resourceRequireExplicitRegistration = false;

    /**
     * The environment entries for this web application, keyed by name.
     */
    private final Map<String, ContextEnvironment> envs = new HashMap<>();

    /**
     * The resource links for this web application, keyed by name.
     */
    private final HashMap<String, ContextResourceLink> resourceLinks =
            new HashMap<>();


    /**
     * @return the container with which the naming resources are associated.
     */
    @Override
    public Object getContainer() {
        return container;
    }

    private Class<?> getSetterType(Class<?> clazz, String name) {
        Method[] methods = Introspection.getDeclaredMethods(clazz);
        if (methods != null && methods.length > 0) {
            for (Method method : methods) {
                if (Introspection.isValidSetter(method) &&
                        Introspection.getPropertyName(method).equals(name)) {
                    return method.getParameterTypes()[0];
                }
            }
        }
        return null;
    }

    @Override
    protected void initInternal() throws LifecycleException {
        super.initInternal();

        // Set this before we register currently known naming resources to avoid
        // timing issues. Duplication registration is not an issue.
        resourceRequireExplicitRegistration = true;

        for (ContextResource cr : resources.values()) {
            try {
                MBeanUtils.createMBean(cr);
            } catch (Exception e) {
                log.warn(sm.getString(
                        "namingResources.mbeanCreateFail", cr.getName()), e);
            }
        }

        for (ContextEnvironment ce : envs.values()) {
            try {
                MBeanUtils.createMBean(ce);
            } catch (Exception e) {
                log.warn(sm.getString(
                        "namingResources.mbeanCreateFail", ce.getName()), e);
            }
        }

        for (ContextResourceLink crl : resourceLinks.values()) {
            try {
                MBeanUtils.createMBean(crl);
            } catch (Exception e) {
                log.warn(sm.getString(
                        "namingResources.mbeanCreateFail", crl.getName()), e);
            }
        }
    }

    /**
     * @return the defined local EJB resource references for this application.
     * If there are none, a zero-length array is returned.
     */
    public ContextLocalEjb[] findLocalEjbs() {

        synchronized (localEjbs) {
            ContextLocalEjb results[] = new ContextLocalEjb[localEjbs.size()];
            return localEjbs.values().toArray(results);
        }

    }


    /**
     * @return the defined EJB resource references for this application.
     * If there are none, a zero-length array is returned.
     */
    public ContextEjb[] findEjbs() {

        synchronized (ejbs) {
            ContextEjb results[] = new ContextEjb[ejbs.size()];
            return ejbs.values().toArray(results);
        }

    }

    /**
     * @return the set of defined environment entries for this web
     * application.  If none have been defined, a zero-length array
     * is returned.
     */
    public ContextEnvironment[] findEnvironments() {

        synchronized (envs) {
            ContextEnvironment results[] = new ContextEnvironment[envs.size()];
            return envs.values().toArray(results);
        }

    }


    /**
     * @return the defined message destination references for this application.
     * If there are none, a zero-length array is returned.
     */
    public MessageDestinationRef[] findMessageDestinationRefs() {

        synchronized (mdrs) {
            MessageDestinationRef results[] =
                    new MessageDestinationRef[mdrs.size()];
            return mdrs.values().toArray(results);
        }

    }



    /**
     * @return the set of resource environment reference names for this
     * web application.  If none have been specified, a zero-length
     * array is returned.
     */
    public ContextResourceEnvRef[] findResourceEnvRefs() {

        synchronized (resourceEnvRefs) {
            ContextResourceEnvRef results[] = new ContextResourceEnvRef[resourceEnvRefs.size()];
            return resourceEnvRefs.values().toArray(results);
        }

    }



    /**
     * @return the defined resource references for this application.  If
     * none have been defined, a zero-length array is returned.
     */
    public ContextResource[] findResources() {

        synchronized (resources) {
            ContextResource results[] = new ContextResource[resources.size()];
            return resources.values().toArray(results);
        }

    }

    /**
     * @return the defined web service references for this application.  If
     * none have been defined, a zero-length array is returned.
     */
    public ContextService[] findServices() {

        synchronized (services) {
            ContextService results[] = new ContextService[services.size()];
            return services.values().toArray(results);
        }

    }

    /**
     * Add a resource reference for this web application.
     *
     * @param resource New resource reference
     */
    @Override
    public void addResource(ContextResource resource) {

        if (entries.contains(resource.getName())) {
            return;
        } else {
            if (!checkResourceType(resource)) {
                throw new IllegalArgumentException(sm.getString(
                        "namingResources.resourceTypeFail", resource.getName(),
                        resource.getType()));
            }
            entries.add(resource.getName());
        }

        synchronized (resources) {
            resource.setNamingResources(this);
            resources.put(resource.getName(), resource);
        }
        support.firePropertyChange("resource", null, resource);

        // Register with JMX
        if (resourceRequireExplicitRegistration) {
            try {
                MBeanUtils.createMBean(resource);
            } catch (Exception e) {
                log.warn(sm.getString("namingResources.mbeanCreateFail",
                        resource.getName()), e);
            }
        }
    }

    /**
     * Checks that the configuration of the type for the specified resource is
     * consistent with any injection targets and if the type is not specified,
     * tries to configure the type based on the injection targets
     *
     * @param resource  The resource to check
     *
     * @return  <code>true</code> if the type for the resource is now valid (if
     *          previously <code>null</code> this means it is now set) or
     *          <code>false</code> if the current resource type is inconsistent
     *          with the injection targets and/or cannot be determined
     */
    private boolean checkResourceType(ResourceBase resource) {
        if (!(container instanceof Context)) {
            // Only Context's will have injection targets
            return true;
        }

        if (resource.getInjectionTargets() == null ||
                resource.getInjectionTargets().size() == 0) {
            // No injection targets so use the defined type for the resource
            return true;
        }

        Context context = (Context) container;

        String typeName = resource.getType();
        Class<?> typeClass = null;
        if (typeName != null) {
            typeClass = Introspection.loadClass(context, typeName);
            if (typeClass == null) {
                // Can't load the type - will trigger a failure later so don't
                // fail here
                return true;
            }
        }

        Class<?> compatibleClass =
                getCompatibleType(context, resource, typeClass);
        if (compatibleClass == null) {
            // Indicates that a compatible type could not be identified that
            // worked for all injection targets
            return false;
        }

        resource.setType(compatibleClass.getCanonicalName());
        return true;
    }

    private Class<?> getCompatibleType(Context context,
                                       ResourceBase resource, Class<?> typeClass) {

        Class<?> result = null;

        for (InjectionTarget injectionTarget : resource.getInjectionTargets()) {
            Class<?> clazz = Introspection.loadClass(
                    context, injectionTarget.getTargetClass());
            if (clazz == null) {
                // Can't load class - therefore ignore this target
                continue;
            }

            // Look for a match
            String targetName = injectionTarget.getTargetName();
            // Look for a setter match first
            Class<?> targetType = getSetterType(clazz, targetName);
            if (targetType == null) {
                // Try a field match if no setter match
                targetType = getFieldType(clazz,targetName);
            }
            if (targetType == null) {
                // No match - ignore this injection target
                continue;
            }
            targetType = Introspection.convertPrimitiveType(targetType);

            if (typeClass == null) {
                // Need to find a common type amongst the injection targets
                if (result == null) {
                    result = targetType;
                } else if (targetType.isAssignableFrom(result)) {
                    // NO-OP - This will work
                } else if (result.isAssignableFrom(targetType)) {
                    // Need to use more specific type
                    result = targetType;
                } else {
                    // Incompatible types
                    return null;
                }
            } else {
                // Each injection target needs to be consistent with the defined
                // type
                if (targetType.isAssignableFrom(typeClass)) {
                    result = typeClass;
                } else {
                    // Incompatible types
                    return null;
                }
            }
        }
        return result;
    }

    private Class<?> getFieldType(Class<?> clazz, String name) {
        Field[] fields = Introspection.getDeclaredFields(clazz);
        if (fields != null && fields.length > 0) {
            for (Field field : fields) {
                if (field.getName().equals(name)) {
                    return field.getType();
                }
            }
        }
        return null;
    }

    /**
     * @return the environment entry with the specified name, if any;
     * otherwise, return <code>null</code>.
     *
     * @param name Name of the desired environment entry
     */
    public ContextEnvironment findEnvironment(String name) {

        synchronized (envs) {
            return envs.get(name);
        }

    }

    /**
     * @return the resource link with the specified name, if any;
     * otherwise return <code>null</code>.
     *
     * @param name Name of the desired resource link
     */
    public ContextResourceLink findResourceLink(String name) {

        synchronized (resourceLinks) {
            return resourceLinks.get(name);
        }

    }

    // Container should be an instance of Server or Context. If it is anything
    // else, return null which will trigger a NPE.
    private Server getServer() {
        if (container instanceof Server) {
            return (Server) container;
        }
        if (container instanceof Context) {
            // Could do this in one go. Lots of casts so split out for clarity
            Engine engine =
                    (Engine) ((Context) container).getParent().getParent();
            return engine.getService().getServer();
        }
        return null;
    }

    /**
     * Add a message destination reference for this web application.
     *
     * @param mdr New message destination reference
     */
    public void addMessageDestinationRef(MessageDestinationRef mdr) {

        if (entries.contains(mdr.getName())) {
            return;
        } else {
            if (!checkResourceType(mdr)) {
                throw new IllegalArgumentException(sm.getString(
                        "namingResources.resourceTypeFail", mdr.getName(),
                        mdr.getType()));
            }
            entries.add(mdr.getName());
        }

        synchronized (mdrs) {
            mdr.setNamingResources(this);
            mdrs.put(mdr.getName(), mdr);
        }
        support.firePropertyChange("messageDestinationRef", null, mdr);

    }



    /**
     * Remove any resource link with the specified name.
     *
     * @param name Name of the resource link to remove
     */
    @Override
    public void removeResourceLink(String name) {

        entries.remove(name);

        ContextResourceLink resourceLink = null;
        synchronized (resourceLinks) {
            resourceLink = resourceLinks.remove(name);
        }
        if (resourceLink != null) {
            support.firePropertyChange("resourceLink", resourceLink, null);
            // De-register with JMX
            if (resourceRequireExplicitRegistration) {
                try {
                    MBeanUtils.destroyMBean(resourceLink);
                } catch (Exception e) {
                    log.warn(sm.getString("namingResources.mbeanDestroyFail",
                            resourceLink.getName()), e);
                }
            }
            resourceLink.setNamingResources(null);
        }
    }


    /**
     * Add an environment entry for this web application.
     *
     * @param environment New environment entry
     */
    @Override
    public void addEnvironment(ContextEnvironment environment) {

        if (entries.contains(environment.getName())) {
            ContextEnvironment ce = findEnvironment(environment.getName());
            ContextResourceLink rl = findResourceLink(environment.getName());
            if (ce != null) {
                if (ce.getOverride()) {
                    removeEnvironment(environment.getName());
                } else {
                    return;
                }
            } else if (rl != null) {
                // Link. Need to look at the global resources
                NamingResourcesImpl global = getServer().getGlobalNamingResources();
                if (global.findEnvironment(rl.getGlobal()) != null) {
                    if (global.findEnvironment(rl.getGlobal()).getOverride()) {
                        removeResourceLink(environment.getName());
                    } else {
                        return;
                    }
                }
            } else {
                // It exists but it isn't an env or a res link...
                return;
            }
        }

        List<InjectionTarget> injectionTargets = environment.getInjectionTargets();
        String value = environment.getValue();
        String lookupName = environment.getLookupName();

        // Entries with injection targets but no value are effectively ignored
        if (injectionTargets != null && injectionTargets.size() > 0 &&
                (value == null || value.length() == 0)) {
            return;
        }

        // Entries with lookup-name and value are an error (EE.5.4.1.3)
        if (value != null && value.length() > 0 && lookupName != null && lookupName.length() > 0) {
            throw new IllegalArgumentException(
                    sm.getString("namingResources.envEntryLookupValue", environment.getName()));
        }

        if (!checkResourceType(environment)) {
            throw new IllegalArgumentException(sm.getString(
                    "namingResources.resourceTypeFail", environment.getName(),
                    environment.getType()));
        }

        entries.add(environment.getName());

        synchronized (envs) {
            environment.setNamingResources(this);
            envs.put(environment.getName(), environment);
        }
        support.firePropertyChange("environment", null, environment);

        // Register with JMX
        if (resourceRequireExplicitRegistration) {
            try {
                MBeanUtils.createMBean(environment);
            } catch (Exception e) {
                log.warn(sm.getString("namingResources.mbeanCreateFail",
                        environment.getName()), e);
            }
        }
    }

    /**
     * Add a web service reference for this web application.
     *
     * @param service New web service reference
     */
    public void addService(ContextService service) {

        if (entries.contains(service.getName())) {
            return;
        } else {
            entries.add(service.getName());
        }

        synchronized (services) {
            service.setNamingResources(this);
            services.put(service.getName(), service);
        }
        support.firePropertyChange("service", null, service);

    }

    /**
     * Remove any environment entry with the specified name.
     *
     * @param name Name of the environment entry to remove
     */
    @Override
    public void removeEnvironment(String name) {

        entries.remove(name);

        ContextEnvironment environment = null;
        synchronized (envs) {
            environment = envs.remove(name);
        }
        if (environment != null) {
            support.firePropertyChange("environment", environment, null);
            // De-register with JMX
            if (resourceRequireExplicitRegistration) {
                try {
                    MBeanUtils.destroyMBean(environment);
                } catch (Exception e) {
                    log.warn(sm.getString("namingResources.mbeanDestroyFail",
                            environment.getName()), e);
                }
            }
            environment.setNamingResources(null);
        }
    }

    /**
     * Add a resource environment reference for this web application.
     *
     * @param resource The resource
     */
    public void addResourceEnvRef(ContextResourceEnvRef resource) {

        if (entries.contains(resource.getName())) {
            return;
        } else {
            if (!checkResourceType(resource)) {
                throw new IllegalArgumentException(sm.getString(
                        "namingResources.resourceTypeFail", resource.getName(),
                        resource.getType()));
            }
            entries.add(resource.getName());
        }

        synchronized (resourceEnvRefs) {
            resource.setNamingResources(this);
            resourceEnvRefs.put(resource.getName(), resource);
        }
        support.firePropertyChange("resourceEnvRef", null, resource);

    }

    @Override
    protected String getObjectNameKeyProperties() {
        Object c = getContainer();
        if (c instanceof Container) {
            return "type=NamingResources" +
                    ((Container) c).getMBeanKeyProperties();
        }
        // Server or just unknown
        return "type=NamingResources";
    }

    @Override
    protected String getDomainInternal() {
        // Use the same domain as our associated container if we have one
        Object c = getContainer();

        if (c instanceof JmxEnabled) {
            return ((JmxEnabled) c).getDomain();
        }

        return null;
    }
    /**
     * Set the container with which the naming resources are associated.
     * @param container the associated with the resources
     */
    public void setContainer(Object container) {
        this.container = container;
    }

    @Override
    protected void destroyInternal() throws LifecycleException {

    }

    @Override
    protected void stopInternal() throws LifecycleException {

    }


    @Override
    protected void startInternal() throws LifecycleException {
        fireLifecycleEvent(CONFIGURE_START_EVENT, null);
        setState(LifecycleState.STARTING);
    }

}
