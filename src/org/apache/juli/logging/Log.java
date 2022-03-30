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

    /**
     * <p> Is trace logging currently enabled? </p>
     *
     * <p> Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatenation)
     * when the log level is more than trace. </p>
     *
     * @return <code>true</code> if trace level logging is enabled, otherwise
     *         <code>false</code>
     */
    public boolean isTraceEnabled();

    /**
     * <p> Log a message with trace log level. </p>
     *
     * @param message log this message
     */
    public void trace(Object message);


    /**
     * <p> Log an error with trace log level. </p>
     *
     * @param message log this message
     * @param t log this cause
     */
    public void trace(Object message, Throwable t);



}
