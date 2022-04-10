package org.apache.tomcat.util.descriptor.web;

import jakarta.servlet.ServletContext;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.descriptor.XmlIdentifiers;
import org.apache.tomcat.util.digester.DocumentProperties;
import org.apache.tomcat.util.res.StringManager;

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
