package org.apache.tomcat.util.net;

import org.apache.tomcat.util.net.openssl.ciphers.Authentication;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SSLHostConfigCertificate {

    // Common
    private final SSLHostConfig sslHostConfig;
    private final Type type;

    public SSLHostConfigCertificate() {
        this(null, Type.UNDEFINED);
    }

    public SSLHostConfigCertificate(SSLHostConfig sslHostConfig, Type type) {
        this.sslHostConfig = sslHostConfig;
        this.type = type;
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
