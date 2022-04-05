package org.apache.tomcat.jni;

public final class SSL {
    /**
     * Get the status of FIPS Mode.
     *
     * @return FIPS_mode return code. It is <code>0</code> if OpenSSL is not
     *  in FIPS mode, <code>1</code> if OpenSSL is in FIPS Mode.
     * @throws Exception If tcnative was not compiled with FIPS Mode available.
     * @see <a href="http://wiki.openssl.org/index.php/FIPS_mode%28%29">OpenSSL method FIPS_mode()</a>
     */
    public static native int fipsModeGet() throws Exception;

    /**
     * Enable/Disable FIPS Mode.
     *
     * @param mode 1 - enable, 0 - disable
     *
     * @return FIPS_mode_set return code
     * @throws Exception If tcnative was not compiled with FIPS Mode available,
     *  or if {@code FIPS_mode_set()} call returned an error value.
     * @see <a href="http://wiki.openssl.org/index.php/FIPS_mode_set%28%29">OpenSSL method FIPS_mode_set()</a>
     */
    public static native int fipsModeSet(int mode) throws Exception;

    /* Return OpenSSL version string (run time version) */
    public static native String versionString();

}
