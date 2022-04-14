package org.apache.tomcat.util.security;

public class Escape {

    private Escape() {
        // Hide default constructor for this utility class
    }

    /**
     * Escape content for use in XML.
     *
     * @param content   The content to escape
     *
     * @return  The escaped content or {@code null} if the content was
     *          {@code null}
     */
    public static String xml(String content) {
        return xml(null, content);
    }


    /**
     * Escape content for use in XML.
     *
     * @param ifNull    The value to return if content is {@code null}
     * @param content   The content to escape
     *
     * @return  The escaped content or the value of {@code ifNull} if the
     *          content was {@code null}
     */
    public static String xml(String ifNull, String content) {
        return xml(ifNull, false, content);
    }


    /**
     * Escape content for use in XML.
     *
     * @param ifNull        The value to return if content is {@code null}
     * @param escapeCRLF    Should CR and LF also be escaped?
     * @param content       The content to escape
     *
     * @return  The escaped content or the value of ifNull if the content was
     *          {@code null}
     */
    public static String xml(String ifNull, boolean escapeCRLF, String content) {
        if (content == null) {
            return ifNull;
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '<') {
                sb.append("&lt;");
            } else if (c == '>') {
                sb.append("&gt;");
            } else if (c == '\'') {
                sb.append("&apos;");
            } else if (c == '&') {
                sb.append("&amp;");
            } else if (c == '"') {
                sb.append("&quot;");
            } else if (escapeCRLF && c == '\r') {
                sb.append("&#13;");
            } else if (escapeCRLF && c == '\n') {
                sb.append("&#10;");
            } else {
                sb.append(c);
            }
        }

        return (sb.length() > content.length()) ? sb.toString(): content;
    }

}
