package org.apache.catalina.connector;

import org.apache.coyote.Adapter;
import org.apache.tomcat.util.res.StringManager;

public class CoyoteAdapter implements Adapter {
    /**
     * The CoyoteConnector with which this processor is associated.
     */
    private final Connector connector;


    /**
     * The string manager for this package.
     */
    protected static final StringManager sm = StringManager.getManager(CoyoteAdapter.class);


    /**
     * Construct a new CoyoteProcessor associated with the specified connector.
     *
     * @param connector CoyoteConnector that owns this processor
     */
    public CoyoteAdapter(Connector connector) {

        super();
        this.connector = connector;

    }

    @Override
    public String getDomain() {
        return connector.getDomain();
    }

}
