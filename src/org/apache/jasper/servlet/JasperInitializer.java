package org.apache.jasper.servlet;

import org.apache.jasper.servlet.jakarta.servlet.ServletContainerInitializer;
import org.apache.jasper.servlet.org.apache.jasper.runtime.JspFactoryImpl;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

public class JasperInitializer implements ServletContainerInitializer {
    private static final String MSG = "org.apache.jasper.servlet.JasperInitializer";
    private final Log log = LogFactory.getLog(JasperInitializer.class); // must not be static

    private static JspFactoryImpl defaultFactory;
}
