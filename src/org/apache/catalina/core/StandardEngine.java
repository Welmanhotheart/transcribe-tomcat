package org.apache.catalina.core;

import org.apache.catalina.Engine;
import org.apache.catalina.Service;

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
}
