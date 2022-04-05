package org.apache.catalina.core;

public class AprStatus {
    private static volatile boolean aprInitialized = false;
    private static volatile boolean aprAvailable = false;
    private static volatile boolean useOpenSSL = true;
    private static volatile boolean instanceCreated = false;


    public static boolean isAprInitialized() {
        return aprInitialized;
    }
    public static void setAprInitialized(boolean aprInitialized) {
        AprStatus.aprInitialized = aprInitialized;
    }

    public static void setAprAvailable(boolean aprAvailable) {
        AprStatus.aprAvailable = aprAvailable;
    }
    public static boolean isAprAvailable() {
        return aprAvailable;
    }
    public static boolean getUseOpenSSL() {
        return useOpenSSL;
    }
}
