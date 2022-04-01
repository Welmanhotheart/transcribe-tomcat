package org.apache.catalina.core;

import org.apache.catalina.Executor;
import org.apache.catalina.util.LifecycleMBeanBase;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.util.threads.ResizableExecutor;

public class StandardThreadExecutor extends LifecycleMBeanBase
        implements Executor, ResizableExecutor {

    protected static final StringManager sm = StringManager.getManager(StandardThreadExecutor.class);

    @Override
    public void execute(Runnable command) {

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