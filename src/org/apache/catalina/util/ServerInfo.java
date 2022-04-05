package org.apache.catalina.util;

import org.apache.tomcat.util.ExceptionUtils;

import java.io.InputStream;
import java.util.Properties;

public class ServerInfo {

    // ------------------------------------------------------- Static Variables


    /**
     * The server information String with which we identify ourselves.
     */
    private static final String serverInfo;

    /**
     * The server built String.
     */
    private static final String serverBuilt;

    /**
     * The server's version number String.
     */
    private static final String serverNumber;

    static {

        String info = null;
        String built = null;
        String number = null;

        Properties props = new Properties();
        try (InputStream is = ServerInfo.class.getResourceAsStream
                ("/org/apache/catalina/util/ServerInfo.properties")) {
            props.load(is);
            info = props.getProperty("server.info");
            built = props.getProperty("server.built");
            number = props.getProperty("server.number");
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
        }
        if (info == null || info.equals("Apache Tomcat/@VERSION@")) {
            info = "Apache Tomcat/10.1.x-dev";
        }
        if (built == null || built.equals("@VERSION_BUILT@")) {
            built = "unknown";
        }
        if (number == null || number.equals("@VERSION_NUMBER@")) {
            number = "10.1.x";
        }

        serverInfo = info;
        serverBuilt = built;
        serverNumber = number;
    }


    // --------------------------------------------------------- Public Methods


    /**
     * @return the server identification for this version of Tomcat.
     */
    public static String getServerInfo() {
        return serverInfo;
    }

    /**
     * @return the server built time for this version of Tomcat.
     */
    public static String getServerBuilt() {
        return serverBuilt;
    }

    /**
     * @return the server's version number.
     */
    public static String getServerNumber() {
        return serverNumber;
    }

}
