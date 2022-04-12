package jakarta.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @since Common Annotations 1.0
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Resource {

    /**
     * @return a String with the name of the resource
     */
    public String name() default "";

    /**
     * @return a string with the mappedName of the resource
     */
    public String mappedName() default "";

    /**
     * Uses generics since Common Annotations 1.2.
     *
     * @return The type for instances of this resource
     */
    public Class<?> type() default Object.class;


    /**
     * @return a string with the description for the resource
     */
    public String description() default "";

    /**
     * @since Common Annotations 1.1
     *
     * @return The name of the entry, if any, to use for this resource
     */
    public String lookup() default "";


    /**
     * @return the AuthenticationType of the resource default CONTAINER
     */
    public AuthenticationType authenticationType() default AuthenticationType.CONTAINER;

    /**
     * @return true (default) if the resource is shareable, or false if not
     */
    public boolean shareable() default true;

    /**
     * The AuthenticationType, either CONTAINER or APPLICATION
     */
    public enum AuthenticationType {
        /**
         * Container authentication
         */
        CONTAINER,
        /**
         * Application authentication
         */
        APPLICATION
    }

}
