package org.apache.catalina.core;

import jakarta.servlet.*;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/***
 * TODO
 * @author <a href="zhiwei.wei@bintools.cn">zhiwei.wei</a>
 * @version 1.0.0 2022-04-2022/4/28-下午5:18
 */
public class ApplicationContext  implements ServletContext {

    // ----------------------------------------------------- Instance Variables


    /**
     * The context attributes for this context.
     */
    protected Map<String,Object> attributes = new ConcurrentHashMap<>();
    /**
     * Flag that indicates if a new {@link ServletContextListener} may be added
     * to the application. Once the first {@link ServletContextListener} is
     * called, no more may be added.
     */
    private boolean newServletContextListenerAllowed = true;


    /**
     * List of read only attributes for this context.
     */
    private final Map<String,String> readOnlyAttributes = new ConcurrentHashMap<>();



    @Override
    public void setAttribute(String name, Object object) {

    }

    @Override
    public URL getResource(String path) throws MalformedURLException {
        return null;
    }

    @Override
    public ClassLoader getClassLoader() {
        return null;
    }

    @Override
    public Object getAttribute(String name) {
        return null;
    }

    @Override
    public SessionCookieConfig getSessionCookieConfig() {
        return null;
    }

    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {

    }

    @Override
    public InputStream getResourceAsStream(String path) {
        return null;
    }

    @Override
    public String getContextPath() {
        return null;
    }

    @Override
    public ServletContext getContext(String uripath) {
        return null;
    }

    @Override
    public int getMajorVersion() {
        return 0;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public int getEffectiveMajorVersion() {
        return 0;
    }

    @Override
    public int getEffectiveMinorVersion() {
        return 0;
    }

    @Override
    public String getMimeType(String file) {
        return null;
    }

    @Override
    public Set<String> getResourcePaths(String path) {
        return null;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    @Override
    public RequestDispatcher getNamedDispatcher(String name) {
        return null;
    }

    @Override
    public void log(String msg) {

    }

    @Override
    public void log(String message, Throwable throwable) {

    }

    @Override
    public String getRealPath(String path) {
        return null;
    }

    protected void setNewServletContextListenerAllowed(boolean allowed) {
        this.newServletContextListenerAllowed = allowed;
    }


    /**
     * Set an attribute as read only.
     */
    void setAttributeReadOnly(String name) {

        if (attributes.containsKey(name)) {
            readOnlyAttributes.put(name, name);
        }

    }
}