package org.apache.catalina;

public interface Loader {


    /**
     * @return the Java class loader to be used by this Container.
     */
    public ClassLoader getClassLoader();


}
