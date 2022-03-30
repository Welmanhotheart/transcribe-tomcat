package org.apache.tomcat.util.log;

import java.io.PrintStream;

public class SystemLogHandler  extends PrintStream {
    /**
     * Construct the handler to capture the output of the given steam.
     *
     * @param wrapped The stream to capture
     */
    public SystemLogHandler(PrintStream wrapped) {
        super(wrapped);
        out = wrapped;
    }
}
