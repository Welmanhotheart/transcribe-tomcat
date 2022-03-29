package org.apache.juli.logging;

public interface Log {
    void error(String class_loader_creation_threw_exception, Throwable t);

    boolean isDebugEnabled();

    void debug(Object message);

    public void debug(Object message, Throwable t);

    /**
     * <p> Log a message with error log level. </p>
     *
     * @param message log this message
     */
    public void error(Object message);


    void warn(String msg);

    void info(Object string, Throwable t);

    void info(Object message);

    void warn(Object string, Throwable e);

    boolean isInfoEnabled();
}
