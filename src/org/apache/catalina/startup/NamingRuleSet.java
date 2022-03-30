package org.apache.catalina.startup;

import org.apache.tomcat.util.digester.Digester;
import org.apache.tomcat.util.digester.RuleSet;

public class NamingRuleSet implements RuleSet {
    /**
     * The matching pattern prefix to use for recognizing our elements.
     */
    protected final String prefix;

    public NamingRuleSet(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public void addRuleInstances(Digester digester) {

        digester.addObjectCreate(prefix + "Ejb",
                "org.apache.tomcat.util.descriptor.web.ContextEjb");
        digester.addSetProperties(prefix + "Ejb");
        digester.addRule(prefix + "Ejb",
                new SetNextNamingRule("addEjb",
                        "org.apache.tomcat.util.descriptor.web.ContextEjb"));

        digester.addObjectCreate(prefix + "Environment",
                "org.apache.tomcat.util.descriptor.web.ContextEnvironment");
        digester.addSetProperties(prefix + "Environment");
        digester.addRule(prefix + "Environment",
                new SetNextNamingRule("addEnvironment",
                        "org.apache.tomcat.util.descriptor.web.ContextEnvironment"));

        digester.addObjectCreate(prefix + "LocalEjb",
                "org.apache.tomcat.util.descriptor.web.ContextLocalEjb");
        digester.addSetProperties(prefix + "LocalEjb");
        digester.addRule(prefix + "LocalEjb",
                new SetNextNamingRule("addLocalEjb",
                        "org.apache.tomcat.util.descriptor.web.ContextLocalEjb"));

        digester.addObjectCreate(prefix + "Resource",
                "org.apache.tomcat.util.descriptor.web.ContextResource");
        digester.addSetProperties(prefix + "Resource");
        digester.addRule(prefix + "Resource",
                new SetNextNamingRule("addResource",
                        "org.apache.tomcat.util.descriptor.web.ContextResource"));

        digester.addObjectCreate(prefix + "ResourceEnvRef",
                "org.apache.tomcat.util.descriptor.web.ContextResourceEnvRef");
        digester.addSetProperties(prefix + "ResourceEnvRef");
        digester.addRule(prefix + "ResourceEnvRef",
                new SetNextNamingRule("addResourceEnvRef",
                        "org.apache.tomcat.util.descriptor.web.ContextResourceEnvRef"));

        digester.addObjectCreate(prefix + "ServiceRef",
                "org.apache.tomcat.util.descriptor.web.ContextService");
        digester.addSetProperties(prefix + "ServiceRef");
        digester.addRule(prefix + "ServiceRef",
                new SetNextNamingRule("addService",
                        "org.apache.tomcat.util.descriptor.web.ContextService"));

        digester.addObjectCreate(prefix + "Transaction",
                "org.apache.tomcat.util.descriptor.web.ContextTransaction");
        digester.addSetProperties(prefix + "Transaction");
        digester.addRule(prefix + "Transaction",
                new SetNextNamingRule("setTransaction",
                        "org.apache.tomcat.util.descriptor.web.ContextTransaction"));
    }
}
