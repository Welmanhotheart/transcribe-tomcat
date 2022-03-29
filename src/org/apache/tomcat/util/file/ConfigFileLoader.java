package org.apache.tomcat.util.file;

import org.apache.catalina.startup.CatalinaBaseConfigurationSource;

public class ConfigFileLoader {
    private static ConfigurationSource source;

    public static void setSource(CatalinaBaseConfigurationSource source) {
        if (ConfigFileLoader.source == null) {
            ConfigFileLoader.source = source;
        }
    }

    public static ConfigurationSource getSource() {
        if (ConfigFileLoader.source == null) {
            return ConfigurationSource.DEFAULT;
        }
        return source;
    }

    private ConfigFileLoader() {
        // Hide the constructor,why here need to hide the constructor
    }
}
