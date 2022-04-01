package org.apache.catalina;

public interface ContainerListener {
    /**
     * Acknowledge the occurrence of the specified event.
     *
     * @param event ContainerEvent that has occurred
     */
    public void containerEvent(ContainerEvent event);

}
