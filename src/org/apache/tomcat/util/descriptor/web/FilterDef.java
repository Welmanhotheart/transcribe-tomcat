package org.apache.tomcat.util.descriptor.web;

import java.io.Serializable;

public class FilterDef  implements Serializable {

    /**
     * The fully qualified name of the Java class that implements this filter.
     */
    private String filterClass = null;

    public String getFilterClass() {
        return this.filterClass;
    }

    public void setFilterClass(String filterClass) {
        this.filterClass = filterClass;
    }


}
