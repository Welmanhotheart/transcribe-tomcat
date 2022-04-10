package org.apache.catalina.org.apache.tomcat.util.descriptor.web.org.apache.tomcat.util.buf;

import org.apache.tomcat.util.res.StringManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class UDecoder {
    private static final StringManager sm = StringManager.getManager(UDecoder.class);

    /**
     * Decode and return the specified URL-encoded String. It is assumed the
     * string is not a query string.
     *
     * @param str The url-encoded string
     * @param charset The character encoding to use; if null, UTF-8 is used.
     * @return the decoded string
     * @exception IllegalArgumentException if a '%' character is not followed
     * by a valid 2-digit hexadecimal number
     */
    public static String URLDecode(String str, Charset charset) {
        if (str == null) {
            return null;
        }

        if (str.indexOf('%') == -1) {
            // No %nn sequences, so return string unchanged
            return str;
        }

        if (charset == null) {
            charset = StandardCharsets.UTF_8;
        }

        /*
         * Decoding is required.
         *
         * Potential complications:
         * - The source String may be partially decoded so it is not valid to
         *   assume that the source String is ASCII.
         * - Have to process as characters since there is no guarantee that the
         *   byte sequence for '%' is going to be the same in all character
         *   sets.
         * - We don't know how many '%nn' sequences are required for a single
         *   character. It varies between character sets and some use a variable
         *   length.
         */

        // This isn't perfect but it is a reasonable guess for the size of the
        // array required
        ByteArrayOutputStream baos = new ByteArrayOutputStream(str.length() * 2);

        OutputStreamWriter osw = new OutputStreamWriter(baos, charset);

        char[] sourceChars = str.toCharArray();
        int len = sourceChars.length;
        int ix = 0;

        try {
            while (ix < len) {
                char c = sourceChars[ix++];
                if (c == '%') {
                    osw.flush();
                    if (ix + 2 > len) {
                        throw new IllegalArgumentException(
                                sm.getString("uDecoder.urlDecode.missingDigit", str));
                    }
                    char c1 = sourceChars[ix++];
                    char c2 = sourceChars[ix++];
                    if (isHexDigit(c1) && isHexDigit(c2)) {
                        baos.write(x2c(c1, c2));
                    } else {
                        throw new IllegalArgumentException(
                                sm.getString("uDecoder.urlDecode.missingDigit", str));
                    }
                } else {
                    osw.append(c);
                }
            }
            osw.flush();

            return baos.toString(charset.name());
        } catch (IOException ioe) {
            throw new IllegalArgumentException(
                    sm.getString("uDecoder.urlDecode.conversionError", str, charset.name()), ioe);
        }
    }

    private static int x2c( char b1, char b2 ) {
        int digit= (b1>='A') ? ( (b1 & 0xDF)-'A') + 10 :
                (b1 -'0');
        digit*=16;
        digit +=(b2>='A') ? ( (b2 & 0xDF)-'A') + 10 :
                (b2 -'0');
        return digit;
    }

    private static int x2c( byte b1, byte b2 ) {
        int digit= (b1>='A') ? ( (b1 & 0xDF)-'A') + 10 :
                (b1 -'0');
        digit*=16;
        digit +=(b2>='A') ? ( (b2 & 0xDF)-'A') + 10 :
                (b2 -'0');
        return digit;
    }

    private static boolean isHexDigit( int c ) {
        return ( ( c>='0' && c<='9' ) ||
                ( c>='a' && c<='f' ) ||
                ( c>='A' && c<='F' ));
    }

}
