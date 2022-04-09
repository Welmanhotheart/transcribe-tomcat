package org.apache.catalina.valves;

import org.apache.catalina.LifecycleException;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.buf.B2CConverter;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class AccessLogValve  extends AbstractAccessLogValve {
    private static final Log log = LogFactory.getLog(AccessLogValve.class);

    /**
     * Date format to place in log file name.
     */
    protected String fileDateFormat = ".yyyy-MM-dd";

    /**
     * A date formatter to format a Date using the format
     * given by <code>fileDateFormat</code>.
     */
    protected SimpleDateFormat fileDateFormatter = null;

    /**
     * The as-of date for the currently open log file, or a zero-length
     * string if there is no open log file.
     */
    private volatile String dateStamp = "";

    /**
     * Character set used by the log file. If it is <code>null</code>, the
     * system default character set will be used. An empty string will be
     * treated as <code>null</code> when this property is assigned.
     */
    protected volatile String encoding = null;

    /**
     * The PrintWriter to which we are currently logging, if any.
     */
    protected PrintWriter writer = null;

    /**
     * The current log file we are writing to. Helpful when checkExists
     * is true.
     */
    protected File currentLogFile = null;

    /**
     * Should we rotate our log file? Default is true (like old behavior)
     */
    protected boolean rotatable = true;

    /**
     * The directory in which log files are created.
     */
    private String directory = "logs";

    private volatile boolean checkForOldLogs = false;

    /**
     * The suffix that is added to log file filenames.
     */
    protected volatile String suffix = "";

    /**
     * The prefix that is added to log file filenames.
     */
    protected volatile String prefix = "access_log";

    /**
     * Should we defer inclusion of the date stamp in the file
     * name until rotate time? Default is false.
     */
    protected boolean renameOnRotate = false;
    //------------------------------------------------------ Constructor
    public AccessLogValve() {
        super();
    }

    /**
     * Start this component and implement the requirements
     * of {@link org.apache.catalina.util.LifecycleBase#startInternal()}.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
    @Override
    protected synchronized void startInternal() throws LifecycleException {

        // Initialize the Date formatters
        String format = getFileDateFormat();
        fileDateFormatter = new SimpleDateFormat(format, Locale.US);
        fileDateFormatter.setTimeZone(TimeZone.getDefault());
        dateStamp = fileDateFormatter.format(new Date(System.currentTimeMillis()));
        if (rotatable && renameOnRotate) {
            restore();
        }
        open();

        super.startInternal();
    }


    /**
     * Open the new log file for the date specified by <code>dateStamp</code>.
     */
    protected synchronized void open() {
        // Open the current log file
        // If no rotate - no need for dateStamp in fileName
        File pathname = getLogFile(rotatable && !renameOnRotate);

        Charset charset = null;
        if (encoding != null) {
            try {
                charset = B2CConverter.getCharset(encoding);
            } catch (UnsupportedEncodingException ex) {
                log.error(sm.getString(
                        "accessLogValve.unsupportedEncoding", encoding), ex);
            }
        }
        if (charset == null) {
            charset = StandardCharsets.ISO_8859_1;
        }

        try {
            writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(pathname, true), charset), 128000),
                    false);

            currentLogFile = pathname;
        } catch (IOException e) {
            writer = null;
            currentLogFile = null;
            log.error(sm.getString("accessLogValve.openFail", pathname, System.getProperty("user.name")), e);
        }
        // Rotating a log file will always trigger a new file to be opened so
        // when a new file is opened, check to see if any old files need to be
        // removed.
        checkForOldLogs = true;
    }

    // -------------------------------------------------------- Private Methods


    private File getDirectoryFile() {
        File dir = new File(directory);
        if (!dir.isAbsolute()) {
            dir = new File(getContainer().getCatalinaBase(), directory);
        }
        return dir;
    }


    /**
     * Create a File object based on the current log file name.
     * Directories are created as needed but the underlying file
     * is not created or opened.
     *
     * @param useDateStamp include the timestamp in the file name.
     * @return the log file object
     */
    private File getLogFile(boolean useDateStamp) {
        // Create the directory if necessary
        File dir = getDirectoryFile();
        if (!dir.mkdirs() && !dir.isDirectory()) {
            log.error(sm.getString("accessLogValve.openDirFail", dir));
        }

        // Calculate the current log file name
        File pathname;
        if (useDateStamp) {
            pathname = new File(dir.getAbsoluteFile(), prefix + dateStamp
                    + suffix);
        } else {
            pathname = new File(dir.getAbsoluteFile(), prefix + suffix);
        }
        File parent = pathname.getParentFile();
        if (!parent.mkdirs() && !parent.isDirectory()) {
            log.error(sm.getString("accessLogValve.openDirFail", parent));
        }
        return pathname;
    }


    /**
     * Move a current but rotated log file back to the unrotated
     * one. Needed if date stamp inclusion is deferred to rotation
     * time.
     */
    private void restore() {
        File newLogFile = getLogFile(false);
        File rotatedLogFile = getLogFile(true);
        if (rotatedLogFile.exists() && !newLogFile.exists() &&
                !rotatedLogFile.equals(newLogFile)) {
            try {
                if (!rotatedLogFile.renameTo(newLogFile)) {
                    log.error(sm.getString("accessLogValve.renameFail", rotatedLogFile, newLogFile));
                }
            } catch (Throwable e) {
                ExceptionUtils.handleThrowable(e);
                log.error(sm.getString("accessLogValve.renameFail", rotatedLogFile, newLogFile), e);
            }
        }
    }


    /**
     * @return the date format date based log rotation.
     */
    public String getFileDateFormat() {
        return fileDateFormat;
    }


    /**
     * Set the date format date based log rotation.
     * @param fileDateFormat The format for the file timestamp
     */
    public void setFileDateFormat(String fileDateFormat) {
        String newFormat;
        if (fileDateFormat == null) {
            newFormat = "";
        } else {
            newFormat = fileDateFormat;
        }
        this.fileDateFormat = newFormat;

        synchronized (this) {
            fileDateFormatter = new SimpleDateFormat(newFormat, Locale.US);
            fileDateFormatter.setTimeZone(TimeZone.getDefault());
        }
    }

}
