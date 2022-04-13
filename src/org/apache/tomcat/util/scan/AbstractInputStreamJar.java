package org.apache.tomcat.util.scan;

import org.apache.tomcat.Jar;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.jar.JarEntry;

public abstract class AbstractInputStreamJar implements Jar {
    private final URL jarFileURL;

    private NonClosingJarInputStream jarInputStream = null;
    private JarEntry entry = null;
    private Boolean multiRelease = null;
    private Map<String,String> mrMap = null;

    public AbstractInputStreamJar(URL jarFileUrl) {
        this.jarFileURL = jarFileUrl;
    }


    protected abstract NonClosingJarInputStream createJarInputStream() throws IOException;


    @Override
    public URL getJarFileURL() {
        return jarFileURL;
    }

    protected void closeStream() {
        if (jarInputStream != null) {
            try {
                jarInputStream.reallyClose();
            } catch (IOException ioe) {
                // Ignore
            }
        }
    }

}
