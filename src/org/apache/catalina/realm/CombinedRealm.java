package org.apache.catalina.realm;

import org.apache.catalina.Realm;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;

import java.util.LinkedList;
import java.util.List;

public class CombinedRealm extends RealmBase{
    private static final Log log = LogFactory.getLog(CombinedRealm.class);

    /**
     * The string manager for this package.
     */
    protected static final StringManager sm = StringManager.getManager(RealmBase.class);

    /**
     * The list of Realms contained by this Realm.
     */
    protected final List<Realm> realms = new LinkedList<>();


    /**
     * Add a realm to the list of realms that will be used to authenticate
     * users.
     * @param theRealm realm which should be wrapped by the combined realm
     */
    public void addRealm(Realm theRealm) {
        realms.add(theRealm);

        if (log.isDebugEnabled()) {
            sm.getString("combinedRealm.addRealm",
                    theRealm.getClass().getName(),
                    Integer.toString(realms.size()));
        }
    }

}
