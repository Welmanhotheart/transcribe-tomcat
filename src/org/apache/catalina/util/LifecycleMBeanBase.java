package org.apache.catalina.util;

import org.apache.catalina.Globals;
import org.apache.catalina.JmxEnabled;
import org.apache.catalina.LifecycleException;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.modeler.Registry;
import org.apache.tomcat.util.res.StringManager;

import javax.management.MBeanServer;
import javax.management.ObjectName;

public abstract class LifecycleMBeanBase extends LifecycleBase  implements JmxEnabled {


    private static final Log log = LogFactory.getLog(LifecycleMBeanBase.class);

    private static final StringManager sm =
            StringManager.getManager("org.apache.catalina.util");

    /* Cache components of the MBean registration. */
    private String domain = null;
    private ObjectName oname = null;


    @Override
    protected void initInternal() throws LifecycleException {
        // If oname is not null then registration has already happened via
        // preRegister().
        if (oname == null) {
            oname = register(this, getObjectNameKeyProperties());
        }
    }

    /**
     * Allow sub-classes to specify the key properties component of the
     * {@link ObjectName} that will be used to register this component.
     *
     * @return  The string representation of the key properties component of the
     *          desired {@link ObjectName}
     */
    protected abstract String getObjectNameKeyProperties();

    /**
     * Obtain the domain under which this component will be / has been
     * registered.
     */
    @Override
    public final String getDomain() {
        if (domain == null) {
            domain = getDomainInternal();
        }

        if (domain == null) {
            domain = Globals.DEFAULT_MBEAN_DOMAIN;
        }

        return domain;
    }

    /**
     * Method implemented by sub-classes to identify the domain in which MBeans
     * should be registered.
     *
     * @return  The name of the domain to use to register MBeans.
     */
    protected abstract String getDomainInternal();


    /**
     * Utility method to enable sub-classes to easily register additional
     * components that don't implement {@link JmxEnabled} with an MBean server.
     * <br>
     * Note: This method should only be used once {@link #initInternal()} has
     * been called and before {@link #destroyInternal()} has been called.
     *
     * @param obj                       The object the register
     * @param objectNameKeyProperties   The key properties component of the
     *                                  object name to use to register the
     *                                  object
     *
     * @return  The name used to register the object
     */
    protected final ObjectName register(Object obj,
                                        String objectNameKeyProperties) {

        // Construct an object name with the right domain
        StringBuilder name = new StringBuilder(getDomain());
        name.append(':');
        name.append(objectNameKeyProperties);

        ObjectName on = null;

        try {
            on = new ObjectName(name.toString());
            Registry.getRegistry(null, null).registerComponent(obj, on, null);
        } catch (Exception e) {
            log.warn(sm.getString("lifecycleMBeanBase.registerFail", obj, name), e);
        }

        return on;
    }

    /**
     * Allows the object to be registered with an alternative
     * {@link MBeanServer} and/or {@link ObjectName}.
     */
    @Override
    public final ObjectName preRegister(MBeanServer server, ObjectName name)
            throws Exception {

        this.oname = name;
        this.domain = name.getDomain().intern();

        return oname;
    }

    /**
     * Not used - NOOP.
     */
    @Override
    public final void postRegister(Boolean registrationDone) {
        // NOOP
    }


    /**
     * Not used - NOOP.
     */
    @Override
    public final void preDeregister() throws Exception {
        // NOOP
    }

    /**
     * Not used - NOOP.
     */
    @Override
    public final void postDeregister() {
        // NOOP
    }
}
