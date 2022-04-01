package org.apache.coyote.http11;

import org.apache.tomcat.util.net.AbstractEndpoint;

public class AbstractHttp11JsseProtocol <S>
        extends AbstractHttp11Protocol<S> {
    public AbstractHttp11JsseProtocol(AbstractEndpoint<S, ?> endpoint) {
        super(endpoint);
    }
}
