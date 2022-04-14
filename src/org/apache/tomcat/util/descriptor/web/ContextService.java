package org.apache.tomcat.util.descriptor.web;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ContextService  extends ResourceBase{

    /**
     * Declares the specific WSDL service element that is being referred to.
     * It is not specified if no wsdl-file is declared or if WSDL contains only
     * 1 service element.
     *
     * A service-qname is composed by a namespaceURI and a localpart.
     * It must be defined if more than 1 service is declared in the WSDL.
     *
     * serviceqname[0] : namespaceURI
     * serviceqname[1] : localpart
     */
    private String[] serviceqname = new String[2];

    public String[] getServiceqname() {
        return this.serviceqname;
    }

    public String getServiceqname(int i) {
        return this.serviceqname[i];
    }

    public String getServiceqnameNamespaceURI() {
        return this.serviceqname[0];
    }

    public String getServiceqnameLocalpart() {
        return this.serviceqname[1];
    }

    public void setServiceqname(String[] serviceqname) {
        this.serviceqname = serviceqname;
    }

    public void setServiceqname(String serviceqname, int i) {
        this.serviceqname[i] = serviceqname;
    }

    public void setServiceqnameNamespaceURI(String namespaceuri) {
        this.serviceqname[0] = namespaceuri;
    }

    public void setServiceqnameLocalpart(String localpart) {
        this.serviceqname[1] = localpart;
    }

    /**
     * Contains the location (relative to the root of
     * the module) of the web service WSDL description.
     */
    private String wsdlfile = null;

    public String getWsdlfile() {
        return this.wsdlfile;
    }

    public void setWsdlfile(String wsdlfile) {
        this.wsdlfile = wsdlfile;
    }

    /**
     * The WebService reference name.
     */
    private String displayname = null;

    public String getDisplayname() {
        return this.displayname;
    }

    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }


    /**
     * A file specifying the correlation of the WSDL definition
     * to the interfaces (Service Endpoint Interface, Service Interface).
     */
    private String jaxrpcmappingfile = null;

    public String getJaxrpcmappingfile() {
        return this.jaxrpcmappingfile;
    }

    public void setJaxrpcmappingfile(String jaxrpcmappingfile) {
        this.jaxrpcmappingfile = jaxrpcmappingfile;
    }

    /**
     * Declares a client dependency on the container to resolving a Service Endpoint Interface
     * to a WSDL port. It optionally associates the Service Endpoint Interface with a
     * particular port-component.
     * @return the endpoint names
     */
    public Iterator<String> getServiceendpoints() {
        return this.listProperties();
    }

    /**
     * The fully qualified class name of the JAX-WS Service interface that the
     * client depends on.
     */
    private String serviceInterface = null;

    public String getInterface() {
        return serviceInterface;
    }

    public void setInterface(String serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    /**
     * A list of Handlers to use for this service-ref.
     *
     * The instantiation of the handler have to be done.
     */
    private final Map<String, ContextHandler> handlers = new HashMap<>();

    public Iterator<String> getHandlers() {
        return handlers.keySet().iterator();
    }

    public ContextHandler getHandler(String handlername) {
        return handlers.get(handlername);
    }

    public void addHandler(ContextHandler handler) {
        handlers.put(handler.getName(), handler);
    }


}
