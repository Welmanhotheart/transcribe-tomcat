package org.apache.juli;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class ClassLoaderLogManager extends LogManager {


    /**
     * Map containing the classloader information, keyed per classloader. A
     * weak hashmap is used to ensure no classloader reference is leaked from
     * application redeployment.
     */
    protected final Map<ClassLoader, ClassLoaderLogInfo> classLoaderLoggers =
            new WeakHashMap<>(); // Guarded by this

    /**
     * Shuts down the logging system.
     */
    public synchronized void shutdown() {
        // The JVM is being shutdown. Make sure all loggers for all class
        // loaders are shutdown
        for (ClassLoaderLogInfo clLogInfo : classLoaderLoggers.values()) {
            resetLoggers(clLogInfo);
        }
    }

    /**
     * Determines if the shutdown hook is used to perform any necessary
     * clean-up such as flushing buffered handlers on JVM shutdown. Defaults to
     * <code>true</code> but may be set to false if another component ensures
     * that {@link #shutdown()} is called.
     */
    protected volatile boolean useShutdownHook = true;


    // ------------------------------------------------------------- Properties


    public boolean isUseShutdownHook() {
        return useShutdownHook;
    }


    public void setUseShutdownHook(boolean useShutdownHook) {
        this.useShutdownHook = useShutdownHook;
    }

    // -------------------------------------------------------- Private Methods
    private void resetLoggers(ClassLoaderLogInfo clLogInfo) {
        // This differs from LogManager#resetLogger() in that we close not all
        // handlers of all loggers, but only those that are present in our
        // ClassLoaderLogInfo#handlers list. That is because our #addLogger(..)
        // method can use handlers from the parent class loaders, and closing
        // handlers that the current class loader does not own would be not
        // good.
        synchronized (clLogInfo) {
            for (Logger logger : clLogInfo.loggers.values()) {
                Handler[] handlers = logger.getHandlers();
                for (Handler handler : handlers) {
                    logger.removeHandler(handler);
                }
            }
            for (Handler handler : clLogInfo.handlers.values()) {
                try {
                    handler.close();
                } catch (Exception e) {
                    // Ignore
                }
            }
            clLogInfo.handlers.clear();
        }
    }

    protected static final class ClassLoaderLogInfo {
        final LogNode rootNode;
        final Map<String, Logger> loggers = new ConcurrentHashMap<>();
        final Map<String, Handler> handlers = new HashMap<>();
        final Properties props = new Properties();

        ClassLoaderLogInfo(final LogNode rootNode) {
            this.rootNode = rootNode;
        }

    }

    /**
     * Set parent child relationship between the two specified loggers.
     *
     * @param logger The logger
     * @param parent The parent logger
     */
    protected static void doSetParentLogger(final Logger logger,
                                            final Logger parent) {
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            logger.setParent(parent);
            return null;
        });
    }



    // ---------------------------------------------------- LogNode Inner Class

    protected static final class LogNode {
        Logger logger;

        final Map<String, LogNode> children = new HashMap<>();

        final LogNode parent;

        LogNode(final LogNode parent, final Logger logger) {
            this.parent = parent;
            this.logger = logger;
        }

        LogNode(final LogNode parent) {
            this(parent, null);
        }

        LogNode findNode(String name) {
            LogNode currentNode = this;
            if (logger.getName().equals(name)) {
                return this;
            }
            while (name != null) {
                final int dotIndex = name.indexOf('.');
                final String nextName;
                if (dotIndex < 0) {
                    nextName = name;
                    name = null;
                } else {
                    nextName = name.substring(0, dotIndex);
                    name = name.substring(dotIndex + 1);
                }
                LogNode childNode = currentNode.children.get(nextName);
                if (childNode == null) {
                    childNode = new LogNode(currentNode);
                    currentNode.children.put(nextName, childNode);
                }
                currentNode = childNode;
            }
            return currentNode;
        }

        Logger findParentLogger() {
            Logger logger = null;
            LogNode node = parent;
            while (node != null && logger == null) {
                logger = node.logger;
                node = node.parent;
            }
            return logger;
        }

        void setParentLogger(final Logger parent) {
            for (final LogNode childNode : children.values()) {
                if (childNode.logger == null) {
                    childNode.setParentLogger(parent);
                } else {
                    doSetParentLogger(childNode.logger, parent);
                }
            }
        }

    }



}
