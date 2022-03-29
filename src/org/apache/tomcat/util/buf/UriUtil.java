package org.apache.tomcat.util.buf;

public class UriUtil {

    private static boolean isSchemeChar(char c) {
        return Character.isLetterOrDigit(c) || c == '+' || c == '-' || c == '.';
    }


    public static boolean isAbsoluteURI(String path) {
        // Special case as only a single /
        if (path.startsWith("file:/")) {
            return true;
        }

        // Start at the beginning of the path and skip over any valid protocol
        // characters
        int i = 0;
        while (i < path.length() && isSchemeChar(path.charAt(i))) {
            i++;
        }
        // Need at least one protocol character. False positives with Windows
        // drives such as C:/... will be caught by the later test for "://"
        if (i == 0) {
            return false;
        }
        // path starts with something that might be a protocol. Look for a
        // following "://"
        if (i + 2 < path.length() && path.charAt(i++) == ':' && path.charAt(i++) == '/' && path.charAt(i) == '/') {
            return true;
        }
        return false;
    }
}
