package org.apache.catalina.core;

import org.apache.catalina.*;
import org.apache.catalina.realm.NullRealm;
import org.apache.juli.logging.Log;

import java.io.File;
import java.util.Locale;

public class StandardEngine extends ContainerBase implements Engine {

    /**
     * Host name to use when no server host, or an unknown host,
     * is specified in the request.
     */
    private String defaultHost = null;

    /**
     * The <code>Service</code> that owns this Engine, if any.
     */
    private Service service = null;



    /**
     * Set the default host.
     *
     * @param host The new default host
     */
    @Override
    public void setDefaultHost(String host) {

        String oldDefaultHost = this.defaultHost;
        if (host == null) {
            this.defaultHost = null;
        } else {
            this.defaultHost = host.toLowerCase(Locale.ENGLISH);
        }
        if (getState().isAvailable()) {
            service.getMapper().setDefaultHostName(host);
        }
        support.firePropertyChange("defaultHost", oldDefaultHost,
                this.defaultHost);

    }

    @Override
    protected void initInternal() throws LifecycleException {
        // Ensure that a Realm is present before any attempt is made to start
        // one. This will create the default NullRealm if necessary.
        getRealm();
        super.initInternal();
    }


    // -------------------- JMX registration  --------------------

    @Override
    protected String getObjectNameKeyProperties() {
        return "type=Engine";
    }


    /**
     * Obtain the configured Realm and provide a default Realm implementation
     * when no explicit configuration is set.
     *
     * @return configured realm, or a {@link NullRealm} by default
     */
    @Override
    public Realm getRealm() {
        Realm configured = super.getRealm();
        // If no set realm has been called - default to NullRealm
        // This can be overridden at engine, context and host level
        if (configured == null) {
            configured = new NullRealm();
            this.setRealm(configured);
        }
        return configured;
    }

    @Override
    public void setService(Service service) {

    }

    @Override
    public File getCatalinaBase() {
        if (service != null) {
            Server s = service.getServer();
            if (s != null) {
                File base = s.getCatalinaBase();
                if (base != null) {
                    return base;
                }
            }
        }
        // Fall-back
        return super.getCatalinaBase();
    }

    @Override
    public Container findChild(String name) {
        return null;
    }


    /**
     * Return the parent class loader for this component.
     */
    @Override
    public ClassLoader getParentClassLoader() {
        if (parentClassLoader != null) {
            return parentClassLoader;
        }
        if (service != null) {
            return service.getParentClassLoader();
        }
        return ClassLoader.getSystemClassLoader();
    }


}
