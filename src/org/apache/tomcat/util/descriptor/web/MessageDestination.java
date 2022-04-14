package org.apache.tomcat.util.descriptor.web;

public class MessageDestination extends ResourceBase{
    /**
     * The display name of this destination.
     */
    private String displayName = null;

    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
