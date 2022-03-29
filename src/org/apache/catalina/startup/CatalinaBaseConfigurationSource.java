package org.apache.catalina.startup;

import org.apache.tomcat.util.buf.UriUtil;
import org.apache.tomcat.util.file.ConfigurationSource;
import org.apache.tomcat.util.res.StringManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class CatalinaBaseConfigurationSource implements ConfigurationSource {

    protected static final StringManager sm = StringManager.getManager(Constants.Package);
    private final String serverXmlPath;
    private final File catalinaBaseFile;
    private final URI catalinaBaseUri;

    public CatalinaBaseConfigurationSource(File catalinaBaseFile, String serverXmlPath) {
        this.catalinaBaseFile = catalinaBaseFile;
        catalinaBaseUri = catalinaBaseFile.toURI();
        this.serverXmlPath = serverXmlPath;
    }

    @Override
    public Resource getResource(String name) throws IOException {
        // Originally only File was supported. Class loader and URI were added
        // later. However (see bug 65106) treating some URIs as files can cause
        // problems. Therefore, if path starts with a valid URI scheme then skip
        // straight to processing this as a URI.
        if (!UriUtil.isAbsoluteURI(name)) {
            File f = new File(name);
            if (!f.isAbsolute()) {
                f = new File(catalinaBaseFile, name);
            }
            if (f.isFile()) {
                FileInputStream fis = new FileInputStream(f);
                return new Resource(fis, f.toURI());
            }

            // Try classloader
            InputStream stream = null;
            try {
                stream = getClass().getClassLoader().getResourceAsStream(name);
                if (stream != null) {
                    return new Resource(stream, getClass().getClassLoader().getResource(name).toURI());
                }
            } catch (URISyntaxException e) {
                stream.close();
                throw new IOException(sm.getString("catalinaConfigurationSource.cannotObtainURL", name), e);
            }
        }

        // Then try URI.
        URI uri = null;
        try {
            uri = getURIInternal(name);
        } catch (IllegalArgumentException e) {
            throw new IOException(sm.getString("catalinaConfigurationSource.cannotObtainURL", name));
        }

        // Obtain the input stream we need
        try {
            URL url = uri.toURL();
            return new Resource(url.openConnection().getInputStream(), uri);
        } catch (MalformedURLException e) {
            throw new IOException(sm.getString("catalinaConfigurationSource.cannotObtainURL", name), e);
        }
    }


    private URI getURIInternal(String name) {
        // Then try URI.
        // Using resolve() enables the code to handle relative paths that did
        // not point to a file
        URI uri;
        if (catalinaBaseUri != null) {
            uri = catalinaBaseUri.resolve(name);
        } else {
            uri = URI.create(name);
        }
        return uri;
    }
}
