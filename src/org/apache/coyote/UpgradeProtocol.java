package org.apache.coyote;

import org.apache.coyote.http11.AbstractHttp11Protocol;

public interface UpgradeProtocol {
    /**
     * @param isSSLEnabled Is this for a connector that is configured to support
     *                     TLS. Some protocols (e.g. HTTP/2) only support HTTP
     *                     upgrade over non-secure connections.
     * @return The name that clients will use to request an upgrade to this
     *         protocol via an HTTP/1.1 upgrade request or <code>null</code> if
     *         upgrade via an HTTP/1.1 upgrade request is not supported.
     */
    public String getHttpUpgradeName(boolean isSSLEnabled);

    /**
     * Configure the HTTP/1.1 protocol that this UpgradeProcotol is nested
     * under. Connections passed to this UpgradeProtocol via HTTP upgrade will
     * have been initially handled by this HTTP/1.1 protocol implementation.
     * <p>
     * The default implementation is a NO-OP.
     *
     * @param protocol The HTTP/1.1 protocol implementation that will initially
     *                 handle any connections passed to this UpgradeProtocol via
     *                 the HTTP upgrade mechanism
     */
    public default void setHttp11Protocol(AbstractHttp11Protocol<?> protocol) {
        // NO-OP
    }

    /**
     * @return The name of the protocol as listed in the IANA registry if and
     *         only if {@link #getAlpnIdentifier()} returns the UTF-8 encoding
     *         of this name. If {@link #getAlpnIdentifier()} returns some other
     *         byte sequence, then this method returns the empty string. If
     *         upgrade via ALPN is not supported then <code>null</code> is
     *         returned.
     */
    /*
     * Implementation note: If Tomcat ever supports ALPN for a protocol where
     *                      the identifier is not the UTF-8 encoding of the name
     *                      then some refactoring is going to be required.
     *
     * Implementation note: Tomcat assumes that the UTF-8 encoding of this name
     *                      will not exceed 255 bytes. Tomcat's behaviour if
     *                      longer names are used is undefined.
     */
    public String getAlpnName();


}
