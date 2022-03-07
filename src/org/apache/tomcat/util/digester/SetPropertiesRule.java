package org.apache.tomcat.util.digester;

import java.util.HashMap;

public class SetPropertiesRule extends Rule{
    protected final HashMap<String,String> excludes;

    public SetPropertiesRule() {
        excludes = null;
    }

    public SetPropertiesRule(String[] exclude) {
        excludes = new HashMap<>();
        for (String s : exclude) {
            if (s != null) {
                this.excludes.put(s, s);
            }
        }
    }
}
