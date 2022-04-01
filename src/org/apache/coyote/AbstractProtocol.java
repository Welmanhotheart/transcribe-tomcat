package org.apache.coyote;

import org.apache.tomcat.util.net.AbstractEndpoint;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.util.concurrent.ScheduledFuture;

public class AbstractProtocol<S> implements ProtocolHandler,
        MBeanRegistration {

    /**
     * Endpoint that provides low-level network I/O - must be matched to the
     * ProtocolHandler implementation (ProtocolHandler using NIO, requires NIO
     * Endpoint etc.).
     */
    private final AbstractEndpoint<S,?> endpoint;

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


    private AbstractEndpoint.Handler<S> handler;


    protected void setHandler(AbstractEndpoint.Handler<S> handler) {
        this.handler = handler;
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

        private final AbstractProtocol<S> proto;

        public ConnectionHandler(AbstractProtocol<S> proto) {
            this.proto = proto;
        }
    }

}
