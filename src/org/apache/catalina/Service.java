package org.apache.catalina;

public interface Service {
    /**
     * Retrieves executor by name, null if not found
     * @param name String
     * @return Executor
     */
    public Executor getExecutor(String name);

    /**
     * @return the domain under which this container will be / has been
     * registered.
     */
    public String getDomain();


}
