package org.apache.catalina.core;

import jakarta.servlet.ServletRegistration;
import jakarta.servlet.ServletSecurityElement;
import org.apache.catalina.*;
import org.apache.catalina.valves.ErrorReportValve;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.ExceptionUtils;

import javax.management.ObjectName;
import java.io.File;
import java.io.IOException;
import java.util.Set;
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
     * host's default config path
     */
    private volatile File hostConfigBase = null;

    /**
     * The XML root for this Host.
     */
    private String xmlBase = null;

    /**
     * Unpack WARs property.
     */
    private boolean unpackWARs = true;

    /**
     * The application root for this Host.
     */
    private String appBase = "webapps";
    private volatile File appBaseFile = null;

    /**
     * The legacy (Java EE) application root for this Host.
     */
    private String legacyAppBase = "webapps-javaee";
    private volatile File legacyAppBaseFile = null;
    /**
     * The deploy on startup flag for this Host.
     */
    private boolean deployOnStartup = true;


    /**
     * The Java class name of the default context configuration class
     * for deployed web applications.
     */
    private String configClass =
            "org.apache.catalina.startup.ContextConfig";
    /**
     * The Java class name of the default error reporter implementation class
     * for deployed web applications.
     */
    private String errorReportValveClass =
            "org.apache.catalina.valves.ErrorReportValve";


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



    /**
     * @return the Java class name of the error report valve class
     * for new web applications.
     */
    public String getErrorReportValveClass() {
        return this.errorReportValveClass;
    }

    /**
     * Start this component and implement the requirements
     * of {@link org.apache.catalina.util.LifecycleBase#startInternal()}.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
    @Override
    protected synchronized void startInternal() throws LifecycleException {
        System.out.println("standard host starting");
        // Set error report valve
        String errorValve = getErrorReportValveClass();
        if ((errorValve != null) && (!errorValve.equals(""))) {
            try {
                boolean found = false;
                Valve[] valves = getPipeline().getValves();
                for (Valve valve : valves) {
                    if (errorValve.equals(valve.getClass().getName())) {
                        found = true;
                        break;
                    }
                }
                if(!found) {
                    Valve valve = ErrorReportValve.class.getName().equals(errorValve) ?
                            new ErrorReportValve() :
                            (Valve) Class.forName(errorValve).getConstructor().newInstance();
                    getPipeline().addValve(valve);
                }
            } catch (Throwable t) {
                ExceptionUtils.handleThrowable(t);
                log.error(sm.getString(
                        "standardHost.invalidErrorReportValveClass",
                        errorValve), t);
            }
        }
        super.startInternal();
    }

    @Override
    protected String getObjectNameKeyProperties() {

        StringBuilder keyProperties = new StringBuilder("type=Host");
        keyProperties.append(getMBeanKeyProperties());

        return keyProperties.toString();
    }
    /**
     * @return the canonical, fully qualified, name of the virtual host
     * this Container represents.
     */
    @Override
    public String getName() {
        return name;
    }

    @Override
    public Set<String> addServletSecurity(ServletRegistration.Dynamic registration, ServletSecurityElement servletSecurityElement) {
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

    /**
     * ({@inheritDoc}
     */
    @Override
    public String getXmlBase() {
        return this.xmlBase;
    }

    /**
     * ({@inheritDoc}
     */
    @Override
    public File getConfigBaseFile() {
        if (hostConfigBase != null) {
            return hostConfigBase;
        }
        String path = null;
        if (getXmlBase()!=null) {
            path = getXmlBase();
        } else {
            StringBuilder xmlDir = new StringBuilder("conf");
            Container parent = getParent();
            if (parent instanceof Engine) {
                xmlDir.append('/');
                xmlDir.append(parent.getName());
            }
            xmlDir.append('/');
            xmlDir.append(getName());
            path = xmlDir.toString();
        }
        File file = new File(path);
        if (!file.isAbsolute()) {
            file = new File(getCatalinaBase(), path);
        }
        try {
            file = file.getCanonicalFile();
        } catch (IOException e) {// ignore
        }
        this.hostConfigBase = file;
        return file;
    }

    @Override
    public String getAppBase() {
        return this.appBase;
    }

    @Override
    public File getAppBaseFile() {

        if (appBaseFile != null) {
            return appBaseFile;
        }

        File file = new File(getAppBase());

        // If not absolute, make it absolute
        if (!file.isAbsolute()) {
            file = new File(getCatalinaBase(), file.getPath());
        }

        // Make it canonical if possible
        try {
            file = file.getCanonicalFile();
        } catch (IOException ioe) {
            // Ignore
        }

        this.appBaseFile = file;
        return file;
    }

    /**
     * @return the value of the deploy on startup flag.  If <code>true</code>, it indicates
     * that this host's child webapps should be discovered and automatically
     * deployed at startup time.
     */
    @Override
    public boolean getDeployOnStartup() {
        return this.deployOnStartup;
    }



    /**
     * @return the Java class name of the context configuration class
     * for new web applications.
     */
    @Override
    public String getConfigClass() {
        return this.configClass;
    }

    @Override
    public void setAppBase(String appBase) {
        if (appBase.trim().equals("")) {
            log.warn(sm.getString("standardHost.problematicAppBase", getName()));
        }
        String oldAppBase = this.appBase;
        this.appBase = appBase;
        support.firePropertyChange("appBase", oldAppBase, this.appBase);
        this.appBaseFile = null;
    }

    @Override
    public String getLegacyAppBase() {
        return this.legacyAppBase;
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
    public File getLegacyAppBaseFile() {
        if (legacyAppBaseFile != null) {
            return legacyAppBaseFile;
        }

        File file = new File(getLegacyAppBase());

        // If not absolute, make it absolute
        if (!file.isAbsolute()) {
            file = new File(getCatalinaBase(), file.getPath());
        }

        // Make it canonical if possible
        try {
            file = file.getCanonicalFile();
        } catch (IOException ioe) {
            // Ignore
        }

        this.legacyAppBaseFile = file;
        return file;
    }


    @Override
    public Pattern getDeployIgnorePattern() {
        return null;
    }

    @Override
    public ExecutorService getStartStopExecutor() {
        return startStopExecutor;
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
