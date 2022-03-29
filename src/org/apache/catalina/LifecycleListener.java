package org.apache.catalina;

import org.eclipse.core.internal.events.LifecycleEvent;

public interface LifecycleListener {
    /**
     * Acknowledge the occurrence of the specified event.
     *
     * @param event LifecycleEvent that has occurred
     */
    public void lifecycleEvent(LifecycleEvent event);

}
