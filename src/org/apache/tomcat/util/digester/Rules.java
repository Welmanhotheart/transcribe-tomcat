package org.apache.tomcat.util.digester;

import java.util.List;

public interface Rules {
    void add(String pattern, Rule rule);

    /**
     * newly associated Digester instance
     * @param digester
     */
    void setDigester(Digester digester);

    public List<Rule> match(String namespaceURI, String pattern);

}
