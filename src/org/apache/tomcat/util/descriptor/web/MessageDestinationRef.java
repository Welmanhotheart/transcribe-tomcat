package org.apache.tomcat.util.descriptor.web;

public class MessageDestinationRef extends ResourceBase{

    /**
     * The usage of this destination ref.
     */
    private String usage = null;

    public String getUsage() {
        return this.usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    /**
     * The link of this destination ref.
     */
    private String link = null;

    public String getLink() {
        return this.link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
