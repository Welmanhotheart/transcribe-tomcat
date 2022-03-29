package org.apache.catalina.startup;

import org.apache.catalina.LifecycleListener;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.digester.ObjectCreateRule;
import org.apache.tomcat.util.res.StringManager;
import org.eclipse.core.internal.events.LifecycleEvent;
import org.xml.sax.Attributes;

/**
 * here is something different? TODO
 */
public class ListenerCreateRule extends ObjectCreateRule {

    private static final Log log = LogFactory.getLog(ListenerCreateRule.class);
    protected static final StringManager sm = StringManager.getManager(ListenerCreateRule.class);


    public ListenerCreateRule(String className, String attributeName) {
        super(className, attributeName);
    }

    @Override
    public void begin(String namespace, String name, Attributes attributes)
            throws Exception {
        if ("true".equals(attributes.getValue("optional"))) {
            try {
                super.begin(namespace, name, attributes);
            } catch (Exception e) {
                String className = getRealClassName(attributes);
                if (log.isDebugEnabled()) {
                    log.info(sm.getString("listener.createFailed", className), e);
                } else {
                    log.info(sm.getString("listener.createFailed", className));
                }
                Object instance = new OptionalListener(className);
                digester.push(instance);
                StringBuilder code = digester.getGeneratedCode();
                if (code != null) {
                    code.append(OptionalListener.class.getName().replace('$', '.')).append(' ');
                    code.append(digester.toVariableName(instance)).append(" = new ");
                    code.append(OptionalListener.class.getName().replace('$', '.')).append("(\"").append(className).append("\");");
                    code.append(System.lineSeparator());
                }
            }
        } else {
            super.begin(namespace, name, attributes);
        }
    }



    public static class OptionalListener implements LifecycleListener {

        protected final String className;

        public OptionalListener(String className) {
            this.className = className;
        }

        @Override
        public void lifecycleEvent(LifecycleEvent event) {

        }
    }
}
