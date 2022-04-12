package jakarta.servlet;

public interface Registration {

    /**
     * Interface through which a Servlet or Filter registered via one of the
     * addServlet or addFilter methods, respectively, on ServletContext may be
     * further configured.
     */
    public interface Dynamic extends Registration {

        /**
         * Mark this Servlet/Filter as supported asynchronous processing.
         *
         * @param isAsyncSupported  Should this Servlet/Filter support
         *                          asynchronous processing
         *
         * @throws IllegalStateException if the ServletContext associated with
         *         this registration has already been initialised
         */
        public void setAsyncSupported(boolean isAsyncSupported);
    }

}
