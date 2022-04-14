package org.apache.tomcat.util.descriptor.web;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ContextHandler  extends ResourceBase{


    /**
     * A list of QName specifying the SOAP Headers the handler will work on.
     * -namespace and localpart values must be found inside the WSDL.
     *
     * A service-qname is composed by a namespaceURI and a localpart.
     *
     * soapHeader[0] : namespaceURI
     * soapHeader[1] : localpart
     */
    private final Map<String, String> soapHeaders = new HashMap<>();

    public Iterator<String> getLocalparts() {
        return soapHeaders.keySet().iterator();
    }

    public String getNamespaceuri(String localpart) {
        return soapHeaders.get(localpart);
    }

    public void addSoapHeaders(String localpart, String namespaceuri) {
        soapHeaders.put(localpart, namespaceuri);
    }

    /**
     * The Handler reference class.
     */
    private String handlerclass = null;

    public String getHandlerclass() {
        return this.handlerclass;
    }

    public void setHandlerclass(String handlerclass) {
        this.handlerclass = handlerclass;
    }


}
