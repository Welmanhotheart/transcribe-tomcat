package org.apache.tomcat.util.net;

import javax.management.ObjectName;

public class SocketProperties {


    /**
     * SO_LINGER option, paired with the <code>soLingerOn</code> value.
     * JVM defaults used unless both attributes are set.
     */
    protected Integer soLingerTime = null;

    /**
     * SO_LINGER option, paired with the <code>soLingerTime</code> value.
     * JVM defaults used unless both attributes are set.
     */
    protected Boolean soLingerOn = null;

    /**
     * TCP_NO_DELAY option. JVM default used if not set.
     */
    protected Boolean tcpNoDelay = Boolean.TRUE;


    /**
     * SO_TIMEOUT option. default is 20000.
     */
    protected Integer soTimeout = Integer.valueOf(20000);
    private ObjectName oname = null;

    public void setSoLingerTime(int soLingerTime) {
        this.soLingerTime = Integer.valueOf(soLingerTime);
    }

    public void setSoLingerOn(boolean soLingerOn) {
        this.soLingerOn = Boolean.valueOf(soLingerOn);
    }

    public boolean getTcpNoDelay() {
        return tcpNoDelay.booleanValue();
    }

    public void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = Boolean.valueOf(tcpNoDelay);
    }

    public void setSoTimeout(int soTimeout) {
        this.soTimeout = Integer.valueOf(soTimeout);
    }

    void setObjectName(ObjectName oname) {
        this.oname = oname;
    }

    ObjectName getObjectName() {
        return oname;
    }
}
