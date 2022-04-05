package org.apache.catalina.deploy;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.mbeans.MBeanUtils;
import org.apache.catalina.util.Introspection;
import org.apache.catalina.util.LifecycleMBeanBase;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.apache.tomcat.util.descriptor.web.InjectionTarget;
import org.apache.tomcat.util.descriptor.web.NamingResources;
import org.apache.tomcat.util.descriptor.web.ResourceBase;
import org.apache.tomcat.util.res.StringManager;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class NamingResourcesImpl extends LifecycleMBeanBase
        implements Serializable, NamingResources {


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
     * The resource references for this web application, keyed by name.
     */
    private final HashMap<String, ContextResource> resources =
            new HashMap<>();

    /**
     * The property change support for this component.
     */
    protected final PropertyChangeSupport support =
            new PropertyChangeSupport(this);

    private volatile boolean resourceRequireExplicitRegistration = false;

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

    @Override
    protected String getObjectNameKeyProperties() {
        return null;
    }

    @Override
    protected String getDomainInternal() {
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

    }
}
