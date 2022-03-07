package org.apache.catalina.startup;

import org.apache.tomcat.util.digester.ObjectCreateRule;

/**
 * here is something different? TODO
 */
public class ListenerCreateRule extends ObjectCreateRule {
    public ListenerCreateRule(String className, String attributeName) {
        super(className, attributeName);
    }
}
