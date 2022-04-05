package org.apache.catalina.core;

import org.apache.catalina.*;

import java.util.concurrent.ConcurrentHashMap;

public abstract class FrameworkListener implements LifecycleListener, ContainerListener {

    protected final ConcurrentHashMap<Context, LifecycleListener> contextListeners =
            new ConcurrentHashMap<>();


    /**
     * Create a lifecycle listener which will then be added to the specified context.
     * @param context the associated Context
     * @return the lifecycle listener
     */
    protected abstract LifecycleListener createLifecycleListener(Context context);


    @Override
    public void lifecycleEvent(LifecycleEvent event) {
        Lifecycle lifecycle = event.getLifecycle();
        if (Lifecycle.BEFORE_START_EVENT.equals(event.getType()) &&
                lifecycle instanceof Server) {
            Server server = (Server) lifecycle;
            registerListenersForServer(server);
        }
    }

    protected void registerListenersForServer(Server server) {
        for (Service service : server.findServices()) {
            Engine engine = service.getContainer();
            if (engine != null) {
                engine.addContainerListener(this);
                registerListenersForEngine(engine);
            }
        }
    }

    protected void registerListenersForEngine(Engine engine) {
        for (Container hostContainer : engine.findChildren()) {
            Host host = (Host) hostContainer;
            host.addContainerListener(this);
            registerListenersForHost(host);
        }
    }

    protected void registerListenersForHost(Host host) {
        for (Container contextContainer : host.findChildren()) {
            Context context = (Context) contextContainer;
            registerContextListener(context);
        }
    }

    protected void registerContextListener(Context context) {
        LifecycleListener listener = createLifecycleListener(context);
        contextListeners.put(context, listener);
        context.addLifecycleListener(listener);
    }


    @Override
    public void containerEvent(ContainerEvent event) {

    }


}
