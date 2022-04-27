package org.apache.tomcat.util.http;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.nio.charset.Charset;

public class Rfc6265CookieProcessor extends CookieProcessorBase{
    @Override
    public void parseCookieHeader(MimeHeaders headers, ServerCookies serverCookies) {

    }

    @Override
    public String generateHeader(Cookie cookie, HttpServletRequest request) {
        return null;
    }

    @Override
    public Charset getCharset() {
        return null;
    }
}
