package org.apache.coyote.http11;

import org.apache.tomcat.util.net.NioChannel;
import org.apache.tomcat.util.net.NioEndpoint;

public class Http11NioProtocol extends AbstractHttp11JsseProtocol<NioChannel> {

    public Http11NioProtocol() {
        super(new NioEndpoint());
    }

}
