package org.apache.coyote;

import org.apache.juli.logging.Log;
import org.apache.tomcat.util.modeler.Registry;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.res.StringManager;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.net.InetAddress;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractProtocol<S> implements ProtocolHandler,
        MBeanRegistration {
    /**
     * The string manager for this package.
     */
    private static final StringManager sm = StringManager.getManager(AbstractProtocol.class);


    /**
     * Counter used to generate unique JMX names for connectors using automatic
     * port binding.
     */
    private static final AtomicInteger nameCounter = new AtomicInteger(0);


    /**
     * Endpoint that provides low-level network I/O - must be matched to the
     * ProtocolHandler implementation (ProtocolHandler using NIO, requires NIO
     * Endpoint etc.).
     */
    private final AbstractEndpoint<S,?> endpoint;

    // ----------------------------------------------------- JMX related methods

    protected String domain;
    protected ObjectName oname;
    protected MBeanServer mserver;
    /**
     * Name of MBean for the Global Request Processor.
     */
    protected ObjectName rgOname = null;
    public ObjectName getGlobalRequestProcessorMBeanName() {
        return rgOname;
    }

    public ObjectName getObjectName() {
        return oname;
    }

    public String getDomain() {
        return domain;
    }


    /**
     * Controller for the timeout scheduling.
     */
    private ScheduledFuture<?> timeoutFuture = null;
    private ScheduledFuture<?> monitorFuture;

    public AbstractProtocol(AbstractEndpoint<S,?> endpoint) {
        this.endpoint = endpoint;
        setConnectionLinger(Constants.DEFAULT_CONNECTION_LINGER);
        setTcpNoDelay(Constants.DEFAULT_TCP_NO_DELAY);
    }

    public void setConnectionLinger(int connectionLinger) {
        endpoint.setConnectionLinger(connectionLinger);
    }

    public int getPort() { return endpoint.getPort(); }
    public void setPort(int port) {
        endpoint.setPort(port);
    }

    public void setConnectionTimeout(int timeout) {
        endpoint.setConnectionTimeout(timeout);
    }

    /**
     * Concrete implementations need to provide access to their logger to be
     * used by the abstract classes.
     * @return the logger
     */
    protected abstract Log getLog();
    /**
     * The name will be prefix-address-port if address is non-null and
     * prefix-port if the address is null.
     *
     * @return A name for this protocol instance that is appropriately quoted
     *         for use in an ObjectName.
     */
    public String getName() {
        return ObjectName.quote(getNameInternal());
    }
    public InetAddress getAddress() { return endpoint.getAddress(); }

    private String getNameInternal() {
        StringBuilder name = new StringBuilder(getNamePrefix());
        name.append('-');
        String id = getId();
        if (id != null) {
            name.append(id);
        } else {
            if (getAddress() != null) {
                name.append(getAddress().getHostAddress());
                name.append('-');
            }
            int port = getPortWithOffset();
            if (port == 0) {
                // Auto binding is in use. Check if port is known
                name.append("auto-");
                name.append(getNameIndex());
                port = getLocalPort();
                if (port != -1) {
                    name.append('-');
                    name.append(port);
                }
            } else {
                name.append(port);
            }
        }
        return name.toString();
    }

    /**
     * Obtain the prefix to be used when construction a name for this protocol
     * handler. The name will be prefix-address-port.
     * @return the prefix
     */
    protected abstract String getNamePrefix();

    public int getPortWithOffset() { return endpoint.getPortWithOffset(); }

    public int getLocalPort() { return endpoint.getLocalPort(); }

    /**
     * Unique ID for this connector. Only used if the connector is configured
     * to use a random port as the port will change if stop(), start() is
     * called.
     */
    private int nameIndex = 0;



    // ---------------------------------------------------------- Public methods

    public synchronized int getNameIndex() {
        if (nameIndex == 0) {
            nameIndex = nameCounter.incrementAndGet();
        }

        return nameIndex;
    }

    private void logPortOffset() {
        if (getPort() != getPortWithOffset()) {
            getLog().info(sm.getString("abstractProtocolHandler.portOffset", getName(),
                    String.valueOf(getPort()), String.valueOf(getPortOffset())));
        }
    }

    private ObjectName createObjectName() throws MalformedObjectNameException {
        // Use the same domain as the connector
        domain = getAdapter().getDomain();

        if (domain == null) {
            return null;
        }

        StringBuilder name = new StringBuilder(getDomain());
        name.append(":type=ProtocolHandler,port=");
        int port = getPortWithOffset();
        if (port > 0) {
            name.append(port);
        } else {
            name.append("auto-");
            name.append(getNameIndex());
        }
        InetAddress address = getAddress();
        if (address != null) {
            name.append(",address=");
            name.append(ObjectName.quote(address.getHostAddress()));
        }
        return new ObjectName(name.toString());
    }


    @Override
    public void init() throws Exception {
        if (getLog().isInfoEnabled()) {
            getLog().info(sm.getString("abstractProtocolHandler.init", getName()));
            logPortOffset();
        }

        if (oname == null) {
            // Component not pre-registered so register it
            oname = createObjectName();
            if (oname != null) {
                Registry.getRegistry(null, null).registerComponent(this, oname, null);
            }
        }

        if (this.domain != null) {
            ObjectName rgOname = new ObjectName(domain + ":type=GlobalRequestProcessor,name=" + getName());
            this.rgOname = rgOname;
            Registry.getRegistry(null, null).registerComponent(
                    getHandler().getGlobal(), rgOname, null);
        }

        String endpointName = getName();
        endpoint.setName(endpointName.substring(1, endpointName.length()-1));
        endpoint.setDomain(domain);

        endpoint.init();
    }
    private AbstractEndpoint.Handler<S> handler;

    protected AbstractEndpoint.Handler<S> getHandler() {
        return handler;
    }


    protected AbstractEndpoint<S,?> getEndpoint() {
        return endpoint;
    }

    public int getPortOffset() { return endpoint.getPortOffset(); }

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

    public boolean getTcpNoDelay() { return endpoint.getTcpNoDelay(); }
    public void setTcpNoDelay(boolean tcpNoDelay) {
        endpoint.setTcpNoDelay(tcpNoDelay);
    }


    protected static class ConnectionHandler<S> implements AbstractEndpoint.Handler<S>{
        private final RequestGroupInfo global = new RequestGroupInfo();
        private final AbstractProtocol<S> proto;

        public ConnectionHandler(AbstractProtocol<S> proto) {
            this.proto = proto;
        }

        @Override
        public Object getGlobal() {
            return global;
        }
    }

}
