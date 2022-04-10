package org.apache.catalina.startup;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.util.Introspection;
import org.apache.tomcat.util.descriptor.web.FilterDef;

public class WebAnnotationSet {

    // ---------------------------------------------------------- Public Methods

    /**
     * Process the annotations on a context.
     *
     * @param context The context which will have its annotations processed
     */
    public static void loadApplicationAnnotations(Context context) {
        loadApplicationListenerAnnotations(context);
        loadApplicationFilterAnnotations(context);
        loadApplicationServletAnnotations(context);
    }


    // ------------------------------------------------------- Protected Methods

    /**
     * Process the annotations for the listeners.
     *
     * @param context The context which will have its annotations processed
     */
    protected static void loadApplicationListenerAnnotations(Context context) {
        String[] applicationListeners = context.findApplicationListeners();
        for (String className : applicationListeners) {
            Class<?> clazz = Introspection.loadClass(context, className);
            if (clazz == null) {
                continue;
            }

            loadClassAnnotation(context, clazz);
            loadFieldsAnnotation(context, clazz);
            loadMethodsAnnotation(context, clazz);
        }
    }


    /**
     * Process the annotations for the filters.
     *
     * @param context The context which will have its annotations processed
     */
    protected static void loadApplicationFilterAnnotations(Context context) {
        FilterDef[] filterDefs = context.findFilterDefs();
        for (FilterDef filterDef : filterDefs) {
            Class<?> clazz = Introspection.loadClass(context, filterDef.getFilterClass());
            if (clazz == null) {
                continue;
            }

            loadClassAnnotation(context, clazz);
            loadFieldsAnnotation(context, clazz);
            loadMethodsAnnotation(context, clazz);
        }
    }


    /**
     * Process the annotations for the servlets.
     *
     * @param context The context which will have its annotations processed
     */
    protected static void loadApplicationServletAnnotations(Context context) {

        Container[] children = context.findChildren();
        for (Container child : children) {
            if (child instanceof Wrapper) {

                Wrapper wrapper = (Wrapper) child;
                if (wrapper.getServletClass() == null) {
                    continue;
                }

                Class<?> clazz = Introspection.loadClass(context, wrapper.getServletClass());
                if (clazz == null) {
                    continue;
                }

                loadClassAnnotation(context, clazz);
                loadFieldsAnnotation(context, clazz);
                loadMethodsAnnotation(context, clazz);

                /* Process RunAs annotation which can be only on servlets.
                 * Ref JSR 250, equivalent to the run-as element in
                 * the deployment descriptor
                 */
                RunAs runAs = clazz.getAnnotation(RunAs.class);
                if (runAs != null) {
                    wrapper.setRunAs(runAs.value());
                }

                // Process ServletSecurity annotation
                ServletSecurity servletSecurity = clazz.getAnnotation(ServletSecurity.class);
                if (servletSecurity != null) {
                    context.addServletSecurity(
                            new ApplicationServletRegistration(wrapper, context),
                            new ServletSecurityElement(servletSecurity));
                }
            }
        }
    }


}
