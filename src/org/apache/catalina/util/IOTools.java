package org.apache.catalina.util;

import java.io.*;

public class IOTools {
    protected static final int DEFAULT_BUFFER_SIZE=4*1024; //4k



    private IOTools() {
        //Ensure non-instantiability
    }

    /**
     * Read input from reader and write it to writer until there is no more
     * input from reader.
     *
     * @param reader the reader to read from.
     * @param writer the writer to write to.
     * @param buf the char array to use as a buffer
     * @throws IOException IO error
     */
    public static void flow(Reader reader, Writer writer, char[] buf )
            throws IOException {
        int numRead;
        while ( (numRead = reader.read(buf) ) >= 0) {
            writer.write(buf, 0, numRead);
        }
    }

    /**
     * Read input from reader and write it to writer until there is no more
     * input from reader.
     *
     * @param reader the reader to read from.
     * @param writer the writer to write to.
     * @throws IOException IO error
     * @see #flow( Reader, Writer, char[] )
     */
    public static void flow( Reader reader, Writer writer )
            throws IOException {
        char[] buf = new char[DEFAULT_BUFFER_SIZE];
        flow( reader, writer, buf );
    }


    /**
     * Read input from input stream and write it to output stream until there is
     * no more input from input stream using a new buffer of the default size
     * (4kB).
     *
     * @param is input stream the input stream to read from.
     * @param os output stream the output stream to write to.
     *
     * @throws IOException If an I/O error occurs during the copy
     */
    public static void flow(InputStream is, OutputStream os) throws IOException {
        byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
        int numRead;
        while ( (numRead = is.read(buf) ) >= 0) {
            if (os != null) {
                os.write(buf, 0, numRead);
            }
        }
    }

}
