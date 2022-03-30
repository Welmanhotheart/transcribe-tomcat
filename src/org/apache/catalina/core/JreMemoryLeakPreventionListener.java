package org.apache.catalina.core;

import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;

public class JreMemoryLeakPreventionListener implements LifecycleListener {
    private static final Log log = LogFactory.getLog(JreMemoryLeakPreventionListener.class);
    private static final StringManager sm = StringManager.getManager(JreMemoryLeakPreventionListener.class);

    @Override
    public void lifecycleEvent(LifecycleEvent event) {

    }
}
