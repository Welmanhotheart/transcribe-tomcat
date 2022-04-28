package org.apache.catalina.session;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Manager;
import org.apache.catalina.util.LifecycleMBeanBase;

/***
 * TODO
 * @author <a href="zhiwei.wei@bintools.cn">zhiwei.wei</a>
 * @version 1.0.0 2022-04-2022/4/28-下午8:27
 */
public class ManagerBase  extends LifecycleMBeanBase implements Manager {
    @Override
    public Context getContext() {
        return null;
    }

    @Override
    public int getActiveSessions() {
        return 0;
    }

    @Override
    public void backgroundProcess() {

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