package org.apache.catalina.mbeans;

public class MBeanFactory {

    /**
     * The container (Server/Service) for which this factory was created.
     */
    private Object container;


    /**
     * Set the container that this factory was created for.
     * @param container The associated container
     */
    public void setContainer(Object container) {
        this.container = container;
    }


}
