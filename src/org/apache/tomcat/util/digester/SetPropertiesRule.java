package org.apache.tomcat.util.digester;

import org.apache.tomcat.util.IntrospectionUtils;
import org.xml.sax.Attributes;

import java.util.HashMap;

public class SetPropertiesRule extends Rule{
    protected final HashMap<String,String> excludes;

    public interface Listener {
        void endSetPropertiesRule();
    }

    public SetPropertiesRule() {
        excludes = null;
    }

    public SetPropertiesRule(String[] exclude) {
        excludes = new HashMap<>();
        for (String s : exclude) {
            if (s != null) {
                this.excludes.put(s, s);
            }
        }
    }

    @Override
    public void begin(String namespace, String theName, Attributes attributes)
            throws Exception {

        // Populate the corresponding properties of the top object
        Object top = digester.peek();
        if (digester.log.isDebugEnabled()) {
            if (top != null) {
                digester.log.debug("[SetPropertiesRule]{" + digester.match +
                        "} Set " + top.getClass().getName() +
                        " properties");
            } else {
                digester.log.debug("[SetPropertiesRule]{" + digester.match +
                        "} Set NULL properties");
            }
        }
        StringBuilder code = digester.getGeneratedCode();
        String variableName = null;
        if (code != null) {
            variableName = digester.toVariableName(top);
        }

        for (int i = 0; i < attributes.getLength(); i++) {
            String name = attributes.getLocalName(i);
            if (name.isEmpty()) {
                name = attributes.getQName(i);
            }
            String value = attributes.getValue(i);

            if (digester.log.isDebugEnabled()) {
                digester.log.debug("[SetPropertiesRule]{" + digester.match +
                        "} Setting property '" + name + "' to '" +
                        value + "'");
            }
            if (!digester.isFakeAttribute(top, name) && (excludes == null || !excludes.containsKey(name))) {
                StringBuilder actualMethod = null;
                if (code != null) {
                    actualMethod = new StringBuilder();
                }
                if (!IntrospectionUtils.setProperty(top, name, value, true, actualMethod)) {
                    if (digester.getRulesValidation() && !"optional".equals(name)) {
                        digester.log.warn(sm.getString("rule.noProperty", digester.match, name, value));
                    }
                } else {
                    if (code != null) {
                        code.append(variableName).append(".").append(actualMethod).append(';');
                        code.append(System.lineSeparator());
                    }
                }
            }
        }

        if (top instanceof Listener) {
            ((Listener) top).endSetPropertiesRule();
            if (code != null) {
                code.append("((org.apache.tomcat.util.digester.SetPropertiesRule.Listener) ");
                code.append(variableName).append(").endSetPropertiesRule();");
                code.append(System.lineSeparator());
            }
        }

    }




}
