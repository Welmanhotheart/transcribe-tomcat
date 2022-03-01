package org.apache.juli.logging;

public interface Log {
    void error(String class_loader_creation_threw_exception, Throwable t);

    boolean isDebugEnabled();

    void debug(Object message);

    void warn(String msg);
}
