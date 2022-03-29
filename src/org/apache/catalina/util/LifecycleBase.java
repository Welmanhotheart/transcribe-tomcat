package org.apache.catalina.util;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class LifecycleBase implements Lifecycle {

    /**
     * The list of registered LifecycleListeners for event notifications.
     */
    private final List<LifecycleListener> lifecycleListeners = new CopyOnWriteArrayList<>();



    /**
     * {@inheritDoc}
     */
    @Override
    public void addLifecycleListener(LifecycleListener listener) {
        lifecycleListeners.add(listener);
    }


}
