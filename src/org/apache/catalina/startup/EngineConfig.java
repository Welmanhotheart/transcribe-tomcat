package org.apache.catalina.startup;

import org.apache.catalina.Engine;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;

public class EngineConfig implements LifecycleListener {

    private static final Log log = LogFactory.getLog(EngineConfig.class);

    // ----------------------------------------------------- Instance Variables


    /**
     * The Engine we are associated with.
     */
    protected Engine engine = null;


    /**
     * The string resources for this package.
     */
    protected static final StringManager sm =
            StringManager.getManager(Constants.Package);

    /**
     * Process the START event for an associated Engine.
     *
     * @param event The lifecycle event that has occurred
     */
    @Override
    public void lifecycleEvent(LifecycleEvent event) {

        // Identify the engine we are associated with
        try {
            engine = (Engine) event.getLifecycle();
        } catch (ClassCastException e) {
            log.error(sm.getString("engineConfig.cce", event.getLifecycle()), e);
            return;
        }

        // Process the event that has occurred
        if (event.getType().equals(Lifecycle.START_EVENT)) {
            start();
        } else if (event.getType().equals(Lifecycle.STOP_EVENT)) {
            stop();
        }

    }

    /**
     * Process a "stop" event for this Engine.
     */
    protected void stop() {

        if (engine.getLogger().isDebugEnabled()) {
            engine.getLogger().debug(sm.getString("engineConfig.stop"));
        }

    }

    /**
     * Process a "start" event for this Engine.
     */
    protected void start() {

        if (engine.getLogger().isDebugEnabled()) {
            engine.getLogger().debug(sm.getString("engineConfig.start"));
        }

    }

}
