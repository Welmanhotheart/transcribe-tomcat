package org.apache.catalina.startup;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.ServletSecurityElement;
import org.apache.catalina.*;
import org.apache.catalina.deploy.NamingResourcesImpl;
import org.apache.catalina.org.apache.tomcat.util.descriptor.web.LoginConfig;
import org.apache.catalina.util.ContextName;
import org.apache.catalina.util.LifecycleMBeanBase;
import org.apache.jasper.servlet.jakarta.servlet.ServletContainerInitializer;
import org.apache.juli.logging.Log;
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.util.descriptor.web.ErrorPage;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.apache.tomcat.util.res.StringManager;

import java.io.File;
import java.net.URL;
import java.util.Set;

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
    public int getStartStopThreads() {
        return 0;
    }

    @Override
    public void setStartStopThreads(int startStopThreads) {

    }

    @Override
    public void addChild(Container child) {

    }

    @Override
    public Set<String> addServletSecurity(ServletRegistration.Dynamic registration, ServletSecurityElement servletSecurityElement) {
        return null;
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
    public void backgroundProcess() {

    }

    @Override
    public void addContainerListener(ContainerListener listener) {

    }

    @Override
    public int getBackgroundProcessorDelay() {
        return -1;
    }


    @Override
    public ClassLoader getParentClassLoader() {
        return null;
    }


    private String path = null;
    @Override
    public String getPath() { return path; }
    @Override
    public void setPath(String path) { this.path = path; }

    @Override
    public boolean getOverride() {
        return false;
    }

    @Override
    public void setOverride(boolean override) {

    }

    @Override
    public void setConfigured(boolean configured) {

    }

    @Override
    public void addConstraint(SecurityConstraint constraint) {

    }

    @Override
    public SecurityConstraint[] findConstraints() {
        return new SecurityConstraint[0];
    }

    @Override
    public void removeErrorPage(ErrorPage errorPage) {

    }

    @Override
    public FilterDef findFilterDef(String filterName) {
        return null;
    }

    @Override
    public String[] findApplicationListeners() {
        return new String[0];
    }

    @Override
    public void removeFilterDef(FilterDef filterDef) {

    }

    @Override
    public String[] findParameters() {
        return new String[0];
    }

    @Override
    public FilterDef[] findFilterDefs() {
        return new FilterDef[0];
    }

    @Override
    public String[] findMimeMappings() {
        return new String[0];
    }

    @Override
    public void removeMimeMapping(String extension) {

    }

    @Override
    public void removeParameter(String name) {

    }

    @Override
    public String[] findSecurityRoles() {
        return new String[0];
    }

    @Override
    public void removeSecurityRole(String role) {

    }

    @Override
    public String[] findServletMappings() {
        return new String[0];
    }

    @Override
    public void removeServletMapping(String pattern) {

    }

    @Override
    public String[] findWelcomeFiles() {
        return new String[0];
    }

    @Override
    public void removeWelcomeFile(String name) {

    }

    @Override
    public String[] findWrapperLifecycles() {
        return new String[0];
    }

    @Override
    public void removeWrapperLifecycle(String listener) {

    }

    @Override
    public String[] findWrapperListeners() {
        return new String[0];
    }

    @Override
    public void removeWrapperListener(String listener) {

    }

    @Override
    public void addServletContainerInitializer(ServletContainerInitializer sci, Set<Class<?>> classes) {

    }

    @Override
    public boolean getXmlValidation() {
        return false;
    }

    @Override
    public boolean getXmlNamespaceAware() {
        return false;
    }

    @Override
    public boolean getIgnoreAnnotations() {
        return false;
    }

    @Override
    public void removeFilterMap(FilterMap filterMap) {

    }

    @Override
    public ErrorPage[] findErrorPages() {
        return new ErrorPage[0];
    }

    @Override
    public void removeConstraint(SecurityConstraint constraint) {

    }

    @Override
    public void addSecurityRole(String role) {

    }

    @Override
    public boolean findSecurityRole(String role) {
        return false;
    }

    @Override
    public LoginConfig getLoginConfig() {
        return null;
    }

    @Override
    public void setLoginConfig(LoginConfig config) {

    }

    @Override
    public Authenticator getAuthenticator() {
        return null;
    }

    @Override
    public boolean getXmlBlockExternal() {
        return false;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public boolean getParallelAnnotationScanning() {
        return false;
    }

    @Override
    public void setParallelAnnotationScanning(boolean parallelAnnotationScanning) {

    }

    @Override
    public boolean getLogEffectiveWebXml() {
        return false;
    }

    @Override
    public FilterMap[] findFilterMaps() {
        return new FilterMap[0];
    }


    @Override
    public WebResourceRoot getResources() {
        return null;
    }

    @Override
    public void setResources(WebResourceRoot resources) {

    }

    @Override
    public InstanceManager getInstanceManager() {
        return null;
    }

    @Override
    public void setInstanceManager(InstanceManager instanceManager) {

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


    private String webappVersion = null;
    @Override
    public String getWebappVersion() { return webappVersion; }
    @Override
    public void setWebappVersion(String webappVersion) {
        this.webappVersion = webappVersion;
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

    @Override
    public ClassLoader bind(boolean usePrivilegedAction, ClassLoader originalClassLoader) {
        return null;
    }

    @Override
    public void unbind(boolean usePrivilegedAction, ClassLoader originalClassLoader) {

    }
}
