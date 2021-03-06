package org.apache.tomcat.util.net;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.net.jsse.JSSEImplementation;
import org.apache.tomcat.util.res.StringManager;

public class SSLImplementation {

    private static final Log logger = LogFactory.getLog(SSLImplementation.class);
    private static final StringManager sm = StringManager.getManager(SSLImplementation.class);


    /**
     * Obtain an instance (not a singleton) of the implementation with the given
     * class name.
     *
     * @param className The class name of the required implementation or null to
     *                  use the default (currently {@link JSSEImplementation}.
     *
     * @return An instance of the required implementation
     *
     * @throws ClassNotFoundException If an instance of the requested class
     *         cannot be created
     */
    public static SSLImplementation getInstance(String className)
            throws ClassNotFoundException {
        if (className == null) {
            return new JSSEImplementation();
        }

        try {
            Class<?> clazz = Class.forName(className);
            return (SSLImplementation) clazz.getConstructor().newInstance();
        } catch (Exception e) {
            String msg = sm.getString("sslImplementation.cnfe", className);
            if (logger.isDebugEnabled()) {
                logger.debug(msg, e);
            }
            throw new ClassNotFoundException(msg, e);
        }
    }

}
