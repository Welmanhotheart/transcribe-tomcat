package org.apache.tomcat.util.modeler.modules;

import org.apache.tomcat.util.modeler.Registry;
import org.apache.tomcat.util.res.StringManager;

import javax.management.ObjectName;
import java.util.List;

public abstract class ModelerSource {
    protected static final StringManager sm = StringManager.getManager(Registry.class);
    protected Object source;

    /**
     * Load data, returns a list of items.
     *
     * @param registry The registry
     * @param type The bean registry type
     * @param source Introspected object or some other source
     * @return a list of object names
     * @throws Exception Error loading descriptors
     */
    public abstract List<ObjectName> loadDescriptors(Registry registry,
                                                     String type, Object source) throws Exception;
}
