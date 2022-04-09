package org.apache.catalina;

public interface CredentialHandler {
    /**
     * Checks to see if the input credentials match the stored credentials
     *
     * @param inputCredentials  User provided credentials
     * @param storedCredentials Credentials stored in the {@link Realm}
     *
     * @return <code>true</code> if the inputCredentials match the
     *         storedCredentials, otherwise <code>false</code>
     */
    boolean matches(String inputCredentials, String storedCredentials);

    /**
     * Generates the equivalent stored credentials for the given input
     * credentials.
     *
     * @param inputCredentials  User provided credentials
     *
     * @return  The equivalent stored credentials for the given input
     *          credentials
     */
    String mutate(String inputCredentials);
}
