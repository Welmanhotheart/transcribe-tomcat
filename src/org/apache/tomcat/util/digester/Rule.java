package org.apache.tomcat.util.digester;

import org.xml.sax.Attributes;

public class Rule {
    protected Digester digester;

    public void setDigester(Digester digester) {
        this.digester = digester;
    }

    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        // NO-OP by default.
    }
}
