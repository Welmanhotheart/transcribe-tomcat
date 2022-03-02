package org.apache.juli.logging;

import aQute.bnd.annotation.spi.ServiceConsumer;
import org.apache.catalina.startup.BootStrap;
import org.eclipse.jdt.core.ITypeRoot;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.ServiceLoader;

@ServiceConsumer(value=org.apache.juli.logging.Log.class)//TODO, 这里是干什么的？
public class LogFactory {
    private static final LogFactory singleton = new LogFactory();

    private final Constructor<? extends Log> discoveredLogConstructor;

    private LogFactory() {
        //here does what TODO
        FileSystems.getDefault();

        ServiceLoader<Log> logLoader = ServiceLoader.load(Log.class);
        Constructor<? extends Log> m = null;
        for (Log log : logLoader) {
            Class<? extends Log> c = log.getClass();
            try {
                m = c.getConstructor(String.class);
                break;
            } catch (NoSuchMethodException | SecurityException e) {
                throw new Error(e);
            }
        }
        discoveredLogConstructor = m;
    }

    public static Log getLog(Class<?> clazz) {
        return getFactory().getInstance(clazz);
    }

    private Log getInstance(Class<?> clazz) {
        return getInstance(clazz.getName());
    }

    private Log getInstance(String name) {
        if (discoveredLogConstructor == null) {
            return DirectJDKLog.getInstance(name);
        }
        try {
            return discoveredLogConstructor.newInstance(name);
        } catch (ReflectiveOperationException | IllegalArgumentException e) {
            throw new LogConfigurationException(e);
        }
    }

    private static LogFactory getFactory() {
        return singleton;
    }


}
