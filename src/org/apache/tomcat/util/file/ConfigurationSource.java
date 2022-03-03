package org.apache.tomcat.util.file;

import org.apache.tomcat.util.buf.UriUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

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
                    new Resource(fis, f.toURI())
                }
            }
            return null;
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

        public Resource(FileInputStream fis, URI toURI) {

        }

        @Override
        public void close() throws Exception {

        }

        public InputStream getInputStream() {
            return inputStream;
        }

        public URI getURI() {
            return null;
        }
    }
}
