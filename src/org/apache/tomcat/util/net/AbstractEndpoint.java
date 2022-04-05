package org.apache.tomcat.util.net;

import org.apache.juli.logging.Log;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.modeler.Registry;
import org.apache.tomcat.util.res.StringManager;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class AbstractEndpoint<S,U> {
    // -------------------------------------------------------------- Constants

    protected static final StringManager sm = StringManager.getManager(AbstractEndpoint.class);

    private int portOffset = 0;


    /**
     * Server socket port.
     */
    private int port = -1;
    public int getPort() { return port; }
    public void setPort(int port ) { this.port=port; }

    /**
     * Handling of accepted sockets.
     */
    private Handler<S> handler = null;
    public void setHandler(Handler<S> handler ) { this.handler = handler; }
    public Handler<S> getHandler() { return handler; }
    /**
     * SSL engine.
     */
    private boolean SSLEnabled = false;
    public boolean isSSLEnabled() { return SSLEnabled; }
    public void setSSLEnabled(boolean SSLEnabled) { this.SSLEnabled = SSLEnabled; }

    /**
     * Address for the server socket.
     */
    private InetAddress address;
    public InetAddress getAddress() { return address; }
    public void setAddress(InetAddress address) { this.address = address; }

    public final int getLocalPort() {
        try {
            InetSocketAddress localAddress = getLocalAddress();
            if (localAddress == null) {
                return -1;
            }
            return localAddress.getPort();
        } catch (IOException ioe) {
            return -1;
        }
    }
    /**
     * Obtain the network address the server socket is bound to. This primarily
     * exists to enable the correct address to be used when unlocking the server
     * socket since it removes the guess-work involved if no address is
     * specifically set.
     *
     * @return The network address that the server socket is listening on or
     *         null if the server socket is not currently bound.
     *
     * @throws IOException If there is a problem determining the currently bound
     *                     socket
     */
    protected abstract InetSocketAddress getLocalAddress() throws IOException;

    /**
     * Name of the thread pool, which will be used for naming child threads.
     */
    private String name = "TP";
    public void setName(String name) { this.name = name; }
    public String getName() { return name; }

    private ObjectName oname = null;

    /**
     * Name of domain to use for JMX registration.
     */
    private String domain;
    public void setDomain(String domain) { this.domain = domain; }
    public String getDomain() { return domain; }

    /**
     * Controls when the Endpoint binds the port. <code>true</code>, the default
     * binds the port on {@link #init()} and unbinds it on {@link #destroy()}.
     * If set to <code>false</code> the port is bound on {@link #start()} and
     * unbound on {@link #stop()}.
     */
    private boolean bindOnInit = true;
    public boolean getBindOnInit() { return bindOnInit; }
    public void setBindOnInit(boolean b) { this.bindOnInit = b; }

    private volatile BindState bindState = BindState.UNBOUND;
    public abstract void bind() throws Exception;
    public abstract void unbind() throws Exception;

    private void bindWithCleanup() throws Exception {
        try {
            bind();
        } catch (Throwable t) {
            // Ensure open sockets etc. are cleaned up if something goes
            // wrong during bind
            ExceptionUtils.handleThrowable(t);
            unbind();
            throw t;
        }
    }



    public final void init() throws Exception {
        if (bindOnInit) {
            bindWithCleanup();
            bindState = BindState.BOUND_ON_INIT;
        }
        if (this.domain != null) {
            // Register endpoint (as ThreadPool - historical name)
            oname = new ObjectName(domain + ":type=ThreadPool,name=\"" + getName() + "\"");
            Registry.getRegistry(null, null).registerComponent(this, oname, null);

            ObjectName socketPropertiesOname = new ObjectName(domain +
                    ":type=SocketProperties,name=\"" + getName() + "\"");
            socketProperties.setObjectName(socketPropertiesOname);
            Registry.getRegistry(null, null).registerComponent(socketProperties, socketPropertiesOname, null);

            for (SSLHostConfig sslHostConfig : findSslHostConfigs()) {
                registerJmx(sslHostConfig);
            }
        }
    }

    /**
     * Re-read the configuration files for all SSL hosts and replace the
     * existing SSL configuration with the updated settings. Note this
     * replacement will happen even if the settings remain unchanged.
     */
    public void reloadSslHostConfigs() {
        for (String hostName : sslHostConfigs.keySet()) {
            reloadSslHostConfig(hostName);
        }
    }

    /**
     * Re-read the configuration files for the SSL host and replace the existing
     * SSL configuration with the updated settings. Note this replacement will
     * happen even if the settings remain unchanged.
     *
     * @param hostName The SSL host for which the configuration should be
     *                 reloaded. This must match a current SSL host
     */
    public void reloadSslHostConfig(String hostName) {
        // Host names are case insensitive but stored/processed in lower case
        // internally because they are used as keys in a ConcurrentMap where
        // keys are compared in a case sensitive manner.
        // This method can be called via various paths so convert the supplied
        // host name to lower case here to ensure the conversion occurs whatever
        // the call path.
        SSLHostConfig sslHostConfig = sslHostConfigs.get(hostName.toLowerCase(Locale.ENGLISH));
        if (sslHostConfig == null) {
            throw new IllegalArgumentException(
                    sm.getString("endpoint.unknownSslHostName", hostName));
        }
        addSslHostConfig(sslHostConfig, true);
    }

    /**
     * Add the given SSL Host configuration.
     *
     * @param sslHostConfig The configuration to add
     *
     * @throws IllegalArgumentException If the host name is not valid or if a
     *                                  configuration has already been provided
     *                                  for that host
     */
    public void addSslHostConfig(SSLHostConfig sslHostConfig) throws IllegalArgumentException {
        addSslHostConfig(sslHostConfig, false);
    }
    /**
     * Add the given SSL Host configuration, optionally replacing the existing
     * configuration for the given host.
     *
     * @param sslHostConfig The configuration to add
     * @param replace       If {@code true} replacement of an existing
     *                      configuration is permitted, otherwise any such
     *                      attempted replacement will trigger an exception
     *
     * @throws IllegalArgumentException If the host name is not valid or if a
     *                                  configuration has already been provided
     *                                  for that host and replacement is not
     *                                  allowed
     */
    public void addSslHostConfig(SSLHostConfig sslHostConfig, boolean replace) throws IllegalArgumentException {
        String key = sslHostConfig.getHostName();
        if (key == null || key.length() == 0) {
            throw new IllegalArgumentException(sm.getString("endpoint.noSslHostName"));
        }
        if (bindState != BindState.UNBOUND && bindState != BindState.SOCKET_CLOSED_ON_STOP &&
                isSSLEnabled()) {
            try {
                createSSLContext(sslHostConfig);
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }
        if (replace) {
            SSLHostConfig previous = sslHostConfigs.put(key, sslHostConfig);
            if (previous != null) {
                unregisterJmx(sslHostConfig);
            }
            registerJmx(sslHostConfig);

            // Do not release any SSLContexts associated with a replaced
            // SSLHostConfig. They may still be in used by existing connections
            // and releasing them would break the connection at best. Let GC
            // handle the clean up.
        } else {
            SSLHostConfig duplicate = sslHostConfigs.putIfAbsent(key, sslHostConfig);
            if (duplicate != null) {
                releaseSSLContext(sslHostConfig);
                throw new IllegalArgumentException(sm.getString("endpoint.duplicateSslHostName", key));
            }
            registerJmx(sslHostConfig);
        }
    }

    public SSLHostConfig[] findSslHostConfigs() {
        return sslHostConfigs.values().toArray(new SSLHostConfig[0]);
    }

    public int getPortWithOffset() {
        // Zero is a special case and negative values are invalid
        int port = getPort();
        if (port > 0) {
            return port + getPortOffset();
        }
        return port;
    }

    /**
     * Socket properties
     */
    protected final SocketProperties socketProperties = new SocketProperties();
    protected ConcurrentMap<String,SSLHostConfig> sslHostConfigs = new ConcurrentHashMap<>();

    public int getPortOffset() { return portOffset; }


    public void setConnectionLinger(int connectionLinger) {
        socketProperties.setSoLingerTime(connectionLinger);
        socketProperties.setSoLingerOn(connectionLinger>=0);
    }


    /**
     * Socket TCP no delay.
     *
     * @return The current TCP no delay setting for sockets created by this
     *         endpoint
     */
    public boolean getTcpNoDelay() { return socketProperties.getTcpNoDelay();}
    public void setTcpNoDelay(boolean tcpNoDelay) { socketProperties.setTcpNoDelay(tcpNoDelay); }
    protected abstract Log getLog();

    private void registerJmx(SSLHostConfig sslHostConfig) {
        if (domain == null) {
            // Before init the domain is null
            return;
        }
        ObjectName sslOname = null;
        try {
            sslOname = new ObjectName(domain + ":type=SSLHostConfig,ThreadPool=\"" +
                    getName() + "\",name=" + ObjectName.quote(sslHostConfig.getHostName()));
            sslHostConfig.setObjectName(sslOname);
            try {
                Registry.getRegistry(null, null).registerComponent(sslHostConfig, sslOname, null);
            } catch (Exception e) {
                getLog().warn(sm.getString("endpoint.jmxRegistrationFailed", sslOname), e);
            }
        } catch (MalformedObjectNameException e) {
            getLog().warn(sm.getString("endpoint.invalidJmxNameSslHost",
                    sslHostConfig.getHostName()), e);
        }

        for (SSLHostConfigCertificate sslHostConfigCert : sslHostConfig.getCertificates()) {
            ObjectName sslCertOname = null;
            try {
                sslCertOname = new ObjectName(domain +
                        ":type=SSLHostConfigCertificate,ThreadPool=\"" + getName() +
                        "\",Host=" + ObjectName.quote(sslHostConfig.getHostName()) +
                        ",name=" + sslHostConfigCert.getType());
                sslHostConfigCert.setObjectName(sslCertOname);
                try {
                    Registry.getRegistry(null, null).registerComponent(
                            sslHostConfigCert, sslCertOname, null);
                } catch (Exception e) {
                    getLog().warn(sm.getString("endpoint.jmxRegistrationFailed", sslCertOname), e);
                }
            } catch (MalformedObjectNameException e) {
                getLog().warn(sm.getString("endpoint.invalidJmxNameSslHostCert",
                        sslHostConfig.getHostName(), sslHostConfigCert.getType()), e);
            }
        }
    }


    private void unregisterJmx(SSLHostConfig sslHostConfig) {
        Registry registry = Registry.getRegistry(null, null);
        registry.unregisterComponent(sslHostConfig.getObjectName());
        for (SSLHostConfigCertificate sslHostConfigCert : sslHostConfig.getCertificates()) {
            registry.unregisterComponent(sslHostConfigCert.getObjectName());
        }
    }


    public void setConnectionTimeout(int soTimeout) { socketProperties.setSoTimeout(soTimeout); }

    protected enum BindState {
        UNBOUND(false, false),
        BOUND_ON_INIT(true, true),
        BOUND_ON_START(true, true),
        SOCKET_CLOSED_ON_STOP(false, true);

        private final boolean bound;
        private final boolean wasBound;

        private BindState(boolean bound, boolean wasBound) {
            this.bound = bound;
            this.wasBound = wasBound;
        }

        public boolean isBound() {
            return bound;
        }

        public boolean wasBound() {
            return wasBound;
        }
    }

    public static interface Handler<S>{
        /**
         * Obtain the GlobalRequestProcessor associated with the handler.
         *
         * @return the GlobalRequestProcessor
         */
        public Object getGlobal();
    }

}
