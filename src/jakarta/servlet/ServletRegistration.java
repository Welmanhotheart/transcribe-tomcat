package jakarta.servlet;

public interface ServletRegistration {

    /**
     * Interface through which a Servlet registered via one of the addServlet
     * methods on ServletContext may be further configured.
     */
    public static interface Dynamic extends ServletRegistration, Registration.Dynamic {

    }
}
