package org.apache.catalina;

public interface Contained {
    /**
     * Get the {@link Container} with which this instance is associated.
     *
     * @return The Container with which this instance is associated or
     *         <code>null</code> if not associated with a Container
     */
    Container getContainer();

    public void setContainer(Container container);
}
