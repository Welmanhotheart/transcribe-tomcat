package org.apache.tomcat.util.descriptor.web;

import jakarta.servlet.SessionTrackingMode;

import java.util.EnumSet;
import java.util.Map;
import java.util.TreeMap;

public class SessionConfig {
    private Integer sessionTimeout;
    private String cookieName;
    private final Map<String,String> cookieAttributes = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final EnumSet<SessionTrackingMode> sessionTrackingModes =
            EnumSet.noneOf(SessionTrackingMode.class);

    public Integer getSessionTimeout() {
        return sessionTimeout;
    }
    public void setSessionTimeout(String sessionTimeout) {
        this.sessionTimeout = Integer.valueOf(sessionTimeout);
    }

    public String getCookieName() {
        return cookieName;
    }
    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
    }


    public EnumSet<SessionTrackingMode> getSessionTrackingModes() {
        return sessionTrackingModes;
    }
    public void addSessionTrackingMode(String sessionTrackingMode) {
        sessionTrackingModes.add(
                SessionTrackingMode.valueOf(sessionTrackingMode));
    }

}
