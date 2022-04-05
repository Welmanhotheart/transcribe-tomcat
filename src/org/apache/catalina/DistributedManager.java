package org.apache.catalina;

public interface DistributedManager {
    /**
     * Returns the total session count for primary, backup and proxy.
     *
     * @return  The total session count across the cluster.
     */
    public int getActiveSessionsFull();

}
