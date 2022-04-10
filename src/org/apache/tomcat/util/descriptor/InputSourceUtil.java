package org.apache.tomcat.util.descriptor;

import org.apache.tomcat.util.ExceptionUtils;
import org.xml.sax.InputSource;

import java.io.InputStream;

public final class InputSourceUtil {

    public static void close(InputSource inputSource) {
        if (inputSource == null) {
            // Nothing to do
            return;
        }

        InputStream is = inputSource.getByteStream();
        if (is != null) {
            try {
                is.close();
            } catch (Throwable t) {
                ExceptionUtils.handleThrowable(t);
            }
        }

    }

}
