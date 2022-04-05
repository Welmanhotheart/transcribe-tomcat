package org.apache.tomcat.util.security;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;

import java.lang.reflect.Field;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class PrivilegedSetAccessControlContext  implements PrivilegedAction<Void> {

    private static final Log log = LogFactory.getLog(PrivilegedSetAccessControlContext.class);
    private static final StringManager sm = StringManager.getManager(PrivilegedSetAccessControlContext.class);

    private static final AccessControlContext acc;
    private static final Field field;
    private final Thread t;

    static {
        acc = AccessController.getContext();
        Field f = null;
        try {
            f = Thread.class.getDeclaredField("inheritedAccessControlContext");
            f.trySetAccessible();
        } catch (NoSuchFieldException | SecurityException e) {
            log.warn(sm.getString("privilegedSetAccessControlContext.lookupFailed"), e);
        }
        field = f;
    }



    public PrivilegedSetAccessControlContext(Thread t) {
        this.t = t;
    }

    @Override
    public Void run() {
        try {
            if (field != null) {
                field.set(t,  acc);
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            log.warn(sm.getString("privilegedSetAccessControlContext.setFailed"), e);
        }
        return null;
    }
}
