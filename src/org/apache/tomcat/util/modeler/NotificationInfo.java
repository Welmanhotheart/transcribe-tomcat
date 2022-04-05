package org.apache.tomcat.util.modeler;

import javax.management.MBeanNotificationInfo;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class NotificationInfo extends FeatureInfo{

    protected final ReadWriteLock notifTypesLock = new ReentrantReadWriteLock();
    transient MBeanNotificationInfo info = null;
    protected String notifTypes[] = new String[0];


    /**
     * Create and return a <code>ModelMBeanNotificationInfo</code> object that
     * corresponds to the attribute described by this instance.
     * @return the notification info
     */
    public MBeanNotificationInfo createNotificationInfo() {

        // Return our cached information (if any)
        if (info != null) {
            return info;
        }

        // Create and return a new information object
        info = new MBeanNotificationInfo
                (getNotifTypes(), getName(), getDescription());
        //Descriptor descriptor = info.getDescriptor();
        //addFields(descriptor);
        //info.setDescriptor(descriptor);
        return info;

    }

    /**
     * @return the set of notification types for this MBean.
     */
    public String[] getNotifTypes() {
        Lock readLock = notifTypesLock.readLock();
        readLock.lock();
        try {
            return this.notifTypes;
        } finally {
            readLock.unlock();
        }
    }

}
