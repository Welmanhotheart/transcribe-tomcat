package org.apache.tomcat.util.net;

import org.apache.tomcat.util.net.openssl.ciphers.Authentication;

import javax.management.ObjectName;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SSLHostConfigCertificate {


    // OpenSSL can handle multiple certs in a single config so the reference to
    // the context is at the virtual host level. JSSE can't so the reference is
    // held here on the certificate.
    private transient SSLContext sslContext;


    // Common
    private final SSLHostConfig sslHostConfig;
    private final Type type;
    // Internal
    private ObjectName oname;
    public SSLHostConfigCertificate() {
        this(null, Type.UNDEFINED);
    }

    public SSLHostConfigCertificate(SSLHostConfig sslHostConfig, Type type) {
        this.sslHostConfig = sslHostConfig;
        this.type = type;
    }

    public SSLContext getSslContext() {
        return sslContext;
    }

    // Common

    public Type getType() {
        return type;
    }

    // Internal

    public ObjectName getObjectName() {
        return oname;
    }


    public void setObjectName(ObjectName oname) {
        this.oname = oname;
    }


    // Nested types

    public enum Type {

        UNDEFINED,
        RSA(Authentication.RSA),
        DSA(Authentication.DSS),
        EC(Authentication.ECDH, Authentication.ECDSA);

        private final Set<Authentication> compatibleAuthentications;

        private Type(Authentication... authentications) {
            compatibleAuthentications = new HashSet<>();
            if (authentications != null) {
                compatibleAuthentications.addAll(Arrays.asList(authentications));
            }
        }

        public boolean isCompatibleWith(Authentication au) {
            return compatibleAuthentications.contains(au);
        }
    }

    enum StoreType {
        KEYSTORE,
        PEM
    }
}
