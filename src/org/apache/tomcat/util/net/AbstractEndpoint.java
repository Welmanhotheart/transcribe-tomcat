package org.apache.tomcat.util.net;

public class AbstractEndpoint<S,U> {

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
     * Socket properties
     */
    protected final SocketProperties socketProperties = new SocketProperties();

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

    public void setConnectionTimeout(int soTimeout) { socketProperties.setSoTimeout(soTimeout); }


    public static interface Handler<S>{

    }

}
