package org.apache.catalina;

public interface Engine  extends Container {
    /**
     * Set the default hostname for this Engine.
     *
     * @param defaultHost The new default host
     */
    public void setDefaultHost(String defaultHost);

    /**
     * Set the <code>Service</code> with which we are associated (if any).
     *
     * @param service The service that owns this Engine
     */
    public void setService(Service service);

    /**
     * @return the <code>Service</code> with which we are associated (if any).
     */
    public Service getService();
}
