package org.apache.tomcat.util.descriptor.web;

import org.apache.tomcat.JarScannerCallback;

import java.util.HashMap;
import java.util.Map;

public class FragmentJarScannerCallback implements JarScannerCallback {

    private static final String FRAGMENT_LOCATION =
            "META-INF/web-fragment.xml";
    private final WebXmlParser webXmlParser;
    private final boolean delegate;
    private final boolean parseRequired;
    private final Map<String,WebXml> fragments = new HashMap<>();
    private boolean ok  = true;

    public FragmentJarScannerCallback(WebXmlParser webXmlParser, boolean delegate,
                                      boolean parseRequired) {
        this.webXmlParser = webXmlParser;
        this.delegate = delegate;
        this.parseRequired = parseRequired;
    }


    public boolean isOk() {
        return ok;
    }
    public Map<String,WebXml> getFragments() {
        return fragments;
    }

}
