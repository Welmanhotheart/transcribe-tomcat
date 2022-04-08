package org.apache.catalina;

public interface WebResourceRoot extends Lifecycle{

    /**
     * @return the web application this WebResourceRoot is associated with.
     */
    Context getContext();

    /**
     * Set the web application this WebResourceRoot is associated with.
     *
     * @param context the associated context
     */
    void setContext(Context context);


    /**
     * This method will be invoked by the context on a periodic basis and allows
     * the implementation a method that executes periodic tasks, such as purging
     * expired cache entries.
     */
    void backgroundProcess();

}
