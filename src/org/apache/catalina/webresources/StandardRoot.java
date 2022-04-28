package org.apache.catalina.webresources;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResource;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.util.LifecycleMBeanBase;

import java.net.URL;

/***
 * TODO
 * @author <a href="zhiwei.wei@bintools.cn">zhiwei.wei</a>
 * @version 1.0.0 2022-04-2022/4/28-下午5:25
 */
public class StandardRoot extends LifecycleMBeanBase implements WebResourceRoot  {


    private Context context;

    /**
     * Creates a new standard implementation of {@link WebResourceRoot}. A no
     * argument constructor is required for this to work with the digester.
     * {@link #setContext(Context)} must be called before this component is
     * initialized.
     */
    public StandardRoot() {
        // NO-OP
    }

    public StandardRoot(Context context) {
        this.context = context;
    }

    @Override
    public WebResource getResource(String path) {
        return null;
    }

    @Override
    public Context getContext() {
        return null;
    }

    @Override
    public void setContext(Context context) {

    }

    @Override
    public void backgroundProcess() {

    }

    @Override
    public WebResource[] listResources(String path) {
        return new WebResource[0];
    }

    @Override
    public void createWebResourceSet(ResourceSetType type, String webAppMount, URL url, String internalPath) {

    }

    @Override
    public void createWebResourceSet(ResourceSetType type, String webAppMount, String base, String archivePath, String internalPath) {

    }

    @Override
    protected void stopInternal() throws LifecycleException {

    }

    @Override
    protected void startInternal() throws LifecycleException {

    }

    @Override
    protected String getObjectNameKeyProperties() {
        return null;
    }

    @Override
    protected String getDomainInternal() {
        return null;
    }
}