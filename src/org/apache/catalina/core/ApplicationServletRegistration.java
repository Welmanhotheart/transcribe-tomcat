package org.apache.catalina.core;

import jakarta.servlet.ServletRegistration;
import jakarta.servlet.ServletSecurityElement;
import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.tomcat.util.res.StringManager;

public class ApplicationServletRegistration implements ServletRegistration.Dynamic{

    /**
     * The string manager for this package.
     */
    private static final StringManager sm = StringManager.getManager(ApplicationServletRegistration.class);

    private final Wrapper wrapper;
    private final Context context;
    private ServletSecurityElement constraint;

    public ApplicationServletRegistration(Wrapper wrapper,
                                          Context context) {
        this.wrapper = wrapper;
        this.context = context;

    }

    @Override
    public void setAsyncSupported(boolean isAsyncSupported) {

    }
}
