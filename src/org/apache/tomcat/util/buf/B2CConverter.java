package org.apache.tomcat.util.buf;

import org.apache.tomcat.util.res.StringManager;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Locale;

public class B2CConverter {

    private static final StringManager sm = StringManager.getManager(B2CConverter.class);

    private static final CharsetCache charsetCache = new CharsetCache();


    /**
     * Obtain the Charset for the given encoding
     *
     * @param enc The name of the encoding for the required charset
     *
     * @return The Charset corresponding to the requested encoding
     *
     * @throws UnsupportedEncodingException If the requested Charset is not
     *                                      available
     */
    public static Charset getCharset(String enc) throws UnsupportedEncodingException {

        // Encoding names should all be ASCII
        String lowerCaseEnc = enc.toLowerCase(Locale.ENGLISH);

        Charset charset = charsetCache.getCharset(lowerCaseEnc);

        if (charset == null) {
            // Pre-population of the cache means this must be invalid
            throw new UnsupportedEncodingException(
                    sm.getString("b2cConverter.unknownEncoding", lowerCaseEnc));
        }
        return charset;
    }


}
