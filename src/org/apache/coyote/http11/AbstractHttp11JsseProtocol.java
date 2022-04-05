package org.apache.coyote.http11;

import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.AbstractJsseEndpoint;

public class AbstractHttp11JsseProtocol <S>
        extends AbstractHttp11Protocol<S> {
    public AbstractHttp11JsseProtocol(AbstractEndpoint<S, ?> endpoint) {
        super(endpoint);
    }
    @Override
    protected AbstractJsseEndpoint<S,?> getEndpoint() {
        // Over-ridden to add cast
        return (AbstractJsseEndpoint<S,?>) super.getEndpoint();
    }

    // ------------------------------------------------ HTTP specific properties
    // ------------------------------------------ passed through to the EndPoint

    public boolean isSSLEnabled() { return getEndpoint().isSSLEnabled();}
    public void setSSLEnabled(boolean SSLEnabled) {
        getEndpoint().setSSLEnabled(SSLEnabled);
    }
    public String getSslImplementationName() { return getEndpoint().getSslImplementationName(); }
    public void setSslImplementationName(String s) { getEndpoint().setSslImplementationName(s); }


}
