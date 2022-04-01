package org.apache.coyote.ajp;

import org.apache.tomcat.util.net.NioChannel;
import org.apache.tomcat.util.net.NioEndpoint;

public class AjpNioProtocol extends AbstractAjpProtocol<NioChannel>{
    // ------------------------------------------------------------ Constructor

    public AjpNioProtocol() {
        super(new NioEndpoint());
    }
}
