package org.apache.tomcat.util.scan;

import org.apache.tomcat.Jar;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.Manifest;

public abstract class AbstractInputStreamJar implements Jar {
    private final URL jarFileURL;

    private NonClosingJarInputStream jarInputStream = null;
    private JarEntry entry = null;
    private Boolean multiRelease = null;
    private Map<String,String> mrMap = null;

    public AbstractInputStreamJar(URL jarFileUrl) {
        this.jarFileURL = jarFileUrl;
    }


    protected abstract NonClosingJarInputStream createJarInputStream() throws IOException;


    @Override
    public URL getJarFileURL() {
        return jarFileURL;
    }

    protected void closeStream() {
        if (jarInputStream != null) {
            try {
                jarInputStream.reallyClose();
            } catch (IOException ioe) {
                // Ignore
            }
        }
    }

    @Override
    public void reset() throws IOException {
        closeStream();
        entry = null;
        jarInputStream = createJarInputStream();
        // Only perform multi-release processing on first access
        if (multiRelease == null) {
            Manifest manifest = jarInputStream.getManifest();
            if (manifest == null) {
                multiRelease = Boolean.FALSE;
            } else {
                String mrValue = manifest.getMainAttributes().getValue("Multi-Release");
                if (mrValue == null) {
                    multiRelease = Boolean.FALSE;
                } else {
                    multiRelease = Boolean.valueOf(mrValue);
                }
            }
            if (multiRelease.booleanValue()) {
                if (mrMap == null) {
                    populateMrMap();
                }
            }
        }
    }

    @Override
    public InputStream getEntryInputStream() throws IOException {
        return jarInputStream;
    }


    @Override
    public String getEntryName() {
        // Given how the entry name is used, there is no requirement to convert
        // the name for a multi-release entry to the corresponding base name.
        if (entry == null) {
            return null;
        } else {
            return entry.getName();
        }
    }

    private void populateMrMap() throws IOException {
        int targetVersion = Runtime.version().feature();

        Map<String,Integer> mrVersions = new HashMap<>();

        JarEntry jarEntry = jarInputStream.getNextJarEntry();

        // Tracking the base name and the latest valid version found is
        // sufficient to be able to create the renaming map required
        while (jarEntry != null) {
            String name = jarEntry.getName();
            if (name.startsWith("META-INF/versions/") && name.endsWith(".class")) {

                // Get the base name and version for this versioned entry
                int i = name.indexOf('/', 18);
                if (i > 0) {
                    String baseName = name.substring(i + 1);
                    int version = Integer.parseInt(name.substring(18, i));

                    // Ignore any entries targeting for a later version than
                    // the target for this runtime
                    if (version <= targetVersion) {
                        Integer mappedVersion = mrVersions.get(baseName);
                        if (mappedVersion == null) {
                            // No version found for this name. Create one.
                            mrVersions.put(baseName, Integer.valueOf(version));
                        } else {
                            // Ignore any entry for which we have already found
                            // a later version
                            if (version > mappedVersion.intValue()) {
                                // Replace the earlier version
                                mrVersions.put(baseName, Integer.valueOf(version));
                            }
                        }
                    }
                }
            }
            jarEntry = jarInputStream.getNextJarEntry();
        }

        mrMap = new HashMap<>();

        for (Map.Entry<String,Integer> mrVersion : mrVersions.entrySet()) {
            mrMap.put(mrVersion.getKey() , "META-INF/versions/" + mrVersion.getValue().toString() +
                    "/" +  mrVersion.getKey());
        }

        // Reset stream back to the beginning of the JAR
        closeStream();
        jarInputStream = createJarInputStream();
    }

    @Override
    public void nextEntry() {
        if (jarInputStream == null) {
            try {
                reset();
            } catch (IOException e) {
                entry = null;
                return;
            }
        }
        try {
            entry = jarInputStream.getNextJarEntry();
            if (multiRelease.booleanValue()) {
                // Skip base entries where there is a multi-release entry
                // Skip multi-release entries that are not being used
                while (entry != null &&
                        (mrMap.keySet().contains(entry.getName()) ||
                                entry.getName().startsWith("META-INF/versions/") &&
                                        !mrMap.values().contains(entry.getName()))) {
                    entry = jarInputStream.getNextJarEntry();
                }
            } else {
                // Skip multi-release entries
                while (entry != null && entry.getName().startsWith("META-INF/versions/")) {
                    entry = jarInputStream.getNextJarEntry();
                }
            }
        } catch (IOException ioe) {
            entry = null;
        }
    }

}
