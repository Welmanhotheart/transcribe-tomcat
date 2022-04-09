package org.apache.catalina.realm;

import org.apache.catalina.CredentialHandler;

public class DigestCredentialHandlerBase  implements CredentialHandler {
    @Override
    public boolean matches(String inputCredentials, String storedCredentials) {
        return false;
    }

    @Override
    public String mutate(String inputCredentials) {
        return null;
    }
}
