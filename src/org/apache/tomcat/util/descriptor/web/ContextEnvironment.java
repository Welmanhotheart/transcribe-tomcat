package org.apache.tomcat.util.descriptor.web;

public class ContextEnvironment  extends ResourceBase{


    /**
     * Does this environment entry allow overrides by the application
     * deployment descriptor?
     */
    private boolean override = true;

    public boolean getOverride() {
        return this.override;
    }

    public void setOverride(boolean override) {
        this.override = override;
    }

    /**
     * The value of this environment entry.
     */
    private String value = null;

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }


}
