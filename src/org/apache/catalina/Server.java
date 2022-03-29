package org.apache.catalina;

public interface Server extends Lifecycle{

    /**
     * Set the port number we listen to for shutdown commands.
     *
     * @param port The new port number
     *
     * @see #setPortOffset(int)
     */
    public void setPort(int port);

    /**
     * Set the shutdown command we are waiting for.
     *
     * @param shutdown The new shutdown command
     */
    public void setShutdown(String shutdown);


}
