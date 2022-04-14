package org.apache.tomcat.util.compat;

import org.apache.tomcat.util.res.StringManager;

import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;

public class JreCompat {
    private static final JreCompat instance;
    private static final boolean graalAvailable;
    private static final boolean jre16Available;
    private static final StringManager sm = StringManager.getManager(JreCompat.class);

    static {
        boolean result = false;
        try {
            Class<?> nativeImageClazz = Class.forName("org.graalvm.nativeimage.ImageInfo");
            result = Boolean.TRUE.equals(nativeImageClazz.getMethod("inImageCode").invoke(null));
        } catch (ClassNotFoundException e) {
            // Must be Graal
        } catch (ReflectiveOperationException | IllegalArgumentException e) {
            // Should never happen
        }
        graalAvailable = result || System.getProperty("org.graalvm.nativeimage.imagecode") != null;

        // This is Tomcat 10.1.x with a minimum Java version of Java 11.
        // Look for the highest supported JVM first
        if (Jre16Compat.isSupported()) {
            instance = new Jre16Compat();
            jre16Available = true;
        } else {
            instance = new JreCompat();
            jre16Available = false;
        }
    }

    // Java 11 implementations of Java 16 methods

    /**
     * Return Unix domain socket address for given path.
     * @param path The path
     * @return the socket address
     */
    public SocketAddress getUnixDomainSocketAddress(String path) {
        return null;
    }

    /**
     * Create server socket channel using the Unix domain socket ProtocolFamily.
     * @return the server socket channel
     */
    public ServerSocketChannel openUnixDomainServerSocketChannel() {
        throw new UnsupportedOperationException(sm.getString("jreCompat.noUnixDomainSocket"));
    }

    public static JreCompat getInstance() {
        return instance;
    }

    public static boolean isGraalAvailable() {
        return graalAvailable;
    }

}
