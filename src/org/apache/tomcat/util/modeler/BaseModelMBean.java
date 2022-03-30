package org.apache.tomcat.util.modeler;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;

import javax.management.*;
import javax.management.modelmbean.ModelMBeanNotificationBroadcaster;

public class BaseModelMBean  implements DynamicMBean, MBeanRegistration,
        ModelMBeanNotificationBroadcaster {

    private static final Log log = LogFactory.getLog(BaseModelMBean.class);
    private static final StringManager sm = StringManager.getManager(BaseModelMBean.class);

    /** Metadata for the mbean instance.
     */
    protected ManagedBean managedBean = null;

    /**
     * The managed resource this MBean is associated with (if any).
     */
    protected Object resource = null;

    // --------------------------------------------------- DynamicMBean Methods
    // TODO: move to ManagedBean
    static final Object[] NO_ARGS_PARAM = new Object[0];

    protected String resourceType = null;




    public void setManagedBean(ManagedBean managedBean) {
        this.managedBean = managedBean;
    }

    public void setManagedResource(Object resource, String type)
            throws InstanceNotFoundException,
            MBeanException, RuntimeOperationsException
    {
        if (resource == null) {
            throw new RuntimeOperationsException
                    (new IllegalArgumentException(sm.getString("baseModelMBean.nullResource")),
                            sm.getString("baseModelMBean.nullResource"));
        }

//        if (!"objectreference".equalsIgnoreCase(type))
//            throw new InvalidTargetObjectTypeException(type);

        this.resource = resource;
        this.resourceType = resource.getClass().getName();

//        // Make the resource aware of the model mbean.
//        try {
//            Method m=resource.getClass().getMethod("setModelMBean",
//                    new Class[] {ModelMBean.class});
//            if( m!= null ) {
//                m.invoke(resource, new Object[] {this});
//            }
//        } catch( NoSuchMethodException t ) {
//            // ignore
//        } catch( Throwable t ) {
//            log.error( "Can't set model mbean ", t );
//        }
    }

    @Override
    public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
        return null;
    }

    @Override
    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {

    }

    @Override
    public AttributeList getAttributes(String[] attributes) {
        return null;
    }

    @Override
    public AttributeList setAttributes(AttributeList attributes) {
        return null;
    }

    @Override
    public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
        return null;
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        return null;
    }

    @Override
    public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
        return null;
    }

    @Override
    public void postRegister(Boolean registrationDone) {

    }

    @Override
    public void preDeregister() throws Exception {

    }

    @Override
    public void postDeregister() {

    }

    @Override
    public void sendNotification(Notification ntfyObj) throws MBeanException, RuntimeOperationsException {

    }

    @Override
    public void sendNotification(String ntfyText) throws MBeanException, RuntimeOperationsException {

    }

    @Override
    public void sendAttributeChangeNotification(AttributeChangeNotification notification) throws MBeanException, RuntimeOperationsException {

    }

    @Override
    public void sendAttributeChangeNotification(Attribute oldValue, Attribute newValue) throws MBeanException, RuntimeOperationsException {

    }

    @Override
    public void addAttributeChangeNotificationListener(NotificationListener listener, String attributeName, Object handback) throws MBeanException, RuntimeOperationsException, IllegalArgumentException {

    }

    @Override
    public void removeAttributeChangeNotificationListener(NotificationListener listener, String attributeName) throws MBeanException, RuntimeOperationsException, ListenerNotFoundException {

    }

    @Override
    public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws IllegalArgumentException {

    }

    @Override
    public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException {

    }

    @Override
    public MBeanNotificationInfo[] getNotificationInfo() {
        return new MBeanNotificationInfo[0];
    }
}
