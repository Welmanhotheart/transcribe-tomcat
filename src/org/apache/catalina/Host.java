package org.apache.catalina;

import javax.management.ObjectName;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

public interface Host extends Container  {


    /**
     * @return the value of the auto deploy flag.  If true, it indicates that
     * this host's child webapps should be discovered and automatically
     * deployed dynamically.
     */
    public boolean getAutoDeploy();
    /**
     * Obtain the JMX name for this container.
     *
     * @return the JMX name associated with this container.
     */
    public ObjectName getObjectName();


    /**
     * @return a default configuration path of this Host. The file will be
     * canonical if possible.
     */
    public File getConfigBaseFile();

    /**
     * @return the application root for this Host.  This can be an absolute
     * pathname, a relative pathname, or a URL.
     */
    public String getAppBase();


    /**
     * @return an absolute {@link File} for the appBase of this Host. The file
     * will be canonical if possible. There is no guarantee that that the
     * appBase exists.
     */
    public File getAppBaseFile();


    /**
     * @return the Java class name of the context configuration class
     * for new web applications.
     */
    public String getConfigClass();


    /**
     * Set the application root for this Host.  This can be an absolute
     * pathname, a relative pathname, or a URL.
     *
     * @param appBase The new application root
     */
    public void setAppBase(String appBase);


    /**
     * @return the legacy (Java EE) application root for this Host.  This can be
     * an absolute pathname, a relative pathname, or a URL.
     */
    public String getLegacyAppBase();


    /**
     * @return an absolute {@link File} for the legacy (Java EE) appBase of this
     * Host. The file will be canonical if possible. There is no guarantee that
     * that the appBase exists.
     */
    public File getLegacyAppBaseFile();


    /**
     * Set the legacy (Java EE) application root for this Host.  This can be an
     * absolute pathname, a relative pathname, or a URL.
     *
     * @param legacyAppBase The new legacy application root
     */
    public void setLegacyAppBase(String legacyAppBase);

    /**
     * Set the deploy on startup flag value for this host.
     *
     * @param deployOnStartup The new deploy on startup flag
     */
    public void setDeployOnStartup(boolean deployOnStartup);



    /**
     * Set the auto deploy flag value for this host.
     *
     * @param autoDeploy The new auto deploy flag
     */
    public void setAutoDeploy(boolean autoDeploy);

    /**
     * @return the value of the deploy on startup flag.  If true, it indicates
     * that this host's child webapps should be discovered and automatically
     * deployed.
     */
    public boolean getDeployOnStartup();

    /**
     * @return the compiled regular expression that defines the files and
     * directories in the host's appBase that will be ignored by the automatic
     * deployment process.
     */
    public Pattern getDeployIgnorePattern();

    /**
     * @return the executor that is used for starting and stopping contexts. This
     * is primarily for use by components deploying contexts that want to do
     * this in a multi-threaded manner.
     */
    public ExecutorService getStartStopExecutor();

    /**
     * @return <code>true</code> of the Host is configured to automatically undeploy old
     * versions of applications deployed using parallel deployment. This only
     * takes effect is {@link #getAutoDeploy()} also returns <code>true</code>.
     */
    public boolean getUndeployOldVersions();


    /**
     * Returns <code>true</code> if the Host will attempt to create directories for appBase and xmlBase
     * unless they already exist.
     * @return true if the Host will attempt to create directories
     */
    public boolean getCreateDirs();


}
