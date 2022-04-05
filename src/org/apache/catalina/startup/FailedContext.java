package org.apache.catalina.startup;

import org.apache.catalina.*;
import org.apache.catalina.deploy.NamingResourcesImpl;
import org.apache.catalina.util.ContextName;
import org.apache.catalina.util.LifecycleMBeanBase;
import org.apache.juli.logging.Log;
import org.apache.tomcat.util.res.StringManager;

import java.io.File;
import java.net.URL;

public class FailedContext  extends LifecycleMBeanBase implements Context {

    protected static final StringManager sm =
            StringManager.getManager(Constants.Package);




    @Override
    public void fireContainerEvent(String type, Object data) {

    }

    @Override
    public void setParentClassLoader(ClassLoader parent) {

    }

    @Override
    public void setName(String name) {

    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Realm getRealm() {
        return null;
    }

    @Override
    public void removeChild(Container child) {

    }

    @Override
    public void addChild(Container child) {

    }

    @Override
    public File getCatalinaBase() {
        return null;
    }

    @Override
    public Container[] findChildren() {
        return new Container[0];
    }

    @Override
    public Container findChild(String name) {
        return null;
    }

    @Override
    public void setRealm(Realm realm) {

    }

    @Override
    public String getMBeanKeyProperties() {
        Container c = this;
        StringBuilder keyProperties = new StringBuilder();
        int containerCount = 0;

        // Work up container hierarchy, add a component to the name for
        // each container
        while (!(c instanceof Engine)) {
            if (c instanceof Context) {
                keyProperties.append(",context=");
                ContextName cn = new ContextName(c.getName(), false);
                keyProperties.append(cn.getDisplayName());
            } else if (c instanceof Host) {
                keyProperties.append(",host=");
                keyProperties.append(c.getName());
            } else if (c == null) {
                // May happen in unit testing and/or some embedding scenarios
                keyProperties.append(",container");
                keyProperties.append(containerCount++);
                keyProperties.append("=null");
                break;
            } else {
                // Should never happen...
                keyProperties.append(",container");
                keyProperties.append(containerCount++);
                keyProperties.append('=');
                keyProperties.append(c.getName());
            }
            c = c.getParent();
        }
        return keyProperties.toString();
    }

    @Override
    public Pipeline getPipeline() {
        return null;
    }

    @Override
    public String getLogName() {
        return null;
    }

    @Override
    public Log getLogger() {
        return null;
    }

    private Container parent;
    @Override
    public Container getParent() { return parent; }
    @Override
    public void setParent(Container parent) { this.parent = parent; }

    @Override
    public void addContainerListener(ContainerListener listener) {

    }


    @Override
    public ClassLoader getParentClassLoader() {
        return null;
    }


    @Override
    public String getPath() {
        return null;
    }

    @Override
    public void setPath(String path) {

    }

    @Override
    public URL getConfigFile() {
        return configFile;
    }

    @Override
    public Loader getLoader() {
        return null;
    }

    @Override
    public void setLoader(Loader loader) {

    }

    private URL configFile;
    @Override
    public void setConfigFile(URL configFile) { this.configFile = configFile; }



    @Override
    public void setWebappVersion(String webappVersion) {

    }

    @Override
    public String getDocBase() {
        return null;
    }

    @Override
    public void setDocBase(String docBase) {

    }

    @Override
    public String[] findWatchedResources() {
        return new String[0];
    }

    @Override
    public void removeLifecycleListener(LifecycleListener listener) {

    }

    @Override
    public void reload() {

    }

    @Override
    public Manager getManager() {
        return null;
    }

    @Override
    public NamingResourcesImpl getNamingResources() {
        return null;
    }

    @Override
    public void setManager(Manager manager) {

    }

    @Override
    public void addServletMappingDecoded(String pattern, String name, boolean jspWildcard) {

    }

    @Override
    public boolean isServlet22() {
        return false;
    }

    @Override
    protected void stopInternal() throws LifecycleException {

    }

    @Override
    protected void startInternal() throws LifecycleException {

    }

    @Override
    protected String getObjectNameKeyProperties() {
        return null;
    }

    @Override
    protected String getDomainInternal() {
        return null;
    }
}
