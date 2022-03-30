package org.apache.catalina.mbeans;

import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;

public class GlobalResourcesLifecycleListener  implements LifecycleListener {
    private static final Log log = LogFactory.getLog(GlobalResourcesLifecycleListener.class);
    protected static final StringManager sm = StringManager.getManager(GlobalResourcesLifecycleListener.class);
    @Override
    public void lifecycleEvent(LifecycleEvent event) {

    }
}
