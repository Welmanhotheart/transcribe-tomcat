package org.apache.tomcat.util.file;

import org.apache.catalina.startup.CatalinaBaseConfigurationSource;

public class ConfigFileLoader {
    private static ConfigurationSource source;

    public static void setSource(CatalinaBaseConfigurationSource catalinaBaseConfigurationSource) {

    }

    public static ConfigurationSource getSource() {
        if (ConfigFileLoader.source == null) {
            return ConfigurationSource.DEFAULT;
        }
        return source;
    }
}
