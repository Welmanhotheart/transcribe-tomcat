package org.apache.catalina;

import java.util.EventObject;

public class LifecycleEvent extends EventObject {

    /**
     * The event data associated with this event.
     */
    private final Object data;


    /**
     * The event type this instance represents.
     */
    private final String type;




    /**
     * Construct a new LifecycleEvent with the specified parameters.
     *
     * @param lifecycle Component on which this event occurred
     * @param type Event type (required)
     * @param data Event data (if any)
     */
    public LifecycleEvent(Lifecycle lifecycle, String type, Object data) {
        super(lifecycle);
        this.type = type;
        this.data = data;
    }
}
