package org.apache.tomcat.util.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public interface ConfigurationSource {
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
