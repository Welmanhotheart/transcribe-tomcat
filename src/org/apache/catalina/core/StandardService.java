package org.apache.catalina.core;

import org.apache.catalina.Service;
import org.apache.catalina.util.LifecycleMBeanBase;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;


public class StandardService extends LifecycleMBeanBase implements Service {
    private static final Log log = LogFactory.getLog(StandardService.class);
    private static final StringManager sm = StringManager.getManager(StandardService.class);

    @Override
    protected String getObjectNameKeyProperties() {
        return null;
    }

    @Override
    protected String getDomainInternal() {
        return null;
    }

}
