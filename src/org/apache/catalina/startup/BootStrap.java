package org.apache.catalina.startup;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class BootStrap {
    private static final Log log = LogFactory.getLog(BootStrap.class);

    /**
     * where does it be used TODO
     */
    private static final Object daemonLock = new Object();
    private static volatile BootStrap daemon = null;

    private static final File catalinaBaseFile; //TODO
    private static final File catalinaHomeFile; //TODO

    // used for TODO
    private static final Pattern PATH_PATTERN = Pattern.compile("(\"[^\"]*\")|(([^,])*)");

    static {
        String userDir = System.getProperty("user.dir");
        // Home first
        String home = System.getProperty(Constants.CATALINA_HOME_PROP);
        File homeFile = null;
        if (home != null) {
            File f = new File(home);
            try {
                homeFile = f.getCanonicalFile();
            } catch (IOException ioe) {
                homeFile = f.getAbsoluteFile();
            }
        }

        if (homeFile == null) {
            // First fall-back. See if current directory is a bin directory
            // use for what TODO
            File bootstrapJar = new File(userDir, "bootstrap.jar");

            if (bootstrapJar.exists()) {
                File f = new File(userDir, "..");
                try {
                    homeFile = f.getCanonicalFile();
                } catch (IOException ioe) {
                    homeFile = f.getAbsoluteFile();
                }
            }
        }

        if (homeFile == null) {
            // Second fall-back. Use current directory
            File f = new File(userDir);
            try {
                homeFile = f.getCanonicalFile();
            } catch (IOException e) {
                homeFile = f.getAbsoluteFile();
            }
        }

        catalinaHomeFile = homeFile; //TODO, after so many rounds, finally get it;
        System.setProperty(Constants.CATALINA_HOME_PROP, catalinaHomeFile.getPath());


        // Then base, what 's difference between it and catalinaBase File TODO
        String base = System.getProperty(Constants.CATALINA_BASE_PROP);

        if (base == null) {
            catalinaBaseFile = catalinaHomeFile;
        } else {
            File baseFile = new File(base);
            try {
                baseFile = baseFile.getCanonicalFile();
            } catch (IOException e) {
                baseFile = baseFile.getAbsoluteFile();
            }
            catalinaBaseFile = baseFile;
        }

        System.setProperty(Constants.CATALINA_BASE_PROP, catalinaBaseFile.getPath());
    }
    // TODO first initialized being finished

    /**
     * Daemon reference
     * @param args
     */

    private Object catalinaDaemon = null;
    // The difference between the three
    ClassLoader commonLoader = null;
    ClassLoader catalinaLoader = null;
    ClassLoader sharedLoader = null;

    public static String getCatalinaHome() {
        return catalinaHomeFile.getPath();
    }

    public static String getCatalinaBase() {
        return catalinaBaseFile.getPath();
    }

    private void initClassLoaders() {
        try{
            commonLoader = createClassLoader("common", null);
            if (commonLoader == null) {
                commonLoader = this.getClass().getClassLoader();
            }
            catalinaLoader = createClassLoader("server", commonLoader);
            sharedLoader = createClassLoader("shared", commonLoader);
        } catch (Throwable t) {
            handleThrowable(t);
            log.error("Class loader creation threw exception", t);
            System.exit(1);
        }


    }

    private ClassLoader createClassLoader(String name, ClassLoader parent) {
        String value = CatalinaProperties.getProperty(name + ".loader");
        if (value == null || value.equals("")) {
            return parent;
        }

        value = replace(value);
        List<ClassLoaderFactory.Repository> repositories = new ArrayList<>();
        String[] repositoryPaths = getPaths(value);

        for (String repository : repositoryPaths) {
            try {
                URL url = new URL(repository);
                repositories.add(new ClassLoaderFactory.Repository(repository, ClassLoaderFactory.RepositoryType.URL));
                continue;
            } catch (MalformedURLException e) {
                // Ignore
            }

            // Local repository

            if (repository.endsWith("*.jar")) {
                repository = repository.substring(0, repository.length() - "*.jar".length());
                repositories.add(new ClassLoaderFactory.Repository(repository, ClassLoaderFactory.RepositoryType.GLOB));
            } else if (repository.endsWith(".jar")) {
                repositories.add(new ClassLoaderFactory.Repository(repository, ClassLoaderFactory.RepositoryType.JAR));
            } else {
                repositories.add(new ClassLoaderFactory.Repository(repository, ClassLoaderFactory.RepositoryType.DIR));
            }

            return ClassLoaderFactory.createClassLoader(repositories, parent);
        }


        return null;
    }

    protected static String[] getPaths(String value) {
        return null;
    }

    protected String replace(String value) {
        return null;
    }

    static void handleThrowable(Throwable t) {

    }

    public static void main(String[] args) {

    }
}
