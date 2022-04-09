package org.apache.catalina.realm;

import org.apache.catalina.LifecycleException;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class LockOutRealm extends CombinedRealm{

    private static final Log log = LogFactory.getLog(LockOutRealm.class);


    /**
     * Number of users that have failed authentication to keep in cache. Over
     * time the cache will grow to this size and may not shrink. Defaults to
     * 1000.
     */
    protected int cacheSize = 1000;

    /**
     * If a failed user is removed from the cache because the cache is too big
     * before it has been in the cache for at least this period of time (in
     * seconds) a warning message will be logged. Defaults to 3600 (1 hour).
     */
    protected int cacheRemovalWarningTime = 3600;

    /**
     * Users whose last authentication attempt failed. Entries will be ordered
     * in access order from least recent to most recent.
     */
    protected Map<String,LockRecord> failedUsers = null;

    /**
     * Prepare for the beginning of active use of the public methods of this
     * component and implement the requirements of
     * {@link org.apache.catalina.util.LifecycleBase#startInternal()}.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
    @Override
    protected synchronized void startInternal() throws LifecycleException {
        // Configure the list of failed users to delete the oldest entry once it
        // exceeds the specified size
        failedUsers = new LinkedHashMap<>(cacheSize, 0.75f,
                true) {
            private static final long serialVersionUID = 1L;
            @Override
            protected boolean removeEldestEntry(
                    Map.Entry<String, LockRecord> eldest) {
                if (size() > cacheSize) {
                    // Check to see if this element has been removed too quickly
                    long timeInCache = (System.currentTimeMillis() -
                            eldest.getValue().getLastFailureTime())/1000;

                    if (timeInCache < cacheRemovalWarningTime) {
                        log.warn(sm.getString("lockOutRealm.removeWarning",
                                eldest.getKey(), Long.valueOf(timeInCache)));
                    }
                    return true;
                }
                return false;
            }
        };

        super.startInternal();
    }

    protected static class LockRecord {
        private final AtomicInteger failures = new AtomicInteger(0);
        private long lastFailureTime = 0;

        public int getFailures() {
            return failures.get();
        }

        public void setFailures(int theFailures) {
            failures.set(theFailures);
        }

        public long getLastFailureTime() {
            return lastFailureTime;
        }

        public void registerFailure() {
            failures.incrementAndGet();
            lastFailureTime = System.currentTimeMillis();
        }
    }

}
