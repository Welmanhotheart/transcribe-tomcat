package org.apache.catalina;

public interface Realm extends Contained{

    /**
     * Execute a periodic task, such as reloading, etc. This method will be
     * invoked inside the classloading context of this container. Unexpected
     * throwables will be caught and logged.
     */
    public void backgroundProcess();

    /**
     * @return the CredentialHandler configured for this Realm.
     */
    public CredentialHandler getCredentialHandler();
}
