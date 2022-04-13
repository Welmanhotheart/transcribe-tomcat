package org.apache.tomcat.util.descriptor.web;

import jakarta.servlet.ServletContext;
import jakarta.servlet.SessionTrackingMode;
import org.apache.catalina.org.apache.tomcat.util.descriptor.web.LoginConfig;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.buf.B2CConverter;
import org.apache.tomcat.util.buf.UDecoder;
import org.apache.tomcat.util.descriptor.XmlIdentifiers;
import org.apache.tomcat.util.digester.DocumentProperties;
import org.apache.tomcat.util.res.StringManager;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.*;

public class WebXml extends XmlEncodingBase implements DocumentProperties.Charset {

    protected static final String ORDER_OTHERS =
            "org.apache.catalina.order.others";
    private static final StringManager sm =
            StringManager.getManager(Constants.PACKAGE_NAME);

    private final Log log = LogFactory.getLog(WebXml.class); // must not be static


    /*
     * Ideally, fragment names will be unique. If they are not, Tomcat needs
     * to know as the action that the specification requires (see 8.2.2 1.e and
     * 2.c) varies depending on the ordering method used.
     */
    private boolean duplicated = false;
    public boolean isDuplicated() {
        return duplicated;
    }
    public void setDuplicated(boolean duplicated) {
        this.duplicated = duplicated;
    }

    // Is this JAR part of the application or is it a container JAR? Assume it
    // is.
    private boolean webappJar = true;
    public void setWebappJar(boolean webappJar) { this.webappJar = webappJar; }
    public boolean getWebappJar() { return webappJar; }

    // distributable
    private boolean distributable = false;
    public boolean isDistributable() { return distributable; }
    public void setDistributable(boolean distributable) {
        this.distributable = distributable;
    }

    // deny-uncovered-http-methods
    private boolean denyUncoveredHttpMethods = false;
    public boolean getDenyUncoveredHttpMethods() {
        return denyUncoveredHttpMethods;
    }
    public void setDenyUncoveredHttpMethods(boolean denyUncoveredHttpMethods) {
        this.denyUncoveredHttpMethods = denyUncoveredHttpMethods;
    }

    // message-destination
    // TODO: Should support multiple description elements with language
    // TODO: Should support multiple display-names elements with language
    // TODO: Should support multiple icon elements ???
    private final Map<String,MessageDestination> messageDestinations =
            new HashMap<>();
    public void addMessageDestination(
            MessageDestination messageDestination) {
        if (messageDestinations.containsKey(
                messageDestination.getName())) {
            // message-destination names must be unique within a
            // web(-fragment).xml
            throw new IllegalArgumentException(
                    sm.getString("webXml.duplicateMessageDestination",
                            messageDestination.getName()));
        }
        messageDestinations.put(messageDestination.getName(),
                messageDestination);
    }
    public Map<String,MessageDestination> getMessageDestinations() {
        return messageDestinations;
    }


    // URL of JAR / exploded JAR for this web-fragment
    private URL uRL = null;
    public void setURL(URL url) { this.uRL = url; }
    public URL getURL() { return uRL; }

    // context-param
    // TODO: description (multiple with language) is ignored
    private final Map<String,String> contextParams = new HashMap<>();
    public void addContextParam(String param, String value) {
        contextParams.put(param, value);
    }
    public Map<String,String> getContextParams() { return contextParams; }





    // display-name - TODO should support multiple with language
    private String displayName = null;
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    private String requestCharacterEncoding;
    public String getRequestCharacterEncoding() {
        return requestCharacterEncoding;
    }
    public void setRequestCharacterEncoding(String requestCharacterEncoding) {
        if (requestCharacterEncoding != null) {
            try {
                B2CConverter.getCharset(requestCharacterEncoding);
            } catch (UnsupportedEncodingException e) {
                throw new IllegalArgumentException(e);
            }
        }
        this.requestCharacterEncoding = requestCharacterEncoding;
    }

    private String responseCharacterEncoding;
    public String getResponseCharacterEncoding() {
        return responseCharacterEncoding;
    }
    public void setResponseCharacterEncoding(String responseCharacterEncoding) {
        if (responseCharacterEncoding != null) {
            try {
                B2CConverter.getCharset(responseCharacterEncoding);
            } catch (UnsupportedEncodingException e) {
                throw new IllegalArgumentException(e);
            }
        }
        this.responseCharacterEncoding = responseCharacterEncoding;
    }


    // ejb-ref
    // TODO: Should support multiple description elements with language
    private final Map<String,ContextEjb> ejbRefs = new HashMap<>();
    public void addEjbRef(ContextEjb ejbRef) {
        ejbRefs.put(ejbRef.getName(),ejbRef);
    }
    public Map<String,ContextEjb> getEjbRefs() { return ejbRefs; }

    // env-entry
    // TODO: Should support multiple description elements with language
    private final Map<String,ContextEnvironment> envEntries = new HashMap<>();
    public void addEnvEntry(ContextEnvironment envEntry) {
        if (envEntries.containsKey(envEntry.getName())) {
            // env-entry names must be unique within a web(-fragment).xml
            throw new IllegalArgumentException(
                    sm.getString("webXml.duplicateEnvEntry",
                            envEntry.getName()));
        }
        envEntries.put(envEntry.getName(),envEntry);
    }
    public Map<String,ContextEnvironment> getEnvEntries() { return envEntries; }

    // error-page
    private final Map<String,ErrorPage> errorPages = new HashMap<>();
    public void addErrorPage(ErrorPage errorPage) {
        errorPage.setCharset(getCharset());
        errorPages.put(errorPage.getName(), errorPage);
    }
    public Map<String,ErrorPage> getErrorPages() { return errorPages; }


    private static boolean mergeFilter(FilterDef src, FilterDef dest,
                                       boolean failOnConflict) {
        if (dest.getAsyncSupported() == null) {
            dest.setAsyncSupported(src.getAsyncSupported());
        } else if (src.getAsyncSupported() != null) {
            if (failOnConflict &&
                    !src.getAsyncSupported().equals(dest.getAsyncSupported())) {
                return false;
            }
        }

        if (dest.getFilterClass()  == null) {
            dest.setFilterClass(src.getFilterClass());
        } else if (src.getFilterClass() != null) {
            if (failOnConflict &&
                    !src.getFilterClass().equals(dest.getFilterClass())) {
                return false;
            }
        }

        for (Map.Entry<String,String> srcEntry :
                src.getParameterMap().entrySet()) {
            if (dest.getParameterMap().containsKey(srcEntry.getKey())) {
                if (failOnConflict && !dest.getParameterMap().get(
                        srcEntry.getKey()).equals(srcEntry.getValue())) {
                    return false;
                }
            } else {
                dest.addInitParameter(srcEntry.getKey(), srcEntry.getValue());
            }
        }
        return true;
    }


    // jsp-config/jsp-property-group
    private final Set<JspPropertyGroup> jspPropertyGroups = new LinkedHashSet<>();




    // locale-encoding-mapping-list
    private final Map<String,String> localeEncodingMappings = new HashMap<>();


    // login-config
    // Digester will check there is only one of these
    private LoginConfig loginConfig = null;
    public void setLoginConfig(LoginConfig loginConfig) {
        loginConfig.setCharset(getCharset());
        this.loginConfig = loginConfig;
    }
    public LoginConfig getLoginConfig() { return loginConfig; }


    private <T extends ResourceBase> boolean mergeResourceMap(
            Map<String, T> fragmentResources, Map<String, T> mainResources,
            Map<String, T> tempResources, WebXml fragment) {
        for (T resource : fragmentResources.values()) {
            String resourceName = resource.getName();
            if (mainResources.containsKey(resourceName)) {
                mainResources.get(resourceName).getInjectionTargets().addAll(
                        resource.getInjectionTargets());
            } else {
                // Not defined in main web.xml
                T existingResource = tempResources.get(resourceName);
                if (existingResource != null) {
                    if (!existingResource.equals(resource)) {
                        log.error(sm.getString(
                                "webXml.mergeConflictResource",
                                resourceName,
                                fragment.getName(),
                                fragment.getURL()));
                        return false;
                    }
                } else {
                    tempResources.put(resourceName, resource);
                }
            }
        }
        return true;
    }


    // ejb-local-ref
    // TODO: Should support multiple description elements with language
    private final Map<String,ContextLocalEjb> ejbLocalRefs = new HashMap<>();
    public void addEjbLocalRef(ContextLocalEjb ejbLocalRef) {
        ejbLocalRefs.put(ejbLocalRef.getName(),ejbLocalRef);
    }
    public Map<String,ContextLocalEjb> getEjbLocalRefs() {
        return ejbLocalRefs;
    }


    // jsp-config/jsp-property-group
    public void addJspPropertyGroup(JspPropertyGroup propertyGroup) {
        propertyGroup.setCharset(getCharset());
        jspPropertyGroups.add(propertyGroup);
    }
    public Set<JspPropertyGroup> getJspPropertyGroups() {
        return jspPropertyGroups;
    }


    // locale-encoding-mapping-list
    public void addLocaleEncodingMapping(String locale, String encoding) {
        localeEncodingMappings.put(locale, encoding);
    }
    public Map<String,String> getLocaleEncodingMappings() {
        return localeEncodingMappings;
    }


    // message-destination-ref
    // TODO: Should support multiple description elements with language
    private final Map<String,MessageDestinationRef> messageDestinationRefs =
            new HashMap<>();
    public void addMessageDestinationRef(
            MessageDestinationRef messageDestinationRef) {
        if (messageDestinationRefs.containsKey(
                messageDestinationRef.getName())) {
            // message-destination-ref names must be unique within a
            // web(-fragment).xml
            throw new IllegalArgumentException(sm.getString(
                    "webXml.duplicateMessageDestinationRef",
                    messageDestinationRef.getName()));
        }
        messageDestinationRefs.put(messageDestinationRef.getName(),
                messageDestinationRef);
    }
    public Map<String,MessageDestinationRef> getMessageDestinationRefs() {
        return messageDestinationRefs;
    }





    // Optional metadata-complete attribute
    private boolean metadataComplete = false;
    public boolean isMetadataComplete() { return metadataComplete; }
    public void setMetadataComplete(boolean metadataComplete) {
        this.metadataComplete = metadataComplete; }

    /**
     * web-fragment.xml only elements
     * Relative ordering
     */
    private final Set<String> after = new LinkedHashSet<>();
    public void addAfterOrdering(String fragmentName) {
        after.add(fragmentName);
    }
    public void addAfterOrderingOthers() {
        if (before.contains(ORDER_OTHERS)) {
            throw new IllegalArgumentException(sm.getString(
                    "webXml.multipleOther"));
        }
        after.add(ORDER_OTHERS);
    }
    public Set<String> getAfterOrdering() { return after; }

    private final Set<String> before = new LinkedHashSet<>();
    public void addBeforeOrdering(String fragmentName) {
        before.add(fragmentName);
    }
    public void addBeforeOrderingOthers() {
        if (after.contains(ORDER_OTHERS)) {
            throw new IllegalArgumentException(sm.getString(
                    "webXml.multipleOther"));
        }
        before.add(ORDER_OTHERS);
    }
    public Set<String> getBeforeOrdering() { return before; }

    // Does this web application delegate first for class loading?
    private boolean delegate = false;
    public boolean getDelegate() { return delegate; }
    public void setDelegate(boolean delegate) { this.delegate = delegate; }


    // Derived major and minor version attributes
    private int majorVersion = 6;
    private int minorVersion = 0;
    public int getMajorVersion() { return majorVersion; }
    public int getMinorVersion() { return minorVersion; }

    /**
     * web.xml only elements
     * Absolute Ordering
     */
    private Set<String> absoluteOrdering = null;
    public void createAbsoluteOrdering() {
        if (absoluteOrdering == null) {
            absoluteOrdering = new LinkedHashSet<>();
        }
    }
    public void addAbsoluteOrdering(String fragmentName) {
        createAbsoluteOrdering();
        absoluteOrdering.add(fragmentName);
    }
    public void addAbsoluteOrderingOthers() {
        createAbsoluteOrdering();
        absoluteOrdering.add(ORDER_OTHERS);
    }
    public Set<String> getAbsoluteOrdering() {
        return absoluteOrdering;
    }


    // Common elements and attributes
    // Required attribute of web-app element
    public String getVersion() {
        StringBuilder sb = new StringBuilder(3);
        sb.append(majorVersion);
        sb.append('.');
        sb.append(minorVersion);
        return sb.toString();
    }


    /**
     * Set the version for this web.xml file
     * @param version   Values of <code>null</code> will be ignored
     */
    public void setVersion(String version) {
        if (version == null) {
            return;
        }
        switch (version) {
            case "2.4":
                majorVersion = 2;
                minorVersion = 4;
                break;
            case "2.5":
                majorVersion = 2;
                minorVersion = 5;
                break;
            case "3.0":
                majorVersion = 3;
                minorVersion = 0;
                break;
            case "3.1":
                majorVersion = 3;
                minorVersion = 1;
                break;
            case "4.0":
                majorVersion = 4;
                minorVersion = 0;
                break;
            case "5.0":
                majorVersion = 5;
                minorVersion = 0;
                break;
            case "6.0":
                majorVersion = 6;
                minorVersion = 0;
                break;
            default:
                log.warn(sm.getString("webXml.version.unknown", version));
        }
    }


    private boolean alwaysAddWelcomeFiles = true;

    /**
     * When merging from this web.xml, should the welcome files be added to the
     * target web.xml even if it already contains welcome file definitions.
     * @param alwaysAddWelcomeFiles <code>true</code> to add welcome files
     */
    public void setAlwaysAddWelcomeFiles(boolean alwaysAddWelcomeFiles) {
        this.alwaysAddWelcomeFiles = alwaysAddWelcomeFiles;
    }

    /**
     * Global defaults are overridable but Servlets and Servlet mappings need to
     * be unique. Duplicates normally trigger an error. This flag indicates if
     * newly added Servlet elements are marked as overridable.
     */
    private boolean overridable = false;

    /**
     * Global defaults are overridable but Servlets and Servlet mappings need to
     * be unique. Duplicates normally trigger an error. This flag indicates if
     * newly added Servlet elements are marked as overridable.
     */
    public boolean isOverridable() {
        return overridable;
    }
    public void setOverridable(boolean overridable) {
        this.overridable = overridable;
    }

    // listener
    // TODO: description (multiple with language) is ignored
    // TODO: display-name (multiple with language) is ignored
    // TODO: icon (multiple) is ignored
    private final Set<String> listeners = new LinkedHashSet<>();
    public void addListener(String className) {
        listeners.add(className);
    }
    public Set<String> getListeners() { return listeners; }

    // servlet
    // TODO: description (multiple with language) is ignored
    // TODO: display-name (multiple with language) is ignored
    // TODO: icon (multiple) is ignored
    // TODO: init-param/description (multiple with language) is ignored
    // TODO: security-role-ref/description (multiple with language) is ignored
    private final Map<String,ServletDef> servlets = new HashMap<>();
    public void addServlet(ServletDef servletDef) {
        servlets.put(servletDef.getServletName(), servletDef);
        if (overridable) {
            servletDef.setOverridable(overridable);
        }
    }
    public Map<String,ServletDef> getServlets() { return servlets; }

    // servlet-mapping
    // Note: URLPatterns from web.xml may be URL encoded
    //       (https://svn.apache.org/r285186)
    private final Map<String,String> servletMappings = new HashMap<>();
    private final Set<String> servletMappingNames = new HashSet<>();
    public void addServletMapping(String urlPattern, String servletName) {
        addServletMappingDecoded(UDecoder.URLDecode(urlPattern, getCharset()), servletName);
    }
    public void addServletMappingDecoded(String urlPattern, String servletName) {
        String oldServletName = servletMappings.put(urlPattern, servletName);
        if (oldServletName != null) {
            // Duplicate mapping. As per clarification from the Servlet EG,
            // deployment should fail.
            throw new IllegalArgumentException(sm.getString(
                    "webXml.duplicateServletMapping", oldServletName,
                    servletName, urlPattern));
        }
        servletMappingNames.add(servletName);
    }
    public Map<String,String> getServletMappings() { return servletMappings; }

    // session-config
    // Digester will check there is only one of these
    private SessionConfig sessionConfig = new SessionConfig();
    public void setSessionConfig(SessionConfig sessionConfig) {
        this.sessionConfig = sessionConfig;
    }
    public SessionConfig getSessionConfig() { return sessionConfig; }

    // filter-mapping
    private final Set<FilterMap> filterMaps = new LinkedHashSet<>();
    private final Set<String> filterMappingNames = new HashSet<>();
    public void addFilterMapping(FilterMap filterMap) {
        filterMap.setCharset(getCharset());
        filterMaps.add(filterMap);
        filterMappingNames.add(filterMap.getFilterName());
    }
    public Set<FilterMap> getFilterMappings() { return filterMaps; }


    private <T> boolean mergeMap(Map<String,T> fragmentMap,
                                 Map<String,T> mainMap, Map<String,T> tempMap, WebXml fragment,
                                 String mapName) {
        for (Map.Entry<String, T> entry : fragmentMap.entrySet()) {
            final String key = entry.getKey();
            if (!mainMap.containsKey(key)) {
                // Not defined in main web.xml
                T value = entry.getValue();
                if (tempMap.containsKey(key)) {
                    if (value != null && !value.equals(
                            tempMap.get(key))) {
                        log.error(sm.getString(
                                "webXml.mergeConflictString",
                                mapName,
                                key,
                                fragment.getName(),
                                fragment.getURL()));
                        return false;
                    }
                } else {
                    tempMap.put(key, value);
                }
            }
        }
        return true;
    }

    /**
     * Merge the supplied web fragments into this main web.xml.
     *
     * @param fragments     The fragments to merge in
     * @return <code>true</code> if merge is successful, else
     *         <code>false</code>
     */
    public boolean merge(Set<WebXml> fragments) {
        // As far as possible, process in alphabetical order so it is easy to
        // check everything is present

        // Merge rules vary from element to element. See SRV.8.2.3

        WebXml temp = new WebXml();

        for (WebXml fragment : fragments) {
            if (!mergeMap(fragment.getContextParams(), contextParams,
                    temp.getContextParams(), fragment, "Context Parameter")) {
                return false;
            }
        }
        contextParams.putAll(temp.getContextParams());

        if (displayName == null) {
            for (WebXml fragment : fragments) {
                String value = fragment.getDisplayName();
                if (value != null) {
                    if (temp.getDisplayName() == null) {
                        temp.setDisplayName(value);
                    } else {
                        log.error(sm.getString(
                                "webXml.mergeConflictDisplayName",
                                fragment.getName(),
                                fragment.getURL()));
                        return false;
                    }
                }
            }
            displayName = temp.getDisplayName();
        }

        // Note: Not permitted in fragments but we also use fragments for
        //       per-Host and global defaults so they may appear there
        if (!denyUncoveredHttpMethods) {
            for (WebXml fragment : fragments) {
                if (fragment.getDenyUncoveredHttpMethods()) {
                    denyUncoveredHttpMethods = true;
                    break;
                }
            }
        }
        if (requestCharacterEncoding == null) {
            for (WebXml fragment : fragments) {
                if (fragment.getRequestCharacterEncoding() != null) {
                    requestCharacterEncoding = fragment.getRequestCharacterEncoding();
                }
            }
        }
        if (responseCharacterEncoding == null) {
            for (WebXml fragment : fragments) {
                if (fragment.getResponseCharacterEncoding() != null) {
                    responseCharacterEncoding = fragment.getResponseCharacterEncoding();
                }
            }
        }

        if (distributable) {
            for (WebXml fragment : fragments) {
                if (!fragment.isDistributable()) {
                    distributable = false;
                    break;
                }
            }
        }

        for (WebXml fragment : fragments) {
            if (!mergeResourceMap(fragment.getEjbLocalRefs(), ejbLocalRefs,
                    temp.getEjbLocalRefs(), fragment)) {
                return false;
            }
        }
        ejbLocalRefs.putAll(temp.getEjbLocalRefs());

        for (WebXml fragment : fragments) {
            if (!mergeResourceMap(fragment.getEjbRefs(), ejbRefs,
                    temp.getEjbRefs(), fragment)) {
                return false;
            }
        }
        ejbRefs.putAll(temp.getEjbRefs());

        for (WebXml fragment : fragments) {
            if (!mergeResourceMap(fragment.getEnvEntries(), envEntries,
                    temp.getEnvEntries(), fragment)) {
                return false;
            }
        }
        envEntries.putAll(temp.getEnvEntries());

        for (WebXml fragment : fragments) {
            if (!mergeMap(fragment.getErrorPages(), errorPages,
                    temp.getErrorPages(), fragment, "Error Page")) {
                return false;
            }
        }
        errorPages.putAll(temp.getErrorPages());

        // As per 'clarification' from the Servlet EG, filter definitions in the
        // main web.xml override those in fragments and those in fragments
        // override those in annotations
        List<FilterMap> filterMapsToAdd = new ArrayList<>();
        for (WebXml fragment : fragments) {
            for (FilterMap filterMap : fragment.getFilterMappings()) {
                if (!filterMappingNames.contains(filterMap.getFilterName())) {
                    filterMapsToAdd.add(filterMap);
                }
            }
        }
        for (FilterMap filterMap : filterMapsToAdd) {
            // Additive
            addFilterMapping(filterMap);
        }

        for (WebXml fragment : fragments) {
            for (Map.Entry<String,FilterDef> entry :
                    fragment.getFilters().entrySet()) {
                if (filters.containsKey(entry.getKey())) {
                    mergeFilter(entry.getValue(),
                            filters.get(entry.getKey()), false);
                } else {
                    if (temp.getFilters().containsKey(entry.getKey())) {
                        if (!(mergeFilter(entry.getValue(),
                                temp.getFilters().get(entry.getKey()), true))) {
                            log.error(sm.getString(
                                    "webXml.mergeConflictFilter",
                                    entry.getKey(),
                                    fragment.getName(),
                                    fragment.getURL()));

                            return false;
                        }
                    } else {
                        temp.getFilters().put(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
        filters.putAll(temp.getFilters());

        for (WebXml fragment : fragments) {
            for (JspPropertyGroup jspPropertyGroup :
                    fragment.getJspPropertyGroups()) {
                // Always additive
                addJspPropertyGroup(jspPropertyGroup);
            }
        }

        for (WebXml fragment : fragments) {
            for (String listener : fragment.getListeners()) {
                // Always additive
                addListener(listener);
            }
        }

        for (WebXml fragment : fragments) {
            if (!mergeMap(fragment.getLocaleEncodingMappings(),
                    localeEncodingMappings, temp.getLocaleEncodingMappings(),
                    fragment, "Locale Encoding Mapping")) {
                return false;
            }
        }
        localeEncodingMappings.putAll(temp.getLocaleEncodingMappings());

        if (getLoginConfig() == null) {
            LoginConfig tempLoginConfig = null;
            for (WebXml fragment : fragments) {
                LoginConfig fragmentLoginConfig = fragment.loginConfig;
                if (fragmentLoginConfig != null) {
                    if (tempLoginConfig == null ||
                            fragmentLoginConfig.equals(tempLoginConfig)) {
                        tempLoginConfig = fragmentLoginConfig;
                    } else {
                        log.error(sm.getString(
                                "webXml.mergeConflictLoginConfig",
                                fragment.getName(),
                                fragment.getURL()));
                    }
                }
            }
            loginConfig = tempLoginConfig;
        }

        for (WebXml fragment : fragments) {
            if (!mergeResourceMap(fragment.getMessageDestinationRefs(), messageDestinationRefs,
                    temp.getMessageDestinationRefs(), fragment)) {
                return false;
            }
        }
        messageDestinationRefs.putAll(temp.getMessageDestinationRefs());

        for (WebXml fragment : fragments) {
            if (!mergeResourceMap(fragment.getMessageDestinations(), messageDestinations,
                    temp.getMessageDestinations(), fragment)) {
                return false;
            }
        }
        messageDestinations.putAll(temp.getMessageDestinations());

        for (WebXml fragment : fragments) {
            if (!mergeMap(fragment.getMimeMappings(), mimeMappings,
                    temp.getMimeMappings(), fragment, "Mime Mapping")) {
                return false;
            }
        }
        mimeMappings.putAll(temp.getMimeMappings());

        for (WebXml fragment : fragments) {
            if (!mergeResourceMap(fragment.getResourceEnvRefs(), resourceEnvRefs,
                    temp.getResourceEnvRefs(), fragment)) {
                return false;
            }
        }
        resourceEnvRefs.putAll(temp.getResourceEnvRefs());

        for (WebXml fragment : fragments) {
            if (!mergeResourceMap(fragment.getResourceRefs(), resourceRefs,
                    temp.getResourceRefs(), fragment)) {
                return false;
            }
        }
        resourceRefs.putAll(temp.getResourceRefs());

        for (WebXml fragment : fragments) {
            for (SecurityConstraint constraint : fragment.getSecurityConstraints()) {
                // Always additive
                addSecurityConstraint(constraint);
            }
        }

        for (WebXml fragment : fragments) {
            for (String role : fragment.getSecurityRoles()) {
                // Always additive
                addSecurityRole(role);
            }
        }

        for (WebXml fragment : fragments) {
            if (!mergeResourceMap(fragment.getServiceRefs(), serviceRefs,
                    temp.getServiceRefs(), fragment)) {
                return false;
            }
        }
        serviceRefs.putAll(temp.getServiceRefs());

        // As per 'clarification' from the Servlet EG, servlet definitions and
        // mappings in the main web.xml override those in fragments and those in
        // fragments override those in annotations
        // Skip servlet definitions and mappings from fragments that are
        // defined in web.xml
        List<Map.Entry<String,String>> servletMappingsToAdd = new ArrayList<>();
        for (WebXml fragment : fragments) {
            for (Map.Entry<String,String> servletMap :
                    fragment.getServletMappings().entrySet()) {
                if (!servletMappingNames.contains(servletMap.getValue()) &&
                        !servletMappings.containsKey(servletMap.getKey())) {
                    servletMappingsToAdd.add(servletMap);
                }
            }
        }

        // Add fragment mappings
        for (Map.Entry<String,String> mapping : servletMappingsToAdd) {
            addServletMappingDecoded(mapping.getKey(), mapping.getValue());
        }

        for (WebXml fragment : fragments) {
            for (Map.Entry<String,ServletDef> entry :
                    fragment.getServlets().entrySet()) {
                if (servlets.containsKey(entry.getKey())) {
                    mergeServlet(entry.getValue(),
                            servlets.get(entry.getKey()), false);
                } else {
                    if (temp.getServlets().containsKey(entry.getKey())) {
                        if (!(mergeServlet(entry.getValue(),
                                temp.getServlets().get(entry.getKey()), true))) {
                            log.error(sm.getString(
                                    "webXml.mergeConflictServlet",
                                    entry.getKey(),
                                    fragment.getName(),
                                    fragment.getURL()));

                            return false;
                        }
                    } else {
                        temp.getServlets().put(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
        servlets.putAll(temp.getServlets());

        if (sessionConfig.getSessionTimeout() == null) {
            for (WebXml fragment : fragments) {
                Integer value = fragment.getSessionConfig().getSessionTimeout();
                if (value != null) {
                    if (temp.getSessionConfig().getSessionTimeout() == null) {
                        temp.getSessionConfig().setSessionTimeout(value.toString());
                    } else if (value.equals(
                            temp.getSessionConfig().getSessionTimeout())) {
                        // Fragments use same value - no conflict
                    } else {
                        log.error(sm.getString(
                                "webXml.mergeConflictSessionTimeout",
                                fragment.getName(),
                                fragment.getURL()));
                        return false;
                    }
                }
            }
            if (temp.getSessionConfig().getSessionTimeout() != null) {
                sessionConfig.setSessionTimeout(
                        temp.getSessionConfig().getSessionTimeout().toString());
            }
        }

        if (sessionConfig.getCookieName() == null) {
            for (WebXml fragment : fragments) {
                String value = fragment.getSessionConfig().getCookieName();
                if (value != null) {
                    if (temp.getSessionConfig().getCookieName() == null) {
                        temp.getSessionConfig().setCookieName(value);
                    } else if (value.equals(
                            temp.getSessionConfig().getCookieName())) {
                        // Fragments use same value - no conflict
                    } else {
                        log.error(sm.getString(
                                "webXml.mergeConflictSessionCookieName",
                                fragment.getName(),
                                fragment.getURL()));
                        return false;
                    }
                }
            }
            sessionConfig.setCookieName(
                    temp.getSessionConfig().getCookieName());
        }

        Map<String,String> mainAttributes = getSessionConfig().getCookieAttributes();
        Map<String,String> mergedFragmentAttributes = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (WebXml fragment : fragments) {
            for (Map.Entry<String,String> attribute : fragment.getSessionConfig().getCookieAttributes().entrySet()) {
                // Skip any attribute in a fragment that is defined in the main web.xml
                if (!mainAttributes.containsKey(attribute.getKey())) {
                    if (mergedFragmentAttributes.containsKey(attribute.getKey())) {
                        // Attribute has already been seen.
                        // If values are the same, NO-OP. If they are different
                        // trigger a merge error
                        if (!mergedFragmentAttributes.get(attribute.getKey()).equals(attribute.getValue())) {
                            log.error(sm.getString(
                                    "webXml.mergeConflictSessionCookieAttributes",
                                    fragment.getName(),
                                    fragment.getURL()));
                            return false;
                        }
                    } else {
                        // First time this attribute has been seen. Add it.
                        mergedFragmentAttributes.put(attribute.getKey(), attribute.getValue());
                    }
                }
            }
        }
        mainAttributes.putAll(mergedFragmentAttributes);

        if (sessionConfig.getSessionTrackingModes().size() == 0) {
            for (WebXml fragment : fragments) {
                EnumSet<SessionTrackingMode> value =
                        fragment.getSessionConfig().getSessionTrackingModes();
                if (value.size() > 0) {
                    if (temp.getSessionConfig().getSessionTrackingModes().size() == 0) {
                        temp.getSessionConfig().getSessionTrackingModes().addAll(value);
                    } else if (value.equals(
                            temp.getSessionConfig().getSessionTrackingModes())) {
                        // Fragments use same value - no conflict
                    } else {
                        log.error(sm.getString(
                                "webXml.mergeConflictSessionTrackingMode",
                                fragment.getName(),
                                fragment.getURL()));
                        return false;
                    }
                }
            }
            sessionConfig.getSessionTrackingModes().addAll(
                    temp.getSessionConfig().getSessionTrackingModes());
        }

        for (WebXml fragment : fragments) {
            if (!mergeMap(fragment.getTaglibs(), taglibs,
                    temp.getTaglibs(), fragment, "Taglibs")) {
                return false;
            }
        }
        taglibs.putAll(temp.getTaglibs());

        for (WebXml fragment : fragments) {
            if (fragment.alwaysAddWelcomeFiles || welcomeFiles.size() == 0) {
                for (String welcomeFile : fragment.getWelcomeFiles()) {
                    addWelcomeFile(welcomeFile);
                }
            }
        }

        if (postConstructMethods.isEmpty()) {
            for (WebXml fragment : fragments) {
                if (!mergeLifecycleCallback(fragment.getPostConstructMethods(),
                        temp.getPostConstructMethods(), fragment,
                        "Post Construct Methods")) {
                    return false;
                }
            }
            postConstructMethods.putAll(temp.getPostConstructMethods());
        }

        if (preDestroyMethods.isEmpty()) {
            for (WebXml fragment : fragments) {
                if (!mergeLifecycleCallback(fragment.getPreDestroyMethods(),
                        temp.getPreDestroyMethods(), fragment,
                        "Pre Destroy Methods")) {
                    return false;
                }
            }
            preDestroyMethods.putAll(temp.getPreDestroyMethods());
        }

        return true;
    }

    // filter
    // TODO: Should support multiple description elements with language
    // TODO: Should support multiple display-name elements with language
    // TODO: Should support multiple icon elements
    // TODO: Description for init-param is ignored
    private final Map<String,FilterDef> filters = new LinkedHashMap<>();
    public void addFilter(FilterDef filter) {
        if (filters.containsKey(filter.getFilterName())) {
            // Filter names must be unique within a web(-fragment).xml
            throw new IllegalArgumentException(
                    sm.getString("webXml.duplicateFilter",
                            filter.getFilterName()));
        }
        filters.put(filter.getFilterName(), filter);
    }
    public Map<String,FilterDef> getFilters() { return filters; }
    // mime-mapping
    private final Map<String,String> mimeMappings = new HashMap<>();
    public void addMimeMapping(String extension, String mimeType) {
        mimeMappings.put(extension, mimeType);
    }
    public Map<String,String> getMimeMappings() { return mimeMappings; }

    // Optional publicId attribute
    private String publicId = null;
    public String getPublicId() { return publicId; }
    public void setPublicId(String publicId) {
        // Update major and minor version
        if (publicId == null) {
            return;
        }
        switch (publicId) {
            case XmlIdentifiers.WEB_22_PUBLIC:
                majorVersion = 2;
                minorVersion = 2;
                this.publicId = publicId;
                break;
            case XmlIdentifiers.WEB_23_PUBLIC:
                majorVersion = 2;
                minorVersion = 3;
                this.publicId = publicId;
                break;
            default:
                log.warn(sm.getString("webXml.unrecognisedPublicId", publicId));
                break;
        }
    }

    // Name of jar file
    private String jarName = null;
    public void setJarName(String jarName) { this.jarName = jarName; }
    public String getJarName() { return jarName; }

    // pre-destroy elements
    private Map<String, String> preDestroyMethods = new HashMap<>();
    public void addPreDestroyMethods(String clazz, String method) {
        if (!preDestroyMethods.containsKey(clazz)) {
            preDestroyMethods.put(clazz, method);
        }
    }
    public Map<String, String> getPreDestroyMethods() {
        return preDestroyMethods;
    }

    // Optional name element
    private String name = null;
    public String getName() { return name; }
    public void setName(String name) {
        if (ORDER_OTHERS.equalsIgnoreCase(name)) {
            // This is unusual. This name will be ignored. Log the fact.
            log.warn(sm.getString("webXml.reservedName", name));
        } else {
            this.name = name;
        }
    }

    // post-construct elements
    private Map<String, String> postConstructMethods = new HashMap<>();
    public void addPostConstructMethods(String clazz, String method) {
        if (!postConstructMethods.containsKey(clazz)) {
            postConstructMethods.put(clazz, method);
        }
    }
    public Map<String, String> getPostConstructMethods() {
        return postConstructMethods;
    }

    /**
     * Generates the sub-set of the web-fragment.xml files to be processed in
     * the order that the fragments must be processed as per the rules in the
     * Servlet spec.
     *
     * @param application    The application web.xml file
     * @param fragments      The map of fragment names to web fragments
     * @param servletContext The servlet context the fragments are associated
     *                       with
     * @return Ordered list of web-fragment.xml files to process
     */
    public static Set<WebXml> orderWebFragments(WebXml application,
                                                Map<String,WebXml> fragments, ServletContext servletContext) {
        return application.orderWebFragments(fragments, servletContext);
    }


    private Set<WebXml> orderWebFragments(Map<String,WebXml> fragments,
                                          ServletContext servletContext) {

        Set<WebXml> orderedFragments = new LinkedHashSet<>();

        boolean absoluteOrdering = getAbsoluteOrdering() != null;
        boolean orderingPresent = false;

        if (absoluteOrdering) {
            orderingPresent = true;
            // Only those fragments listed should be processed
            Set<String> requestedOrder = getAbsoluteOrdering();

            for (String requestedName : requestedOrder) {
                if (WebXml.ORDER_OTHERS.equals(requestedName)) {
                    // Add all fragments not named explicitly at this point
                    for (Map.Entry<String, WebXml> entry : fragments.entrySet()) {
                        if (!requestedOrder.contains(entry.getKey())) {
                            WebXml fragment = entry.getValue();
                            if (fragment != null) {
                                orderedFragments.add(fragment);
                            }
                        }
                    }
                } else {
                    WebXml fragment = fragments.get(requestedName);
                    if (fragment != null) {
                        orderedFragments.add(fragment);
                    } else {
                        log.warn(sm.getString("webXml.wrongFragmentName",requestedName));
                    }
                }
            }
        } else {
            // Stage 0. Check there were no fragments with duplicate names
            for (WebXml fragment : fragments.values()) {
                if (fragment.isDuplicated()) {
                    throw new IllegalArgumentException(
                            sm.getString("webXml.duplicateFragment", fragment.getName()));
                }
            }
            // Stage 1. Make all dependencies bi-directional - this makes the
            //          next stage simpler.
            for (WebXml fragment : fragments.values()) {
                Iterator<String> before =
                        fragment.getBeforeOrdering().iterator();
                while (before.hasNext()) {
                    orderingPresent = true;
                    String beforeEntry = before.next();
                    if (!beforeEntry.equals(ORDER_OTHERS)) {
                        WebXml beforeFragment = fragments.get(beforeEntry);
                        if (beforeFragment == null) {
                            before.remove();
                        } else {
                            beforeFragment.addAfterOrdering(fragment.getName());
                        }
                    }
                }
                Iterator<String> after = fragment.getAfterOrdering().iterator();
                while (after.hasNext()) {
                    orderingPresent = true;
                    String afterEntry = after.next();
                    if (!afterEntry.equals(ORDER_OTHERS)) {
                        WebXml afterFragment = fragments.get(afterEntry);
                        if (afterFragment == null) {
                            after.remove();
                        } else {
                            afterFragment.addBeforeOrdering(fragment.getName());
                        }
                    }
                }
            }

            // Stage 2. Make all fragments that are implicitly before/after
            //          others explicitly so. This is iterative so the next
            //          stage doesn't have to be.
            for (WebXml fragment : fragments.values()) {
                if (fragment.getBeforeOrdering().contains(ORDER_OTHERS)) {
                    makeBeforeOthersExplicit(fragment.getAfterOrdering(), fragments);
                }
                if (fragment.getAfterOrdering().contains(ORDER_OTHERS)) {
                    makeAfterOthersExplicit(fragment.getBeforeOrdering(), fragments);
                }
            }

            // Stage 3. Separate into three groups
            Set<WebXml> beforeSet = new HashSet<>();
            Set<WebXml> othersSet = new HashSet<>();
            Set<WebXml> afterSet = new HashSet<>();

            for (WebXml fragment : fragments.values()) {
                if (fragment.getBeforeOrdering().contains(ORDER_OTHERS)) {
                    beforeSet.add(fragment);
                    fragment.getBeforeOrdering().remove(ORDER_OTHERS);
                } else if (fragment.getAfterOrdering().contains(ORDER_OTHERS)) {
                    afterSet.add(fragment);
                    fragment.getAfterOrdering().remove(ORDER_OTHERS);
                } else {
                    othersSet.add(fragment);
                }
            }

            // Stage 4. Decouple the groups so the ordering requirements for
            //          each fragment in the group only refer to other fragments
            //          in the group. Ordering requirements outside the group
            //          will be handled by processing the groups in order.
            //          Note: Only after ordering requirements are considered.
            //                This is OK because of the processing in stage 1.
            decoupleOtherGroups(beforeSet);
            decoupleOtherGroups(othersSet);
            decoupleOtherGroups(afterSet);

            // Stage 5. Order each group
            //          Note: Only after ordering requirements are considered.
            //                This is OK because of the processing in stage 1.
            orderFragments(orderedFragments, beforeSet);
            orderFragments(orderedFragments, othersSet);
            orderFragments(orderedFragments, afterSet);
        }

        // Container fragments are always included
        Set<WebXml> containerFragments = new LinkedHashSet<>();
        // Find all the container fragments and remove any present from the
        // ordered list
        for (WebXml fragment : fragments.values()) {
            if (!fragment.getWebappJar()) {
                containerFragments.add(fragment);
                orderedFragments.remove(fragment);
            }
        }

        // Avoid NPE when unit testing
        if (servletContext != null) {
            // Publish the ordered fragments. The app does not need to know
            // about container fragments
            List<String> orderedJarFileNames = null;
            if (orderingPresent) {
                orderedJarFileNames = new ArrayList<>();
                for (WebXml fragment: orderedFragments) {
                    orderedJarFileNames.add(fragment.getJarName());
                }
            }
            servletContext.setAttribute(ServletContext.ORDERED_LIBS,
                    orderedJarFileNames);
        }

        // The remainder of the processing needs to know about container
        // fragments
        if (containerFragments.size() > 0) {
            Set<WebXml> result = new LinkedHashSet<>();
            if (containerFragments.iterator().next().getDelegate()) {
                result.addAll(containerFragments);
                result.addAll(orderedFragments);
            } else {
                result.addAll(orderedFragments);
                result.addAll(containerFragments);
            }
            return result;
        } else {
            return orderedFragments;
        }
    }

    private static void orderFragments(Set<WebXml> orderedFragments,
                                       Set<WebXml> unordered) {
        Set<WebXml> addedThisRound = new HashSet<>();
        Set<WebXml> addedLastRound = new HashSet<>();
        while (unordered.size() > 0) {
            Iterator<WebXml> source = unordered.iterator();
            while (source.hasNext()) {
                WebXml fragment = source.next();
                for (WebXml toRemove : addedLastRound) {
                    fragment.getAfterOrdering().remove(toRemove.getName());
                }
                if (fragment.getAfterOrdering().isEmpty()) {
                    addedThisRound.add(fragment);
                    orderedFragments.add(fragment);
                    source.remove();
                }
            }
            if (addedThisRound.size() == 0) {
                // Circular
                throw new IllegalArgumentException(
                        sm.getString("webXml.mergeConflictOrder"));
            }
            addedLastRound.clear();
            addedLastRound.addAll(addedThisRound);
            addedThisRound.clear();
        }
    }

    private static void decoupleOtherGroups(Set<WebXml> group) {
        Set<String> names = new HashSet<>();
        for (WebXml fragment : group) {
            names.add(fragment.getName());
        }
        for (WebXml fragment : group) {
            fragment.getAfterOrdering().removeIf(entry -> !names.contains(entry));
        }
    }

    private static void makeBeforeOthersExplicit(Set<String> beforeOrdering,
                                                 Map<String, WebXml> fragments) {
        for (String before : beforeOrdering) {
            if (!before.equals(ORDER_OTHERS)) {
                WebXml webXml = fragments.get(before);
                if (!webXml.getBeforeOrdering().contains(ORDER_OTHERS)) {
                    webXml.addBeforeOrderingOthers();
                    makeBeforeOthersExplicit(webXml.getAfterOrdering(), fragments);
                }
            }
        }
    }

    private static void makeAfterOthersExplicit(Set<String> afterOrdering,
                                                Map<String, WebXml> fragments) {
        for (String after : afterOrdering) {
            if (!after.equals(ORDER_OTHERS)) {
                WebXml webXml = fragments.get(after);
                if (!webXml.getAfterOrdering().contains(ORDER_OTHERS)) {
                    webXml.addAfterOrderingOthers();
                    makeAfterOthersExplicit(webXml.getBeforeOrdering(), fragments);
                }
            }
        }
    }

}
