package org.apache.catalina.core;

import org.apache.catalina.Container;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Pipeline;
import org.apache.catalina.util.LifecycleBase;

public class StandardPipeline extends LifecycleBase implements Pipeline {
    public StandardPipeline(Container container) {

        super();
        setContainer(container);

    }

    /**
     * The Container with which this Pipeline is associated.
     */
    protected Container container = null;

    @Override
    public void setContainer(Container container) {
        this.container = container;
    }

    @Override
    protected void destroyInternal() throws LifecycleException {

    }

    @Override
    protected void stopInternal() throws LifecycleException {

    }

    @Override
    protected void initInternal() throws LifecycleException {

    }

    @Override
    protected void startInternal() throws LifecycleException {

    }
}
