package org.apache.tomcat.util.modeler;

import javax.management.MBeanParameterInfo;

public class ParameterInfo  extends FeatureInfo  {

    /**
     * Create and return a <code>MBeanParameterInfo</code> object that
     * corresponds to the parameter described by this instance.
     * @return a parameter info
     */
    public MBeanParameterInfo createParameterInfo() {

        // Return our cached information (if any)
        if (info == null) {
            info = new MBeanParameterInfo
                    (getName(), getType(), getDescription());
        }
        return (MBeanParameterInfo)info;
    }
}
