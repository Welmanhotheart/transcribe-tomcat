package org.apache.catalina.startup;

import org.apache.catalina.security.SecurityClassLoad;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Bootstrap {
    private static final Log log = LogFactory.getLog(Bootstrap.class);

    /**
     * where does it be used TODO
     */
    private static final Object daemonLock = new Object();
    private static volatile Bootstrap daemon = null;

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
    // the server loader TODO
    ClassLoader catalinaLoader = null;
    ClassLoader sharedLoader = null;

    public static String getCatalinaHome() {
        return catalinaHomeFile.getPath();
    }

    public static String getCatalinaBase() {
        return catalinaBaseFile.getPath();
    }

    public static File getCatalinaBaseFile() {
        return catalinaBaseFile;
    }




    public static File getCatalinaHomeFile() {
        return catalinaHomeFile;
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

    private ClassLoader createClassLoader(String name, ClassLoader parent) throws Exception {
        String value = CatalinaProperties.getProperty(name + ".loader");
        if (value == null || value.equals("")) {
            return parent;
        }

        value = replace(value);
        List<ClassLoaderFactory.Repository> repositories = new ArrayList<>();
        String[] repositoryPaths = getPaths(value);

        for (String repository : repositoryPaths) {
            try {
                // Check for a jar URL repository
                @SuppressWarnings("unused")
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

        }
        return ClassLoaderFactory.createClassLoader(repositories, parent);
    }

    // protected for unit testing
    protected static String[] getPaths(String value) {
        ArrayList<String> result = new ArrayList<>();
        Matcher matcher = PATH_PATTERN.matcher(value);

        while (matcher.find()) {
            // matcher.start, matcher.end need to understand TODO
            String path = value.substring(matcher.start(), matcher.end());

            path = path.trim();

            if (path.length() == 0) {
                continue;
            }

            char first = path.charAt(0);
            char last = path.charAt(path.length() - 1);

            if (first == '"' && last == '"' && path.length() > 1) {
                path = path.substring(1, path.length() - 1);
                path = path.trim();
                if (path.length() == 0) {
                    continue;
                }
            } else if (path.contains("\"")) {
                //Unbalanced quotes
                // Too early to use standard i18n support. The class path hasn't
                // been configured.
                throw new IllegalArgumentException("The double quote [\"] character can only be used to quote paths. It must " +
                        "not appear in a path. This loader path is not valid: ["+ value + "]");
            } else {
                // Not quoted - NO-OP
            }

            result.add(path);

        }
        return result.toArray(new String[0]);
    }

    protected String replace(String str) {
        String result = str;
        int post_start = str.indexOf("${");
        if (post_start > 0) {
            StringBuilder builder = new StringBuilder();
            int pos_end = -1;

            while (post_start > 0) {
                builder.append(str, pos_end + 1, post_start);
                pos_end = str.indexOf('}', post_start + 2);
                if (pos_end < 0) {
                    pos_end = post_start - 1;
                    break;
                }
                String propName = str.substring(post_start + 2, pos_end);
                String replacement = null;
                if (propName.length() == 0) {
                    replacement = null;
                } else if(Constants.CATALINA_HOME_PROP.equals(propName)) {
                    replacement = getCatalinaHome();
                } else if(Constants.CATALINA_BASE_PROP.equals(propName)) {
                    replacement = getCatalinaBase();
                }

                if (replacement != null) {
                    builder.append(replacement);
                } else {
                    builder.append(str, post_start, pos_end + 1);
                }

                post_start = str.indexOf("${", pos_end + 1);
            }
            builder.append(str, pos_end + 1, str.length());
            result = builder.toString();
        }
        return result;
    }

    static void handleThrowable(Throwable t) {

    }

    public static void main(String[] args) {
        // here why it is used like so TODO
        synchronized (daemonLock) {
            if (daemon == null) {
                Bootstrap bootStrap = new Bootstrap();
                try {
                    bootStrap.init();
                } catch (Throwable t) { // here is throwable, why not Exception
                    handleThrowable(t);
                    t.printStackTrace();
                    return;
                }
                daemon = bootStrap;
            } else {
                // here set the server loader
                Thread.currentThread().setContextClassLoader(daemon.catalinaLoader);
            }
        }

        try {
            String command = "start";
            if (args.length > 0) {
                command = args[args.length - 1];
            }
            if (command.equals("startd")) {
                args[args.length - 1] = "start";
                daemon.load(args);
                daemon.start();
            } else if (command.equals("stopd")) {
                args[args.length - 1] = "stop";
                daemon.stop();
            } else if (command.equals("start")) {
                daemon.setAwait(true);
                daemon.load(args);
                daemon.start();
                if (null == daemon.getServer()) {
                    System.exit(1);
                }
            } else if (command.equals("stop")) {
                daemon.stopServer(args);
            } else if (command.equals("configtest")) {
                daemon.load(args);
                if (null == daemon.getServer()) {
                    System.exit(1);
                }
                System.exit(0);
            } else {
                log.warn("Bootstrap: command \"" + command + "\" does not exist.");
            }
        } catch (Throwable t) {
            if(t instanceof InvocationTargetException &&
                    t.getCause()!= null) {
                t = t.getCause();
            }
            handleThrowable(t);
            t.printStackTrace();
            System.exit(1);
        }
    }

    private void stopServer(String[] args) {
    }

    private Object getServer() throws Exception {
        String methodName = "getServer";
        Method method = catalinaDaemon.getClass().getMethod(methodName);
        return method.invoke(catalinaDaemon);
    }

    private void setAwait(boolean b) {
    }

    private void stop() {

    }

    private void start() {

    }

    private void load(String[] arguments) throws Exception {
        // Call the load() method
        String methodName = "load";
        Object param[];
        Class<?> paramTypes[];

        if (arguments == null || arguments.length == 0) {
            paramTypes = null;
            param = null;
        } else {
            paramTypes = new Class[1];
            paramTypes[0] = arguments.getClass();
            param = new Object[1];
            param[0] = arguments;
        }

        Method method = catalinaDaemon.getClass().getMethod(methodName,paramTypes);
        if (log.isDebugEnabled()) {
            log.debug("Calling startup class " + method);
        }
        method.invoke(catalinaDaemon, param);
    }

    private void init() throws Exception {
        // common, server, shared
        initClassLoaders();

        Thread.currentThread().setContextClassLoader(catalinaLoader);

        //TODO
        SecurityClassLoad.securityClassLoad(catalinaLoader);

        if (log.isDebugEnabled()) {
            log.debug("Loading startup class");
        }

        Class<?> startupClass = catalinaLoader.loadClass("org.apache.catalina.startup.Catalina");
        Object startupInstance = startupClass.getConstructor().newInstance();

        // Set the shared extensions class loader
        if (log.isDebugEnabled()) {
            log.debug("Setting startup class properties");
        }

        String methodName = "setParentClassLoader";
        Class<?> paramTypes[] = new Class[1];
        paramTypes[0] = Class.forName("java.lang.ClassLoader");
        Object[] paramValues = new Object[1];
        paramValues[0] = sharedLoader; //TODO, what? passed sharedLoader?， what is the role of sharedLoader
        Method method = startupInstance.getClass().getMethod(methodName, paramTypes);
        method.invoke(startupInstance, paramValues);
        catalinaDaemon = startupInstance;

    }
}
