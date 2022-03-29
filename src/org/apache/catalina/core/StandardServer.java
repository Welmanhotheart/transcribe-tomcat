package org.apache.catalina.core;

import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Server;
import org.apache.catalina.util.LifecycleMBeanBase;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class StandardServer extends LifecycleMBeanBase implements Server {

    /**
     * The port number on which we wait for shutdown commands.
     */
    private int port = 8005;

    /**
     * The shutdown command string we are looking for.
     */
    private String shutdown = "SHUTDOWN";

    /**
     * Set the port number we listen to for shutdown commands.
     *
     * @param port The new port number
     */
    @Override
    public void setPort(int port) {
        this.port = port;
    }


    /**
     * Set the shutdown command we are waiting for.
     *
     * @param shutdown The new shutdown command
     */
    @Override
    public void setShutdown(String shutdown) {
        this.shutdown = shutdown;
    }



}
