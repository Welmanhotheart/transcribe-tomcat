package org.apache.tomcat.util.descriptor.web;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public abstract class XmlEncodingBase {
    private Charset charset = StandardCharsets.UTF_8;


    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    /**
     * Obtain the character encoding of the XML source that was used to
     * populated this object.
     *
     * @return The character encoding of the associated XML source or
     *         <code>UTF-8</code> if the encoding could not be determined
     */
    public Charset getCharset() {
        return charset;
    }

}
