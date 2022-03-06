package org.apache.tomcat.util.file;

import org.apache.tomcat.util.buf.UriUtil;
import org.eclipse.text.edits.MalformedTreeException;

import java.io.*;
import java.net.URI;
import java.net.URL;

public interface ConfigurationSource {
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
            } catch (MalformedTreeException e) {
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
        public void close() throws Exception {

        }

        public InputStream getInputStream() {
            return inputStream;
        }

        public URI getURI() {
            return uri;
        }
    }
}
