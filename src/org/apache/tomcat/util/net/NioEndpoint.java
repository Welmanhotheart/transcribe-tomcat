package org.apache.tomcat.util.net;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.compat.JreCompat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.Channel;
import java.nio.channels.NetworkChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class NioEndpoint extends AbstractJsseEndpoint<NioChannel, SocketChannel>{


    // -------------------------------------------------------------- Constants


    private static final Log log = LogFactory.getLog(NioEndpoint.class);


    /**
     * Stop latch used to wait for poller stop
     */
    private volatile CountDownLatch stopLatch = null;

    /**
     * Server socket "pointer".
     */
    private volatile ServerSocketChannel serverSock = null;


    protected CountDownLatch getStopLatch() {
        return stopLatch;
    }


    protected void setStopLatch(CountDownLatch stopLatch) {
        this.stopLatch = stopLatch;
    }

    @Override
    protected NetworkChannel getServerSocket() {
        return serverSock;
    }

    /**
     * Use System.inheritableChannel to obtain channel from stdin/stdout.
     */
    private boolean useInheritedChannel = false;
    public void setUseInheritedChannel(boolean useInheritedChannel) { this.useInheritedChannel = useInheritedChannel; }
    public boolean getUseInheritedChannel() { return useInheritedChannel; }

    /**
     * Path for the Unix domain socket, used to create the socket address.
     */
    private String unixDomainSocketPath = null;
    public String getUnixDomainSocketPath() { return this.unixDomainSocketPath; }
    public void setUnixDomainSocketPath(String unixDomainSocketPath) {
        this.unixDomainSocketPath = unixDomainSocketPath;
    }

    /**
     * Initialize the endpoint.
     */
    @Override
    public void bind() throws Exception {
        initServerSocket();

        setStopLatch(new CountDownLatch(1));

        // Initialize SSL if needed
        initialiseSsl();
    }


    /**
     * Permissions which will be set on the Unix domain socket if it is created.
     */
    private String unixDomainSocketPathPermissions = null;
    public String getUnixDomainSocketPathPermissions() { return this.unixDomainSocketPathPermissions; }
    public void setUnixDomainSocketPathPermissions(String unixDomainSocketPathPermissions) {
        this.unixDomainSocketPathPermissions = unixDomainSocketPathPermissions;
    }

    // Separated out to make it easier for folks that extend NioEndpoint to
    // implement custom [server]sockets
    protected void initServerSocket() throws Exception {
        if (getUseInheritedChannel()) {
            // Retrieve the channel provided by the OS
            Channel ic = System.inheritedChannel();
            if (ic instanceof ServerSocketChannel) {
                serverSock = (ServerSocketChannel) ic;
            }
            if (serverSock == null) {
                throw new IllegalArgumentException(sm.getString("endpoint.init.bind.inherited"));
            }
        } else if (getUnixDomainSocketPath() != null) {
            SocketAddress sa = JreCompat.getInstance().getUnixDomainSocketAddress(getUnixDomainSocketPath());
            serverSock = JreCompat.getInstance().openUnixDomainServerSocketChannel();
            serverSock.bind(sa, getAcceptCount());
            if (getUnixDomainSocketPathPermissions() != null) {
                Path path = Paths.get(getUnixDomainSocketPath());
                Set<PosixFilePermission> permissions =
                        PosixFilePermissions.fromString(getUnixDomainSocketPathPermissions());
                if (path.getFileSystem().supportedFileAttributeViews().contains("posix")) {
                    FileAttribute<Set<PosixFilePermission>> attrs = PosixFilePermissions.asFileAttribute(permissions);
                    Files.setAttribute(path, attrs.name(), attrs.value());
                } else {
                    java.io.File file = path.toFile();
                    if (permissions.contains(PosixFilePermission.OTHERS_READ) && !file.setReadable(true, false)) {
                        log.warn(sm.getString("endpoint.nio.perms.readFail", file.getPath()));
                    }
                    if (permissions.contains(PosixFilePermission.OTHERS_WRITE) && !file.setWritable(true, false)) {
                        log.warn(sm.getString("endpoint.nio.perms.writeFail", file.getPath()));
                    }
                }
            }
        } else {
            serverSock = ServerSocketChannel.open();
            socketProperties.setProperties(serverSock.socket());
            InetSocketAddress addr = new InetSocketAddress(getAddress(), getPortWithOffset());
            serverSock.bind(addr, getAcceptCount());
        }
        serverSock.configureBlocking(true); //mimic APR behavior
    }


    @Override
    public void unbind() throws Exception {

    }

    @Override
    protected void createSSLContext(SSLHostConfig sslHostConfig) throws Exception {

    }

    @Override
    protected Log getLog() {
        return null;
    }
}
