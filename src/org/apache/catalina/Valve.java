package org.apache.catalina;

public interface Valve {



    //-------------------------------------------------------------- Properties

    /**
     * @return the next Valve in the pipeline containing this Valve, if any.
     */
    public Valve getNext();


    /**
     * Set the next Valve in the pipeline containing this Valve.
     *
     * @param valve The new next valve, or <code>null</code> if none
     */
    public void setNext(Valve valve);

}
