package org.apache.coyote.ajp;

import org.apache.coyote.AbstractProtocol;
import org.apache.tomcat.util.net.AbstractEndpoint;

public abstract class AbstractAjpProtocol<S> extends AbstractProtocol<S> {
    public AbstractAjpProtocol(AbstractEndpoint<S, ?> endpoint) {
        super(endpoint);
    }
}
