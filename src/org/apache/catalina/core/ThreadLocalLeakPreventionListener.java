package org.apache.catalina.core;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleListener;

public class ThreadLocalLeakPreventionListener extends FrameworkListener{
    @Override
    protected LifecycleListener createLifecycleListener(Context context) {
        return this;
    }
}
