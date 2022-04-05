package org.apache.catalina.valves;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

public class AccessLogValve  extends AbstractAccessLogValve {
    private static final Log log = LogFactory.getLog(AccessLogValve.class);

    //------------------------------------------------------ Constructor
    public AccessLogValve() {
        super();
    }
}
