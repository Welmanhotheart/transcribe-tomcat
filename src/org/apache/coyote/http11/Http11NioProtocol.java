package org.apache.coyote.http11;

import org.apache.coyote.Adapter;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.net.NioChannel;
import org.apache.tomcat.util.net.NioEndpoint;

import java.util.concurrent.ScheduledExecutorService;

public class Http11NioProtocol extends AbstractHttp11JsseProtocol<NioChannel> {

    private static final Log log = LogFactory.getLog(Http11NioProtocol.class);

    public Http11NioProtocol() {
        super(new NioEndpoint());
    }

    @Override
    protected Log getLog() { return log; }

    // ----------------------------------------------------- JMX related methods

    @Override
    protected String getNamePrefix() {
        if (isSSLEnabled()) {
            return "https-" + getSslImplementationShortName()+ "-nio";
        } else {
            return "http-nio";
        }
    }

}
