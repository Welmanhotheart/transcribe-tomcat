package org.apache.tomcat;

public interface InstanceManager {


    /**
     * Called by the component using the InstanceManager periodically to perform
     * any regular maintenance that might be required. By default, this method
     * is a NO-OP.
     */
    default void backgroundProcess() {
        // NO-OP by default
    }

}
