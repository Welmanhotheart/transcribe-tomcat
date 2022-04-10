package org.apache.tomcat.util.digester;

public interface DocumentProperties {
    /**
     * The character encoding used by the source XML document.
     */
    public interface Charset {
        public void setCharset(java.nio.charset.Charset charset);
    }
}
