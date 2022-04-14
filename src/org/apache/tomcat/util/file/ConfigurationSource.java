package org.apache.tomcat.util.file;

import org.apache.tomcat.util.buf.UriUtil;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public interface ConfigurationSource {

    /**
     * Returns the contents of the shared conf/web.xml file. This usually
     * contains the declaration of the default and JSP servlets.
     * @return the web.xml as an InputStream
     * @throws IOException if an error occurs or if the resource does not exist
     */
    public default Resource getSharedWebXml()
            throws IOException {
        return getConfResource("web.xml");
    }

    public static final ConfigurationSource DEFAULT = new ConfigurationSource() {
        protected final File userDir = new File(System.getProperty("user.dir"));
        protected final URI userDirUri = userDir.toURI();
        @Override //TODO, what here is used
        public Resource getResource(String name) throws IOException {
            if (!UriUtil.isAbsoluteURI(name)) {
                File f = new File(name);
                if (!f.isAbsolute()) {
                    f = new File(userDir, name);
                }
                if (f.isFile()) {
                    FileInputStream fis = new FileInputStream(f);
                    return new Resource(fis, f.toURI());
                }
            }
            URI uri = null;
            try {
                uri = userDirUri.resolve(name);
            } catch (IllegalArgumentException e) {
                throw new FileNotFoundException(name);
            }
            try {
                URL url = uri.toURL();
                return new Resource(url.openConnection().getInputStream(),uri);
            } catch (MalformedURLException e) {
                throw  new FileNotFoundException(name);
            }
        }
    };

    default Resource getServerXml() throws IOException {
        return getConfResource("server.xml");
    }

    default Resource getConfResource(String name) throws IOException {
        String fullName = "conf/" + name;
        return getResource(fullName);
    }

    Resource getResource(String fullName) throws IOException;


    public class Resource implements AutoCloseable{
        private InputStream inputStream;
        private final URI uri;
        //TODO, why we need uri?
        public Resource(InputStream fis, URI toURI) {
            this.inputStream = fis;
            this.uri = toURI;
        }

        @Override
        public void close() throws IOException {

        }

        public InputStream getInputStream() {
            return inputStream;
        }

        public URI getURI() {
            return uri;
        }
    }
}
