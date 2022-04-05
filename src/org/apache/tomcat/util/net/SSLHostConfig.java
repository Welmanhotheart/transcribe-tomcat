package org.apache.tomcat.util.net;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;

import javax.management.ObjectName;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class SSLHostConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(SSLHostConfig.class);
    private static final StringManager sm = StringManager.getManager(SSLHostConfig.class);
    private String hostName = DEFAULT_SSL_HOST_NAME;
    private ObjectName oname;

    // Must be lower case. SSL host names are always stored using lower case as
    // they are case insensitive but are used by case sensitive code such as
    // keys in Maps.
    protected static final String DEFAULT_SSL_HOST_NAME = "_default_";
    protected static final Set<String> SSL_PROTO_ALL_SET = new HashSet<>();
    public void setHostName(String hostName) {
        this.hostName = hostName.toLowerCase(Locale.ENGLISH);
    }


    /**
     * @return The host name associated with this SSL configuration - always in
     *         lower case.
     */
    public String getHostName() {
        return hostName;
    }

    public ObjectName getObjectName() {
        return oname;
    }


    public void setObjectName(ObjectName oname) {
        this.oname = oname;
    }

}
