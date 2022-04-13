package org.apache.tomcat;

import java.net.URL;

public interface Jar extends AutoCloseable{
    /**
     * Close any resources associated with this JAR.
     */
    void close();

    /**
     * @return The URL for accessing the JAR file.
     */
    URL getJarFileURL();

}
