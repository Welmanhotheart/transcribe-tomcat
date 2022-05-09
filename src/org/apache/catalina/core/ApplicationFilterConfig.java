package org.apache.catalina.core;

import jakarta.servlet.Filter;
import jakarta.servlet.ServletException;
import org.apache.catalina.Context;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.log.SystemLogHandler;
import org.apache.tomcat.util.modeler.Registry;
import org.apache.tomcat.util.res.StringManager;

import javax.management.ObjectName;
import javax.naming.NamingException;
import java.util.Collections;
import java.util.List;

/***
 * TODO
 * @author <a href="zhiwei.wei@bintools.cn">zhiwei.wei</a>
 * @version 1.0.0 2022-05-2022/5/9-下午7:52
 */
public class ApplicationFilterConfig {

    private static final long serialVersionUID = 1L;

    static final StringManager sm = StringManager.getManager(ApplicationFilterConfig.class);

    private transient Log log = LogFactory.getLog(ApplicationFilterConfig.class); // must not be static

    /**
     * Empty String collection to serve as the basis for empty enumerations.
     */
    private static final List<String> emptyString = Collections.emptyList();


    /**
     * Construct a new ApplicationFilterConfig for the specified filter
     * definition.
     *
     * @param context The context with which we are associated
     * @param filterDef Filter definition for which a FilterConfig is to be
     *  constructed
     *
     * @exception ClassCastException if the specified class does not implement
     *  the <code>jakarta.servlet.Filter</code> interface
     * @exception ClassNotFoundException if the filter class cannot be found
     * @exception IllegalAccessException if the filter class cannot be
     *  publicly instantiated
     * @exception InstantiationException if an exception occurs while
     *  instantiating the filter object
     * @exception ServletException if thrown by the filter's init() method
     * @throws NamingException If a JNDI lookup fails
     * @throws SecurityException If a security manager prevents the creation
     * @throws IllegalArgumentException If the provided configuration is not
     *         valid
     */
    ApplicationFilterConfig(Context context, FilterDef filterDef)
            throws ClassCastException, ReflectiveOperationException, ServletException,
            NamingException, IllegalArgumentException, SecurityException {

        super();

        this.context = context;
        this.filterDef = filterDef;
        // Allocate a new filter instance if necessary
        if (filterDef.getFilter() == null) {
            getFilter();
        } else {
            this.filter = filterDef.getFilter();
            context.getInstanceManager().newInstance(filter);
            initFilter();
        }
    }

    /**
     * Return the application Filter we are configured for.
     *
     * @exception ClassCastException if the specified class does not implement
     *  the <code>jakarta.servlet.Filter</code> interface
     * @exception ClassNotFoundException if the filter class cannot be found
     * @exception IllegalAccessException if the filter class cannot be
     *  publicly instantiated
     * @exception InstantiationException if an exception occurs while
     *  instantiating the filter object
     * @exception ServletException if thrown by the filter's init() method
     * @throws NamingException If a JNDI lookup fails
     * @throws ReflectiveOperationException If the creation of the filter fails
     * @throws SecurityException If a security manager prevents the creation
     * @throws IllegalArgumentException If the provided configuration is not
     *         valid
     */
    Filter getFilter() throws ClassCastException, ReflectiveOperationException, ServletException,
            NamingException, IllegalArgumentException, SecurityException {

        // Return the existing filter instance, if any
        if (this.filter != null) {
            return this.filter;
        }

        // Identify the class loader we will be using
        String filterClass = filterDef.getFilterClass();
        this.filter = (Filter) context.getInstanceManager().newInstance(filterClass);

        initFilter();

        return this.filter;

    }

    private void initFilter() throws ServletException {
        if (context instanceof StandardContext &&
                context.getSwallowOutput()) {
            try {
                SystemLogHandler.startCapture();
                filter.init(this);
            } finally {
                String capturedlog = SystemLogHandler.stopCapture();
                if (capturedlog != null && capturedlog.length() > 0) {
                    getServletContext().log(capturedlog);
                }
            }
        } else {
            filter.init(this);
        }

        // Expose filter via JMX
        registerJMX();
    }

    // -------------------------------------------------------- Private Methods

    private void registerJMX() {
        String parentName = context.getName();
        if (!parentName.startsWith("/")) {
            parentName = "/" + parentName;
        }

        String hostName = context.getParent().getName();
        hostName = (hostName == null) ? "DEFAULT" : hostName;

        // domain == engine name
        String domain = context.getParent().getParent().getName();

        String webMod = "//" + hostName + parentName;
        String onameStr = null;
        String filterName = filterDef.getFilterName();
        if (Util.objectNameValueNeedsQuote(filterName)) {
            filterName = ObjectName.quote(filterName);
        }
        if (context instanceof StandardContext) {
            StandardContext standardContext = (StandardContext) context;
            onameStr = domain + ":j2eeType=Filter,WebModule=" + webMod +
                    ",name=" + filterName + ",J2EEApplication=" +
                    standardContext.getJ2EEApplication() + ",J2EEServer=" +
                    standardContext.getJ2EEServer();
        } else {
            onameStr = domain + ":j2eeType=Filter,name=" + filterName +
                    ",WebModule=" + webMod;
        }
        try {
            oname = new ObjectName(onameStr);
            Registry.getRegistry(null, null).registerComponent(this, oname, null);
        } catch (Exception ex) {
            log.warn(sm.getString("applicationFilterConfig.jmxRegisterFail",
                    getFilterClass(), getFilterName()), ex);
        }
    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The Context with which we are associated.
     */
    private final transient Context context;


    /**
     * The application Filter we are configured for.
     */
    private transient Filter filter = null;


    /**
     * The <code>FilterDef</code> that defines our associated Filter.
     */
    private final FilterDef filterDef;

    /**
     * JMX registration name
     */
    private ObjectName oname;

}