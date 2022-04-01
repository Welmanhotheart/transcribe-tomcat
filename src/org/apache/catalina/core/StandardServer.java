package org.apache.catalina.core;

import org.apache.catalina.*;
import org.apache.catalina.deploy.NamingResourcesImpl;
import org.apache.catalina.startup.Catalina;
import org.apache.catalina.util.LifecycleMBeanBase;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.beans.PropertyChangeSupport;
import java.io.File;
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

    private File catalinaHome = null;

    private File catalinaBase = null;

    private Catalina catalina = null;

    /**
     * Global naming resources.
     */
    private NamingResourcesImpl globalNamingResources = null;


    public StandardServer() {
        System.out.println("dasf");
    }

    /**
     * The property change support for this component.
     */
    final PropertyChangeSupport support = new PropertyChangeSupport(this);


    /**
     * The current state of the source component.
     */
    private volatile LifecycleState state = LifecycleState.NEW;

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

    @Override
    public Catalina getCatalina() {
        return catalina;
    }


    /**
     * Set the outer Catalina startup/shutdown component if present.
     */
    @Override
    public void setCatalina(Catalina catalina) {
        this.catalina = catalina;
    }

    @Override
    public void setCatalinaHome(File catalinaHome) {
        this.catalinaHome = catalinaHome;
    }


    @Override
    public void setCatalinaBase(File catalinaBase) {
        this.catalinaBase = catalinaBase;
    }

    @Override
    protected String getObjectNameKeyProperties() {
        return null;
    }

    @Override
    protected String getDomainInternal() {
        return null;
    }

    private int portOffset = 0;

    @Override
    public int getPortOffset() {
        return portOffset;
    }

    /**
     * Set the global naming resources.
     *
     * @param globalNamingResources The new global naming resources
     */
    @Override
    public void setGlobalNamingResources
    (NamingResourcesImpl globalNamingResources) {

        NamingResourcesImpl oldGlobalNamingResources =
                this.globalNamingResources;
        this.globalNamingResources = globalNamingResources;
        this.globalNamingResources.setContainer(this);
        support.firePropertyChange("globalNamingResources",
                oldGlobalNamingResources,
                this.globalNamingResources);

    }

    @Override
    protected void destroyInternal() throws LifecycleException {

    }

    @Override
    protected void stopInternal() throws LifecycleException {

    }

    @Override
    protected void startInternal() throws LifecycleException {

    }
}
