package org.apache.catalina.startup;

import jakarta.servlet.ServletContext;
import org.apache.catalina.Context;
import org.apache.tomcat.util.scan.JarFactory;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

public class WebappServiceLoader<T> {
    private static final String CLASSES = "/WEB-INF/classes/";
    private static final String LIB = "/WEB-INF/lib/";
    private static final String SERVICES = "META-INF/services/";

    private final Context context;
    private final ServletContext servletContext;
    private final Pattern containerSciFilterPattern;

    /**
     * Construct a loader to load services from a ServletContext.
     *
     * @param context the context to use
     */
    public WebappServiceLoader(Context context) {
        this.context = context;
        this.servletContext = context.getServletContext();
        String containerSciFilter = context.getContainerSciFilter();
        if (containerSciFilter != null && containerSciFilter.length() > 0) {
            containerSciFilterPattern = Pattern.compile(containerSciFilter);
        } else {
            containerSciFilterPattern = null;
        }
    }

    /**
     * Load the providers for a service type. Container defined services will be
     * loaded before application defined services in case the application
     * depends on a Container provided service. Note that services are always
     * loaded via the Context (web application) class loader so it is possible
     * for an application to provide an alternative implementation of what would
     * normally be a Container provided service.
     *
     * @param serviceType the type of service to load
     * @return an unmodifiable collection of service providers
     * @throws IOException if there was a problem loading any service
     */
    public List<T> load(Class<T> serviceType) throws IOException {
        String configFile = SERVICES + serviceType.getName();

        // Obtain the Container provided service configuration files.
        ClassLoader loader = context.getParentClassLoader();
        Enumeration<URL> containerResources;
        if (loader == null) {
            containerResources = ClassLoader.getSystemResources(configFile);
        } else {
            containerResources = loader.getResources(configFile);
        }

        // Extract the Container provided service class names. Each
        // configuration file may list more than one service class name. This
        // uses a LinkedHashSet so if a service class name appears more than
        // once in the configuration files, only the first one found is used.
        LinkedHashSet<String> containerServiceClassNames = new LinkedHashSet<>();
        Set<URL> containerServiceConfigFiles = new HashSet<>();
        while (containerResources.hasMoreElements()) {
            URL containerServiceConfigFile = containerResources.nextElement();
            containerServiceConfigFiles.add(containerServiceConfigFile);
            parseConfigFile(containerServiceClassNames, containerServiceConfigFile);
        }

        // Filter the discovered container SCIs if required
        if (containerSciFilterPattern != null) {
            containerServiceClassNames.removeIf(s -> containerSciFilterPattern.matcher(s).find());
        }

        // Obtaining the application provided configuration files is a little
        // more difficult for two reasons:
        // - The web application may employ a custom class loader. Ideally, we
        //   would use ClassLoader.findResources() but that method is protected.
        //   We could force custom class loaders to override that method and
        //   make it public but that would be a new requirement and break
        //   backwards compatibility for what is an often customised component.
        // - If the application web.xml file has defined an order for fragments
        //   then only those JAR files represented by fragments in that order
        //   (and arguably WEB-INF/classes) should be scanned for services.
        LinkedHashSet<String> applicationServiceClassNames = new LinkedHashSet<>();

        // Check to see if the ServletContext has ORDERED_LIBS defined
        @SuppressWarnings("unchecked")
        List<String> orderedLibs = (List<String>) servletContext.getAttribute(ServletContext.ORDERED_LIBS);

        // Obtain the application provided service configuration files
        if (orderedLibs == null) {
            // Because a custom class loader may be being used, we have to use
            // getResources() which will return application and Container files.
            Enumeration<URL> allResources = servletContext.getClassLoader().getResources(configFile);
            while (allResources.hasMoreElements()) {
                URL serviceConfigFile = allResources.nextElement();
                // Only process the service configuration file if it is not a
                // Container level file that has already been processed
                if (!containerServiceConfigFiles.contains(serviceConfigFile)) {
                    parseConfigFile(applicationServiceClassNames, serviceConfigFile);
                }
            }
        } else {
            // Ordered libs so only use services defined in those libs and any
            // in WEB-INF/classes
            URL unpacked = servletContext.getResource(CLASSES + configFile);
            if (unpacked != null) {
                parseConfigFile(applicationServiceClassNames, unpacked);
            }

            for (String lib : orderedLibs) {
                URL jarUrl = servletContext.getResource(LIB + lib);
                if (jarUrl == null) {
                    // should not happen, just ignore
                    continue;
                }

                String base = jarUrl.toExternalForm();
                URL url;
                if (base.endsWith("/")) {
                    url = new URL(base + configFile);
                } else {
                    url = JarFactory.getJarEntryURL(jarUrl, configFile);
                }
                try {
                    parseConfigFile(applicationServiceClassNames, url);
                } catch (FileNotFoundException e) {
                    // no provider file found, this is OK
                }
            }
        }

        // Add the application services after the container services to ensure
        // that the container services are loaded first
        containerServiceClassNames.addAll(applicationServiceClassNames);

        // Short-cut if no services have been found
        if (containerServiceClassNames.isEmpty()) {
            return Collections.emptyList();
        }
        // Load the discovered services
        return loadServices(serviceType, containerServiceClassNames);
    }

    void parseConfigFile(LinkedHashSet<String> servicesFound, URL url) throws IOException {
        try (InputStream is = url.openStream();
             InputStreamReader in = new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(in)) {
            String line;
            while ((line = reader.readLine()) != null) {
                int i = line.indexOf('#');
                if (i >= 0) {
                    line = line.substring(0, i);
                }
                line = line.trim();
                if (line.length() == 0) {
                    continue;
                }
                servicesFound.add(line);
            }
        }
    }


    List<T> loadServices(Class<T> serviceType, LinkedHashSet<String> servicesFound) throws IOException {
        ClassLoader loader = servletContext.getClassLoader();
        List<T> services = new ArrayList<>(servicesFound.size());
        for (String serviceClass : servicesFound) {
            try {
                Class<?> clazz = Class.forName(serviceClass, true, loader);
                services.add(serviceType.cast(clazz.getConstructor().newInstance()));
            } catch (ReflectiveOperationException | ClassCastException e) {
                throw new IOException(e);
            }
        }
        return Collections.unmodifiableList(services);
    }


}
