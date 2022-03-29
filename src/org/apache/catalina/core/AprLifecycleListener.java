package org.apache.catalina.core;

import org.apache.catalina.LifecycleListener;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;
import org.eclipse.core.internal.events.LifecycleEvent;

public class AprLifecycleListener implements LifecycleListener {
    private static final Log log = LogFactory.getLog(AprLifecycleListener.class);

    protected static String SSLEngine = "on"; //default on
    protected static boolean sslInitialized = false;

    /**
     * The string manager for this package.
     */
    protected static final StringManager sm = StringManager.getManager(AprLifecycleListener.class);


    @Override
    public void lifecycleEvent(LifecycleEvent event) {

    }

    public void setSSLEngine(String SSLEngine) {
        if (!SSLEngine.equals(AprLifecycleListener.SSLEngine)) {
            // Ensure that the SSLEngine is consistent with that used for SSL init
            if (sslInitialized) {
                throw new IllegalStateException(
                        sm.getString("aprListener.tooLateForSSLEngine"));
            }

            AprLifecycleListener.SSLEngine = SSLEngine;
        }
    }

}
