package org.apache.tomcat.util.descriptor;

import org.apache.juli.logging.Log;
import org.apache.tomcat.util.res.StringManager;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.util.ArrayList;
import java.util.List;

public class XmlErrorHandler implements ErrorHandler {

    private static final StringManager sm =
            StringManager.getManager(Constants.PACKAGE_NAME);

    private final List<SAXParseException> errors = new ArrayList<>();

    private final List<SAXParseException> warnings = new ArrayList<>();

    public List<SAXParseException> getWarnings() {
        // Internal use only - don't worry about immutability
        return warnings;
    }

    @Override
    public void warning(SAXParseException e) throws SAXException {

    }

    @Override
    public void error(SAXParseException e) throws SAXException {

    }

    public void logFindings(Log log, String source) {
        for (SAXParseException e : getWarnings()) {
            log.warn(sm.getString(
                    "xmlErrorHandler.warning", e.getMessage(), source));
        }
        for (SAXParseException e : getErrors()) {
            log.warn(sm.getString(
                    "xmlErrorHandler.error", e.getMessage(), source));
        }
    }

    public List<SAXParseException> getErrors() {
        // Internal use only - don't worry about immutability
        return errors;
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {

    }
}
