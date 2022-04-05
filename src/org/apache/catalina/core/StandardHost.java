package org.apache.catalina.core;

import org.apache.catalina.Container;
import org.apache.catalina.Globals;
import org.apache.catalina.Host;
import org.apache.catalina.Pipeline;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import javax.management.ObjectName;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

public class StandardHost extends ContainerBase implements Host {
    private static final Log log = LogFactory.getLog(StandardHost.class);


    /**
     * deploy Context XML config files property.
     */
    private boolean deployXML = !Globals.IS_SECURITY_ENABLED;


    /**
     * Should XML files be copied to
     * $CATALINA_BASE/conf/&lt;engine&gt;/&lt;host&gt; by default when
     * a web application is deployed?
     */
    private boolean copyXML = false;


    /**
     * Unpack WARs property.
     */
    private boolean unpackWARs = true;


    /**
     * The Java class name of the default Context implementation class for
     * deployed web applications.
     */
    private String contextClass =
            "org.apache.catalina.core.StandardContext";


    /**
     * Create a new StandardHost component with the default basic Valve.
     */
    public StandardHost() {

        super();
        pipeline.setBasic(new StandardHostValve());

    }

    /**
     * @return <code>true</code> if WARs should be unpacked on deployment.
     */
    public boolean isUnpackWARs() {
        return unpackWARs;
    }

    /**
     * @return the Java class name of the Context implementation class
     * for new web applications.
     */
    public String getContextClass() {
        return this.contextClass;
    }


    @Override
    public Container findChild(String name) {
        return null;
    }

    @Override
    public String getMBeanKeyProperties() {
        return null;
    }

    @Override
    public Pipeline getPipeline() {
        return null;
    }

    @Override
    public ClassLoader getParentClassLoader() {
        return null;
    }



    /**
     * @return <code>true</code> if XML context descriptors should be deployed.
     */
    public boolean isDeployXML() {
        return deployXML;
    }


    /**
     * Deploy XML Context config files flag mutator.
     *
     * @param deployXML <code>true</code> if context descriptors should be deployed
     */
    public void setDeployXML(boolean deployXML) {
        this.deployXML = deployXML;
    }


    /**
     * @return the copy XML config file flag for this component.
     */
    public boolean isCopyXML() {
        return this.copyXML;
    }


    /**
     * Set the copy XML config file flag for this component.
     *
     * @param copyXML The new copy XML flag
     */
    public void setCopyXML(boolean copyXML) {
        this.copyXML = copyXML;
    }


    @Override
    public boolean getAutoDeploy() {
        return false;
    }

    @Override
    public ObjectName getObjectName() {
        return null;
    }

    @Override
    public File getConfigBaseFile() {
        return null;
    }

    @Override
    public String getAppBase() {
        return null;
    }

    @Override
    public File getAppBaseFile() {
        return null;
    }

    @Override
    public String getConfigClass() {
        return null;
    }

    @Override
    public void setAppBase(String appBase) {

    }

    @Override
    public String getLegacyAppBase() {
        return null;
    }

    @Override
    public File getLegacyAppBaseFile() {
        return null;
    }

    @Override
    public void setLegacyAppBase(String legacyAppBase) {

    }

    @Override
    public void setDeployOnStartup(boolean deployOnStartup) {

    }

    @Override
    public void setAutoDeploy(boolean autoDeploy) {

    }

    @Override
    public boolean getDeployOnStartup() {
        return false;
    }

    @Override
    public Pattern getDeployIgnorePattern() {
        return null;
    }

    @Override
    public ExecutorService getStartStopExecutor() {
        return null;
    }

    @Override
    public boolean getUndeployOldVersions() {
        return false;
    }

    @Override
    public boolean getCreateDirs() {
        return false;
    }
}
