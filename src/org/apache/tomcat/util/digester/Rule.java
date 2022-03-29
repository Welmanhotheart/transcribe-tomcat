package org.apache.tomcat.util.digester;

import org.apache.tomcat.util.res.StringManager;
import org.xml.sax.Attributes;

public class Rule {

    protected static final StringManager sm = StringManager.getManager(Rule.class);


    protected Digester digester;

    /**
     * The namespace URI for which this Rule is relevant, if any.
     */
    protected String namespaceURI = null;


    public void setDigester(Digester digester) {
        this.digester = digester;
    }

    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        // NO-OP by default.
    }

    public void body(String namespace, String name, String text) throws Exception {
        // NO-OP by default.
    }

    public void end(String namespace, String name) throws Exception {
        // NO-OP by default.
    }
    public String getNamespaceURI() {
        return namespaceURI;
    }

}
