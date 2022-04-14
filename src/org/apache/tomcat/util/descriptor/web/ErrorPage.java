package org.apache.tomcat.util.descriptor.web;

import org.apache.tomcat.util.buf.UDecoder;

import java.io.Serializable;

public class ErrorPage extends XmlEncodingBase implements Serializable {

    /**
     * The error (status) code for which this error page is active. Note that
     * status code 0 is used for the default error page.
     */
    private int errorCode = 0;

    /**
     * The context-relative location to handle this error or exception.
     */
    private String location = null;

    /**
     * The exception type for which this error page is active.
     */
    private String exceptionType = null;

    public String getName() {
        if (exceptionType == null) {
            return Integer.toString(errorCode);
        } else {
            return exceptionType;
        }
    }



    /**
     * @return the exception type.
     */
    public String getExceptionType() {
        return this.exceptionType;
    }

    /**
     * @return the error code.
     */
    public int getErrorCode() {
        return this.errorCode;
    }


    /**
     * Set the error code.
     *
     * @param errorCode The new error code
     */
    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }


    /**
     * Set the error code (hack for default XmlMapper data type).
     *
     * @param errorCode The new error code
     */
    public void setErrorCode(String errorCode) {

        try {
            this.errorCode = Integer.parseInt(errorCode);
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException(nfe);
        }
    }


    /**
     * Set the exception type.
     *
     * @param exceptionType The new exception type
     */
    public void setExceptionType(String exceptionType) {
        this.exceptionType = exceptionType;
    }


    /**
     * @return the location.
     */
    public String getLocation() {
        return this.location;
    }


    /**
     * Set the location.
     *
     * @param location The new location
     */
    public void setLocation(String location) {

        //        if ((location == null) || !location.startsWith("/"))
        //            throw new IllegalArgumentException
        //                ("Error Page Location must start with a '/'");
        this.location = UDecoder.URLDecode(location, getCharset());

    }

}
