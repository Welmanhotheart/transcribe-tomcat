package org.apache.catalina.core;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.Valve;
import org.apache.catalina.valves.ValveBase;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;

public class StandardHostValve  extends ValveBase {
    private static final Log log = LogFactory.getLog(StandardHostValve.class);
    private static final StringManager sm = StringManager.getManager(StandardHostValve.class);

    // Saves a call to getClassLoader() on very request. Under high load these
    // calls took just long enough to appear as a hot spot (although a very
    // minor one) in a profiler.
    private static final ClassLoader MY_CLASSLOADER = StandardHostValve.class.getClassLoader();

    //------------------------------------------------------ Constructor

    public StandardHostValve() {
        super(true);
    }

}
