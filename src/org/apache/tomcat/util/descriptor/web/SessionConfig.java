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

    public String getCookieDomain() {
        return getCookieAttribute(Constants.COOKIE_DOMAIN_ATTR);
    }
    public void setCookieDomain(String cookieDomain) {
        setCookieAttribute(Constants.COOKIE_DOMAIN_ATTR, cookieDomain);
    }

    public String getCookiePath() {
        return getCookieAttribute(Constants.COOKIE_PATH_ATTR);
    }
    public void setCookiePath(String cookiePath) {
        setCookieAttribute(Constants.COOKIE_PATH_ATTR, cookiePath);
    }

    public Boolean getCookieHttpOnly() {
        String httpOnly = getCookieAttribute(Constants.COOKIE_HTTP_ONLY_ATTR);
        if (httpOnly == null) {
            return null;
        }
        return Boolean.valueOf(httpOnly);
    }

    public Boolean getCookieSecure() {
        String secure = getCookieAttribute(Constants.COOKIE_SECURE_ATTR);
        if (secure == null) {
            return null;
        }
        return Boolean.valueOf(secure);
    }

    public Integer getCookieMaxAge() {
        String maxAge = getCookieAttribute(Constants.COOKIE_MAX_AGE_ATTR);
        if (maxAge == null) {
            return null;
        }
        return Integer.valueOf(maxAge);
    }
    public void setCookieMaxAge(String cookieMaxAge) {
        setCookieAttribute(Constants.COOKIE_MAX_AGE_ATTR, cookieMaxAge);
    }

    public void setCookieSecure(String cookieSecure) {
        setCookieAttribute(Constants.COOKIE_SECURE_ATTR, cookieSecure);
    }

    public void setCookieHttpOnly(String cookieHttpOnly) {
        setCookieAttribute(Constants.COOKIE_HTTP_ONLY_ATTR, cookieHttpOnly);
    }

    public String getCookieComment() {
        return getCookieAttribute(Constants.COOKIE_COMMENT_ATTR);
    }
    public void setCookieComment(String cookieComment) {
        setCookieAttribute(Constants.COOKIE_COMMENT_ATTR, cookieComment);
    }

    public Map<String,String> getCookieAttributes() {
        return cookieAttributes;
    }
    public void setCookieAttribute(String name, String value) {
        cookieAttributes.put(name, value);
    }
    public String getCookieAttribute(String name) {
        return cookieAttributes.get(name);
    }


}
