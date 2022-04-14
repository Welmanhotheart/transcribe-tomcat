package org.apache.tomcat.util.descriptor.web;

import java.io.Serializable;

public class SecurityCollection extends XmlEncodingBase implements Serializable {


    /**
     * The URL patterns protected by this security collection.
     */
    private String patterns[] = new String[0];

    /**
     * The HTTP methods explicitly covered by this web resource collection.
     */
    private String methods[] = new String[0];

    /**
     * The HTTP methods explicitly excluded from this web resource collection.
     */
    private String omittedMethods[] = new String[0];

    /**
     * @return the set of HTTP request methods that are explicitly excluded from
     * this web resource collection, or a zero-length array if no request
     * methods are excluded.
     */
    public String[] findOmittedMethods() {
        return omittedMethods;
    }

    /**
     * Is the specified pattern part of this web resource collection?
     *
     * @param pattern Pattern to be compared
     * @return <code>true</code> if the pattern is part of the collection
     */
    public boolean findPattern(String pattern) {
        for (String s : patterns) {
            if (s.equals(pattern)) {
                return true;
            }
        }
        return false;
    }


    /**
     * @return the set of URL patterns that are part of this web resource
     * collection.  If none have been specified, a zero-length array is
     * returned.
     */
    public String[] findPatterns() {
        return patterns;
    }

    /**
     * @return the set of HTTP request methods that are part of this web
     * resource collection, or a zero-length array if no methods have been
     * explicitly included.
     */
    public String[] findMethods() {
        return methods;
    }

    // ----------------------------------------------------- Instance Variables


    /**
     * Description of this web resource collection.
     */
    private String description = null;

    /**
     * The name of this web resource collection.
     */
    private String name = null;

    /**
     * @return the description of this web resource collection.
     */
    public String getDescription() {
        return this.description;
    }


    /**
     * Set the description of this web resource collection.
     *
     * @param description The new description
     */
    public void setDescription(String description) {
        this.description = description;
    }


    /**
     * @return the name of this web resource collection.
     */
    public String getName() {
        return this.name;
    }


    /**
     * Set the name of this web resource collection
     *
     * @param name The new name
     */
    public void setName(String name) {
        this.name = name;
    }


}
