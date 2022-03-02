package org.apache.catalina.startup;

import org.apache.tomcat.util.file.ConfigurationSource;

import java.io.File;
import java.io.IOException;

public class CatalinaBaseConfigurationSource implements ConfigurationSource {
    public CatalinaBaseConfigurationSource(File catalinaBaseFile, String configFile) {

    }

    @Override
    public Resource getResource(String fullName) throws IOException {
        return null;
    }
}
