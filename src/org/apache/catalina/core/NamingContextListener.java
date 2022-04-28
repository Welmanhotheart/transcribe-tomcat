package org.apache.catalina.core;

import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/***
 * TODO
 * @author <a href="zhiwei.wei@bintools.cn">zhiwei.wei</a>
 * @version 1.0.0 2022-04-2022/4/28-下午7:35
 */
public class NamingContextListener implements LifecycleListener, PropertyChangeListener {

    // ----------------------------------------------------- Instance Variables

    /**
     * Name of the associated naming context.
     */
    protected String name = "/";


    /**
     * Env context.
     */
    protected javax.naming.Context envCtx = null;


    /**
     * Determines if an attempt to write to a read-only context results in an
     * exception or if the request is ignored.
     */
    private boolean exceptionOnFailedWrite = true;



    @Override
    public void propertyChange(PropertyChangeEvent evt) {

    }

    @Override
    public void lifecycleEvent(LifecycleEvent event) {

    }

    /**
     * @return the "name" property.
     */
    public String getName() {
        return this.name;
    }


    /**
     * Set the "name" property.
     *
     * @param name The new name
     */
    public void setName(String name) {
        this.name = name;
    }

// ------------------------------------------------------------- Properties

    /**
     * @return whether or not an attempt to modify the JNDI context will trigger
     * an exception or if the request will be ignored.
     */
    public boolean getExceptionOnFailedWrite() {
        return exceptionOnFailedWrite;
    }

    /**
     * @return the naming environment context.
     */
    public javax.naming.Context getEnvContext() {
        return this.envCtx;
    }

    /**
     * Controls whether or not an attempt to modify the JNDI context will
     * trigger an exception or if the request will be ignored.
     *
     * @param exceptionOnFailedWrite    The new value
     */
    public void setExceptionOnFailedWrite(boolean exceptionOnFailedWrite) {
        this.exceptionOnFailedWrite = exceptionOnFailedWrite;
    }

}