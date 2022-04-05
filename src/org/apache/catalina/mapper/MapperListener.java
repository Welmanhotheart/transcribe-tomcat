package org.apache.catalina.mapper;

import org.apache.catalina.*;
import org.apache.catalina.util.LifecycleMBeanBase;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;

public class MapperListener  extends LifecycleMBeanBase
        implements ContainerListener, LifecycleListener {


    private static final Log log = LogFactory.getLog(MapperListener.class);


    // ----------------------------------------------------- Instance Variables
    /**
     * Associated mapper.
     */
    private final Mapper mapper;

    /**
     * Associated service
     */
    private final Service service;


    /**
     * The string manager for this package.
     */
    private static final StringManager sm =
            StringManager.getManager(Constants.Package);

    /**
     * The domain (effectively the engine) this mapper is associated with
     */
    private final String domain = null;


    // ----------------------------------------------------------- Constructors

    /**
     * Create mapper listener.
     *
     * @param service The service this listener is associated with
     */
    public MapperListener(Service service) {
        this.service = service;
        this.mapper = service.getMapper();
    }


    @Override
    public void containerEvent(ContainerEvent event) {

    }

    @Override
    public void lifecycleEvent(LifecycleEvent event) {

    }

    @Override
    protected void stopInternal() throws LifecycleException {

    }

    @Override
    protected void startInternal() throws LifecycleException {

    }

    @Override
    protected String getObjectNameKeyProperties() {
        // Same as connector but Mapper rather than Connector
        return "type=Mapper";
    }

    @Override
    protected String getDomainInternal() {
        if (service instanceof LifecycleMBeanBase) {
            return service.getDomain();
        } else {
            return null;
        }
    }
}
