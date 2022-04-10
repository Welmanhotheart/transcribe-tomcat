package org.apache.jasper.servlet.org.apache.jasper.runtime.jakarta.servlet.jsp;

public abstract class JspFactory {
    private static volatile JspFactory deflt = null;

    /**
     * Sole constructor. (For invocation by subclass constructors,
     * typically implicit.)
     */
    public JspFactory() {
        // NOOP by default
    }
}
