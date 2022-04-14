package org.apache.tomcat.util.descriptor.web;

import java.io.Serializable;

public class MultipartDef implements Serializable {

    private static final long serialVersionUID = 1L;

    // ------------------------------------------------------------- Properties
    private String location;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    private String fileSizeThreshold;

    public String getFileSizeThreshold() {
        return fileSizeThreshold;
    }

    public void setFileSizeThreshold(String fileSizeThreshold) {
        this.fileSizeThreshold = fileSizeThreshold;
    }


    private String maxFileSize;

    public String getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(String maxFileSize) {
        this.maxFileSize = maxFileSize;
    }


    private String maxRequestSize;

    public String getMaxRequestSize() {
        return maxRequestSize;
    }

    public void setMaxRequestSize(String maxRequestSize) {
        this.maxRequestSize = maxRequestSize;
    }


}
