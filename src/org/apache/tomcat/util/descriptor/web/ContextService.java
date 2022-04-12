package org.apache.tomcat.util.descriptor.web;

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

}
