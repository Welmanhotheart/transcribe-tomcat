package org.apache.juli.logging;

import aQute.bnd.annotation.spi.ServiceConsumer;
import org.apache.catalina.startup.BootStrap;

@ServiceConsumer(value=org.apache.juli.logging.Log.class)//TODO, 这里是干什么的？
public class LogFactory {
    public static Log getLog(Class<?> bootStrapClass) {
        return null;
    }
}
