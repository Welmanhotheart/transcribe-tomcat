package org.apache.catalina;

public interface Container extends Lifecycle{

    public void setParentClassLoader(ClassLoader parent);
}
