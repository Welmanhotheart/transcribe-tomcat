package org.apache.coyote;

import java.lang.reflect.InvocationTargetException;

public interface ProtocolHandler {

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

}
