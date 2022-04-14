package org.apache.tomcat.util.descriptor.web;

import jakarta.servlet.descriptor.JspPropertyGroupDescriptor;

public class JspPropertyGroupDescriptorImpl implements JspPropertyGroupDescriptor {

    private final JspPropertyGroup jspPropertyGroup;


    public JspPropertyGroupDescriptorImpl(
            JspPropertyGroup jspPropertyGroup) {
        this.jspPropertyGroup = jspPropertyGroup;
    }

}
