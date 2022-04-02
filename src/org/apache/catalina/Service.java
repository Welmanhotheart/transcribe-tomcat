package org.apache.catalina;

import org.apache.catalina.connector.Connector;
import org.apache.catalina.mapper.Mapper;

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

    /**
     * Add a new Connector to the set of defined Connectors, and associate it
     * with this Service's Container.
     *
     * @param connector The Connector to be added
     */
    public void addConnector(Connector connector);

    /**
     * @return the mapper associated with this Service.
     */
    Mapper getMapper();

}
