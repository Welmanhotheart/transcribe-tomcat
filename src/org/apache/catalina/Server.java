package org.apache.catalina;

import org.apache.catalina.deploy.NamingResourcesImpl;
import org.apache.catalina.startup.Catalina;

import java.io.File;

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

    public Catalina getCatalina();

    void setCatalina(Catalina catalina);

    void setCatalinaHome(File catalinaHomeFile);

    void setCatalinaBase(File catalinaBaseFile);

    /**
     * Set the global naming resources.
     *
     * @param globalNamingResources The new global naming resources
     */
    public void setGlobalNamingResources
    (NamingResourcesImpl globalNamingResources);


}
