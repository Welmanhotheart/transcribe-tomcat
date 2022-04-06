package org.apache.tomcat.util.compat;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;

import java.lang.reflect.Method;
import java.net.ProtocolFamily;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class Jre16Compat extends JreCompat {

    private static final Log log = LogFactory.getLog(Jre16Compat.class);
    private static final StringManager sm = StringManager.getManager(Jre16Compat.class);

    private static final Class<?> unixDomainSocketAddressClazz;
    private static final Method openServerSocketChannelFamilyMethod;
    private static final Method unixDomainSocketAddressOfMethod;
    private static final Method openSocketChannelFamilyMethod;

    static {
        Class<?> c1 = null;
        Method m1 = null;
        Method m2 = null;
        Method m3 = null;
        try {
            c1 = Class.forName("java.net.UnixDomainSocketAddress");
            m1 = ServerSocketChannel.class.getMethod("open", ProtocolFamily.class);
            m2 = c1.getMethod("of", String.class);
            m3 = SocketChannel.class.getMethod("open", ProtocolFamily.class);
        } catch (ClassNotFoundException e) {
            // Must be pre-Java 16
            log.debug(sm.getString("jre16Compat.javaPre16"), e);
        } catch (ReflectiveOperationException | IllegalArgumentException e) {
            // Should never happen
            log.error(sm.getString("jre16Compat.unexpected"), e);
        }
        unixDomainSocketAddressClazz = c1;
        openServerSocketChannelFamilyMethod = m1;
        unixDomainSocketAddressOfMethod = m2;
        openSocketChannelFamilyMethod = m3;
    }

    static boolean isSupported() {
        return unixDomainSocketAddressClazz != null;
    }



}
