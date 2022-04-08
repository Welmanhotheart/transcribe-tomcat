package org.apache.catalina.realm;

import org.apache.catalina.Container;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Realm;
import org.apache.catalina.util.LifecycleMBeanBase;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import java.beans.PropertyChangeSupport;

public class RealmBase extends LifecycleMBeanBase implements Realm {
    private static final Log log = LogFactory.getLog(RealmBase.class);


    /**
     * The Container with which this Realm is associated.
     */
    protected Container container = null;

    /**
     * The property change support for this component.
     */
    protected final PropertyChangeSupport support = new PropertyChangeSupport(this);


    /**
     * Return the Container with which this Realm has been associated.
     */
    @Override
    public Container getContainer() {
        return container;
    }

    /**
     * Set the Container with which this Realm has been associated.
     *
     * @param container The associated Container
     */
    @Override
    public void setContainer(Container container) {

        Container oldContainer = this.container;
        this.container = container;
        support.firePropertyChange("container", oldContainer, this.container);

    }

    @Override
    protected void stopInternal() throws LifecycleException {

    }

    @Override
    protected void startInternal() throws LifecycleException {

    }


    protected String realmPath = "/realm0";

    public String getRealmPath() {
        return realmPath;
    }

    public void setRealmPath(String theRealmPath) {
        realmPath = theRealmPath;
    }



    protected String getRealmSuffix() {
        return ",realmPath=" + getRealmPath();
    }

    // -------------------- JMX and Registration  --------------------

    @Override
    public String getObjectNameKeyProperties() {

        StringBuilder keyProperties = new StringBuilder("type=Realm");
        keyProperties.append(getRealmSuffix());
        keyProperties.append(container.getMBeanKeyProperties());

        return keyProperties.toString();
    }
    @Override
    protected String getDomainInternal() {
        return null;
    }

    @Override
    public void backgroundProcess() {

    }
}
