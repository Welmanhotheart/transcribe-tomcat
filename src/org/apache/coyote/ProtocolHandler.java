package org.apache.coyote;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ScheduledExecutorService;

public interface ProtocolHandler {


    /**
     * Get the utility executor that should be used by the protocol handler.
     * @return the executor
     */
    public ScheduledExecutorService getUtilityExecutor();


    /**
     * Set the utility executor that should be used by the protocol handler.
     * @param utilityExecutor the executor
     */
    public void setUtilityExecutor(ScheduledExecutorService utilityExecutor);

    /**
     * Add a new protocol for used by HTTP/1.1 upgrade or ALPN.
     * @param upgradeProtocol the protocol
     */
    public void addUpgradeProtocol(UpgradeProtocol upgradeProtocol);

    /**
     * Return all configured upgrade protocols.
     * @return the protocols
     */
    public UpgradeProtocol[] findUpgradeProtocols();

    /**
     * Return the adapter associated with the protocol handler.
     * @return the adapter
     */
    public Adapter getAdapter();


    /**
     * The adapter, used to call the connector.
     *
     * @param adapter The adapter to associate
     */
    public void setAdapter(Adapter adapter);


    /**
     * Create a new ProtocolHandler for the given protocol.
     * @param protocol the protocol
     * @return the newly instantiated protocol handler
     * @throws ClassNotFoundException Specified protocol was not found
     * @throws InstantiationException Specified protocol could not be instantiated
     * @throws IllegalAccessException Exception occurred
     * @throws IllegalArgumentException Exception occurred
     * @throws InvocationTargetException Exception occurred
     * @throws NoSuchMethodException Exception occurred
     * @throws SecurityException Exception occurred
     */
    public static ProtocolHandler create(String protocol)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        if (protocol == null || "HTTP/1.1".equals(protocol)
                || org.apache.coyote.http11.Http11NioProtocol.class.getName().equals(protocol)) {
            return new org.apache.coyote.http11.Http11NioProtocol();
        } else if ("AJP/1.3".equals(protocol)
                || org.apache.coyote.ajp.AjpNioProtocol.class.getName().equals(protocol)) {
            return new org.apache.coyote.ajp.AjpNioProtocol();
        } else {
            // Instantiate protocol handler
            Class<?> clazz = Class.forName(protocol);
            return (ProtocolHandler) clazz.getConstructor().newInstance();
        }
    }

    /**
     * The default behavior is to identify connectors uniquely with address
     * and port. However, certain connectors are not using that and need
     * some other identifier, which then can be used as a replacement.
     * @return the id
     */
    public default String getId() {
        return null;
    }

    /**
     * Initialise the protocol.
     *
     * @throws Exception If the protocol handler fails to initialise
     */
    public void init() throws Exception;

}
