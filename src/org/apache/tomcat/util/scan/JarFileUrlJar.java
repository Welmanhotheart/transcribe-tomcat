package org.apache.tomcat.util.scan;

import org.apache.tomcat.Jar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

public class JarFileUrlJar implements Jar {


    private final JarFile jarFile;
    private final URL jarFileURL;
    private final boolean multiRelease;
    private Enumeration<JarEntry> entries;
    private Set<String> entryNamesSeen;
    private JarEntry entry = null;

    public JarFileUrlJar(URL url, boolean startsWithJar) throws IOException {
        if (startsWithJar) {
            // jar:file:...
            JarURLConnection jarConn = (JarURLConnection) url.openConnection();
            jarConn.setUseCaches(false);
            jarFile = jarConn.getJarFile();
            jarFileURL = jarConn.getJarFileURL();
        } else {
            // file:...
            File f;
            try {
                f = new File(url.toURI());
            } catch (URISyntaxException e) {
                throw new IOException(e);
            }
            jarFile = new JarFile(f, true, ZipFile.OPEN_READ, Runtime.version());
            jarFileURL = url;
        }
        multiRelease = jarFile.isMultiRelease();
    }


    @Override
    public void close() {
        if (jarFile != null) {
            try {
                jarFile.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    @Override
    public URL getJarFileURL() {
        return jarFileURL;
    }


    @Override
    public void reset() throws IOException {
        entries = null;
        entryNamesSeen = null;
        entry = null;
    }

    @Override
    public void nextEntry() {

    }

    @Override
    public String getEntryName() {
        return null;
    }

    @Override
    public InputStream getEntryInputStream() throws IOException {
        return null;
    }
}
