package org.apache.juli.logging;

import java.util.logging.Logger;

public class DirectJDKLog implements Log{
    private final Logger logger;

    public DirectJDKLog(String name) {
        logger = Logger.getLogger(name);
    }

    public static Log getInstance(String name) {
        return new DirectJDKLog(name);
    }

    @Override
    public void error(String class_loader_creation_threw_exception, Throwable t) {

    }

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public void debug(Object message) {

    }

    @Override
    public void debug(Object message, Throwable t) {

    }

    @Override
    public void error(Object message) {

    }

    @Override
    public void warn(String msg) {

    }

    @Override
    public void info(Object string, Throwable t) {

    }

    @Override
    public void info(Object message) {

    }

    @Override
    public void warn(Object string, Throwable e) {

    }

    @Override
    public boolean isInfoEnabled() {
        return false;
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public void trace(Object message) {

    }

    @Override
    public void trace(Object message, Throwable t) {

    }
}
