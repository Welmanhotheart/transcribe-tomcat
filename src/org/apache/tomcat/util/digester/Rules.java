package org.apache.tomcat.util.digester;

public interface Rules {
    void add(String pattern, Rule rule);

    /**
     * newly associated Digester instance
     * @param digester
     */
    void setDigester(Digester digester);
}
