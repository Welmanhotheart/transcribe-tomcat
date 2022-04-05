package org.apache.tomcat.util.modeler;

import javax.management.MBeanFeatureInfo;
import java.io.Serializable;

public class FeatureInfo implements Serializable {
    private static final long serialVersionUID = -911529176124712296L;
    protected String name = null;

    // all have type except Constructor
    protected String type = null;
    protected MBeanFeatureInfo info = null;
    protected String description = null;



    /**
     * @return the name of this feature, which must be unique among features
     *  in the same collection.
     */
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the fully qualified Java class name of this element.
     */
    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    // ------------------------------------------------------------- Properties

    /**
     * @return the human-readable description of this feature.
     */
    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
