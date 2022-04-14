package org.apache.tomcat;

import java.io.IOException;
import java.io.InputStream;
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

    /**
     * Resets the internal pointer used to track JAR entries to the beginning of
     * the JAR.
     *
     * @throws IOException  If the pointer cannot be reset
     */
    void reset() throws IOException;

    /**
     * Moves the internal pointer to the next entry in the JAR.
     */
    void nextEntry();

    /**
     * Obtains the name of the current entry.
     *
     * @return  The entry name
     */
    String getEntryName();

    /**
     * Obtains the input stream for the current entry.
     *
     * @return  The input stream
     * @throws IOException  If the stream cannot be obtained
     */
    InputStream getEntryInputStream() throws IOException;

}
