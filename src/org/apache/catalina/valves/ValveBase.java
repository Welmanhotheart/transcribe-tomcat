package org.apache.catalina.valves;

import org.apache.catalina.*;
import org.apache.catalina.util.LifecycleMBeanBase;
import org.apache.juli.logging.Log;
import org.apache.tomcat.util.res.StringManager;

public abstract class ValveBase extends LifecycleMBeanBase implements Contained, Valve {
    protected static final StringManager sm = StringManager.getManager(ValveBase.class);

//------------------------------------------------------ Instance Variables

    /**
     * Does this valve support Servlet 3+ async requests?
     */
    protected boolean asyncSupported;

    /**
     * The next Valve in the pipeline this Valve is a component of.
     */
    protected Valve next = null;

    /**
     * Container log
     */
    protected Log containerLog = null;

    /**
     * The Container whose pipeline this Valve is a component of.
     */
    protected Container container = null;


    //------------------------------------------------------ Constructor

    public ValveBase() {
        this(false);
    }


    public ValveBase(boolean asyncSupported) {
        this.asyncSupported = asyncSupported;
    }

    /**
     * Return the next Valve in this pipeline, or <code>null</code> if this
     * is the last Valve in the pipeline.
     */
    @Override
    public Valve getNext() {
        return next;
    }
    /**
     * Set the Container with which this Valve is associated, if any.
     *
     * @param container The new associated container
     */
    @Override
    public void setContainer(Container container) {
        this.container = container;
    }

    /**
     * Stop this component and implement the requirements
     * of {@link org.apache.catalina.util.LifecycleBase#stopInternal()}.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
    @Override
    protected synchronized void stopInternal() throws LifecycleException {
        setState(LifecycleState.STOPPING);
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
        setState(LifecycleState.STARTING);
    }
    /**
     * Return the Container with which this Valve is associated, if any.
     */
    @Override
    public Container getContainer() {
        return container;
    }


    @Override
    protected void initInternal() throws LifecycleException {
        super.initInternal();
        containerLog = getContainer().getLogger();
    }

    @Override
    public String getObjectNameKeyProperties() {
        StringBuilder name = new StringBuilder("type=Valve");

        Container container = getContainer();

        name.append(container.getMBeanKeyProperties());

        int seq = 0;

        // Pipeline may not be present in unit testing
        Pipeline p = container.getPipeline();
        if (p != null) {
            for (Valve valve : p.getValves()) {
                // Skip null valves
                if (valve == null) {
                    continue;
                }
                // Only compare valves in pipeline until we find this valve
                if (valve == this) {
                    break;
                }
                if (valve.getClass() == this.getClass()) {
                    // Duplicate valve earlier in pipeline
                    // increment sequence number
                    seq ++;
                }
            }
        }

        if (seq > 0) {
            name.append(",seq=");
            name.append(seq);
        }

        String className = this.getClass().getName();
        int period = className.lastIndexOf('.');
        if (period >= 0) {
            className = className.substring(period + 1);
        }
        name.append(",name=");
        name.append(className);

        return name.toString();
    }


    @Override
    public String getDomainInternal() {
        Container c = getContainer();
        if (c == null) {
            return null;
        } else {
            return c.getDomain();
        }
    }

    /**
     * Set the Valve that follows this one in the pipeline it is part of.
     *
     * @param valve The new next valve
     */
    @Override
    public void setNext(Valve valve) {
        this.next = valve;
    }


    /**
     * Execute a periodic task, such as reloading, etc. This method will be
     * invoked inside the classloading context of this container. Unexpected
     * throwables will be caught and logged.
     */
    @Override
    public void backgroundProcess() {
        // NOOP by default
    }

}
