package org.apache.catalina.startup;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class ClassLoaderFactory {

    private static final Log log = LogFactory.getLog(ClassLoaderFactory.class);

    public static ClassLoader createClassLoader(List<Repository> repositories, ClassLoader parent) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Creating new class loader");
        }

        //Construct the "class path" for this class loader

        Set<URL> set = new LinkedHashSet<>();

        if (repositories != null) {
            for (Repository repository : repositories) {
                if (repository.getType() == RepositoryType.URL) {
                    URL url = buildClassLoaderUrl(repository.getLocation());
                    if (log.isDebugEnabled()) {
                        log.debug(" Including URL " + url);
                    }
                    set.add(url);
                } else if (repository.getType() == RepositoryType.DIR) {
                    File directory = new File(repository.getLocation());
                    directory = directory.getCanonicalFile();
                    if (!validateFile(directory, RepositoryType.DIR)) {
                        continue;
                    }
                    URL url = buildClassLoaderUrl(directory);
                    if (log.isDebugEnabled()) {
                        log.debug(" Including directory " + url);
                    }
                    set.add(url);
                } else if (repository.getType() == RepositoryType.JAR) {
                    File file = new File(repository.getLocation());
                    file = file.getCanonicalFile();
                    if (!validateFile(file, RepositoryType.JAR)) {
                        continue;
                    }
                    URL url = buildClassLoaderUrl(file);
                    if (log.isDebugEnabled()) {
                        log.debug(" Including jar file " + url);
                    }
                    set.add(url);
                } else if (repository.getType() == RepositoryType.GLOB) {
                    File directory = new File(repository.getLocation());
                    directory = directory.getCanonicalFile();
                    if (!validateFile(directory, RepositoryType.GLOB)) {
                        continue;
                    }
                    if (log.isDebugEnabled()) {
                        log.debug(" Including directory glob " + directory.getAbsolutePath());
                    }
                    String[] filenames = directory.list();

                    if (filenames == null) {
                        continue;
                    }
                    for (String s : filenames) {
                        String filename = s.toLowerCase(Locale.ENGLISH);//TODO, here to local , what does it mean?
                        if (!filename.endsWith(".jar")) {
                            continue;
                        }
                        File file = new File(directory, s);
                        file = file.getCanonicalFile();
                        if (!validateFile(file, RepositoryType.JAR)) {
                            continue;
                        }
                        if (log.isDebugEnabled()) {
                            log.debug(" Including glob jar file " + file.getAbsolutePath());
                        }
                        URL url = buildClassLoaderUrl(file);
                        set.add(url);
                    }

                }
            }
        }

        final URL[] array = set.toArray(new URL[0]);
        if (log.isDebugEnabled()) {
            for (int i = 0; i < array.length; i++) {
                log.debug(" location " + i + " is " + array[i]);

            }
        }

        return AccessController.doPrivileged(
                (PrivilegedAction<URLClassLoader>) () -> {
                    if (parent == null) {
                        return new URLClassLoader(array);
                    } else {
                        return new URLClassLoader(array, parent);
                    }
                } );

    }

    private static URL buildClassLoaderUrl(File file) throws MalformedURLException {
        String fileUrlString = file.toURI().toString();
        fileUrlString = fileUrlString.replaceAll("!/", "%21/");
        return new URL(fileUrlString);
    }

    // why need to  validate TODO
    private static boolean validateFile(File file, RepositoryType type) throws IOException {
        if (RepositoryType.DIR == type || RepositoryType.GLOB == type) {
            if (!file.isDirectory()|| !file.canRead()) {
                String msg = "Problem with directory [" + file +
                        "], exists:[" + file.exists() + "], isDirectory: [" + file.isDirectory() +
                        "], canRead: [" + file.canRead() + "]";
                File home = new File(Bootstrap.getCatalinaHome());
                home = home.getCanonicalFile();
                File base = new File(Bootstrap.getCatalinaBase());
                base = base.getCanonicalFile();
                File defaultValue = new File(base, "lib");

                //TODO here why is it determined like so?
                if (!home.getPath().equals(base.getPath())
                        && file.getPath().equals(defaultValue.getPath())
                        && !file.exists()) {
                    log.debug(msg);
                } else {
                    log.warn(msg);
                }
                return false;
            }
        } else if (RepositoryType.JAR == type) {
            if (!file.canRead()) {
                log.warn("problem with JAR file [" + file +
                        "], exists: [" + file.exists() +
                        "], canRead:[" + file.canRead() + "]");
                return false;
            }
        }
        return true;
    }

    private static URL buildClassLoaderUrl(String urlString) throws MalformedURLException {
        //TODO, why is it like so?
        String result = urlString.replaceAll("!/", "%21/");
        return new URL(result);
    }

    public enum RepositoryType {
        DIR,GLOB,
        JAR,URL
    }

    public static class Repository {
        private final String location;
        private final RepositoryType type;
        public Repository(String location, RepositoryType type) {
            this.location = location;
            this.type = type;
        }

        public String getLocation() {
            return location;
        }

        public RepositoryType getType() {
            return type;
        }
    }
}
