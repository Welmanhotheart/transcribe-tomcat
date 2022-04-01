package org.apache.tomcat.util.digester;

import org.apache.catalina.startup.Catalina;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.tomcat.util.IntrospectionUtils.PropertySource;
import org.apache.tomcat.util.res.StringManager;
import org.xml.sax.*;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.ext.EntityResolver2;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class Digester extends DefaultHandler2 {
    protected static final StringManager sm = StringManager.getManager(Digester.class);
    private static GeneratedCodeLoader generatedCodeLoader;
    /**
     * The Log to which most logging calls will be made.
     */
    protected Log log = LogFactory.getLog(Digester.class);


    protected ArrayStack<Object> stack = new ArrayStack<>();
    /**
     * The Locator associated with our parser.
     */
    protected Locator locator = null;

    private Object root = null;
    private Rules rules;
    /**
     * Generated code.
     */
    protected StringBuilder code = null;
    protected ArrayList<Object> known = new ArrayList<>();

    /**
     * The current match pattern for nested element processing.
     */
    protected String match = "";

    protected ArrayStack<List<Rule>> matches = new ArrayStack<>(10);


    /**
     * The Log to which all SAX event related logging calls will be made.
     */
    protected Log saxLog = LogFactory.getLog("org.apache.tomcat.util.digester.Digester.sax");

    /**
     * Has this Digester been configured yet.TODO
     */
    protected boolean configured = false;

    /**
     * The XMLReader used to parse digester rules.
     */
    protected XMLReader reader = null;


    /**
     * The SAXParserFactory that is created the first time we need it.
     */
    protected SAXParserFactory factory = null;


    /**
     * Do we want a "namespace aware" parser.TODO
     */
    protected boolean namespaceAware = false;

    /**
     * The body text of the current element.
     */
    protected StringBuilder bodyText = new StringBuilder();


    /**
     * The stack of body text string buffers for surrounding elements.
     */
    protected ArrayStack<StringBuilder> bodyTexts = new ArrayStack<>();

    /**
     * TODO what is validating here TODO
     */
    private boolean validating;

    /**
     * Warn on missing attributes and elements.
     */
    protected boolean rulesValidation = false;



    /**
     * Do we want to use the Context ClassLoader when loading classes
     * for instantiating new objects.  Default is <code>false</code>.
     */
    protected boolean useContextClassLoader = false;


    protected EntityResolver entityResolver;
    protected PropertySource[] source;
    protected ClassLoader classLoader;
    protected SAXParser parser;

    /**
     * Fake attributes map (attributes are often used for object creation).
     */
    protected Map<Class<?>, List<String>> fakeAttributes = null;


    public static boolean isGeneratedCodeLoaderSet() {
        return false;

    }


    public static void setGeneratedCodeLoader(GeneratedCodeLoader loader) {

    }

    public static Object loadGeneratedClass(String className) {
        if (generatedCodeLoader != null) {
            return generatedCodeLoader.loadGeneratedCode(className);
        }
        return null;

    }

    public void push(Object object) {
        if (stack.size() == 0) {
            root = object;
        }
        stack.push(object);
    }

    public void startGeneratingCode() {

    }

    public Object parse(InputSource inputSource) throws SAXException, IOException {
        configure();
        // here read do the parsing process
        getXMLReader().parse(inputSource);
        return root;
    }

    /**
     * what does this method mean here? TODO
     */
    private void configure() {
        // Do not configure more than once
        if (configured) {
            return;
        }

        log = LogFactory.getLog("org.apache.tomcat.util.digester.Digester");
        saxLog = LogFactory.getLog("org.apache.tomcat.util.digester.Digester.sax");

        // Set the configuration flag to avoid repeating
        configured = true;
    }

    /**
     * what does here mean?
     * @param b
     */
    public void setValidating(boolean b) {

    }

    /**
     * @return the rules validation flag.
     */
    public boolean getRulesValidation() {
        return this.rulesValidation;
    }

    /**
     * what does here mean? TODO
     * @param b
     */
    public void setRulesValidation(boolean b) {

    }

    /**
     * Set the fake attributes.
     *
     * @param fakeAttributes The new fake attributes.
     */
    public void setFakeAttributes(Map<Class<?>, List<String>> fakeAttributes) {

        this.fakeAttributes = fakeAttributes;

    }

    public XMLReader getXMLReader() throws SAXException {
        if (reader == null) {
            reader = getParser().getXMLReader();
        }
        // what is dtdHandler here what is dtd TODO
        reader.setDTDHandler(this);
        //what id content handler here , TODO
        reader.setContentHandler(this);

        EntityResolver entityResolver = getEntityResolver();
        if (entityResolver == null) {
            entityResolver = this;
        }

        // Wrap the resolver so we can perform ${...} property replacement
        if (entityResolver instanceof EntityResolver2) {
            entityResolver = new EntityResolver2Wrapper((EntityResolver2) entityResolver, source, classLoader);
        } else {
            entityResolver = new EntityResolverWrapper(entityResolver, source, classLoader);
        }

        reader.setEntityResolver(entityResolver);

        //what does this code mean here ?
        reader.setProperty("http://xml.org/sax/properties/lexical-handler", this);

        reader.setErrorHandler(this);
        return reader;
    }

    /**
     * Set the <code>EntityResolver</code> used by SAX when resolving
     * public id and system id.
     * This must be called before the first call to <code>parse()</code>.
     * @param entityResolver a class that implement the <code>EntityResolver</code> interface.
     */
    public void setEntityResolver(EntityResolver entityResolver) {
        this.entityResolver = entityResolver;
    }

    /**
     * here is public , doubt TODO
     * @return
     */
    public EntityResolver getEntityResolver() {
        return entityResolver;
    }

    private SAXParser getParser() {
        // Return the parser we already created (if any)
        if (parser != null) {
            return parser;
        }
        try {
            parser = getFactory().newSAXParser();
        } catch (Exception e) {
            log.error(sm.getString("digester.createParserError"), e);
            return null;
        }

        return parser;
    }

    private SAXParserFactory getFactory() throws SAXNotSupportedException, SAXNotRecognizedException, ParserConfigurationException {
        if (factory == null) {
            factory = SAXParserFactory.newInstance();

            //TODO here what is namespaceAware TODO
            factory.setNamespaceAware(namespaceAware);
            // Preserve xmlns attributes
            if (namespaceAware) {
                factory.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
            }

            factory.setValidating(validating);
            if (validating) {
                // Enable DTD validation
                factory.setFeature("http://xml.org/sax/features/validation", true);
                // Enable schema validation
                factory.setFeature("http://apache.org/xml/features/validation/schema", true);
            }
        }
        return factory;
    }

    public void setUseContextClassLoader(boolean b) {

    }

    public void addObjectCreate(String patter, String className, String attributeName) {
        addRule(patter, new ObjectCreateRule(className, attributeName));
    }

    public void addObjectCreate(String pattern, String className) {

        addRule(pattern, new ObjectCreateRule(className));

    }

    public void addSetProperties(String pattern) {
        addRule(pattern, new SetPropertiesRule());
    }

    public void addSetProperties(String pattern, String[] excludes) {

        addRule(pattern, new SetPropertiesRule(excludes));

    }

    public void addRule(String pattern, Rule rule) {
        rule.setDigester(this);
        getRules().add(pattern, rule);
    }

    private Rules getRules() {
        if (this.rules == null) {
            this.rules = new RulesBase();
            this.rules.setDigester(this);
        }
        return this.rules;
    }

    public void addRuleSet(RuleSet ruleSet) {
        ruleSet.addRuleInstances(this);
    }

    public void addSetNext(String pattern, String methodName, String paramType) {
        addRule(pattern, new SetNextRule(methodName, paramType));
    }

    /**
     * TODO what does here mean?
     */
    public interface GeneratedCodeLoader {
        Object loadGeneratedCode(String className);
    }


    public Log getLogger() {

        return log;

    }

    public Object peek() {
        try {
            return stack.peek();
        } catch (EmptyStackException e) {
            log.warn(sm.getString("digester.emptyStack"));
            return null;
        }
    }

    public Object peek(int n) {
        try {
            return stack.peek(n);
        } catch (EmptyStackException e) {
            log.warn(sm.getString("digester.emptyStack"));
            return null;
        }
    }

    /**
     * what here generate code
     * @return
     */
    public StringBuilder getGeneratedCode() {
        return code;
    }

    /**
     * what here
     * @param object
     * @return
     */
    public String toVariableName(Object object) {
        boolean found = false;
        int pos = 0;
        if (known.size() > 0) {
            for (int i = known.size() - 1; i >= 0; i--) {
                if (known.get(i) == object) {
                    pos = i;
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            pos = known.size();
            known.add(object);
        }
        return "tc_" + object.getClass().getSimpleName() + "_" + String.valueOf(pos);
    }


    private static class EntityResolverWrapper implements EntityResolver {

        private final EntityResolver entityResolver;
        private final PropertySource[] source;
        private final ClassLoader classLoader;

        public EntityResolverWrapper(EntityResolver entityResolver, PropertySource[] source, ClassLoader classLoader) {
            this.entityResolver = entityResolver;
            this.source = source;
            this.classLoader = classLoader;
        }

        @Override
        public InputSource resolveEntity(String publicId, String systemId)
                throws SAXException, IOException {
            publicId = replace(publicId);
            systemId = replace(systemId);
            return entityResolver.resolveEntity(publicId, systemId);
        }

        protected String replace(String input) {
            try {
                return IntrospectionUtils.replaceProperties(input, null, source, classLoader);
            } catch (Exception e) {
                return input;
            }
        }
    }

    private static class EntityResolver2Wrapper extends EntityResolverWrapper implements EntityResolver2 {

        private final EntityResolver2 entityResolver2;

        public EntityResolver2Wrapper(EntityResolver2 entityResolver, PropertySource[] source,
                                      ClassLoader classLoader) {
            super(entityResolver, source, classLoader);
            this.entityResolver2 = entityResolver;
        }

        @Override
        public InputSource getExternalSubset(String name, String baseURI)
                throws SAXException, IOException {
            name = replace(name);
            baseURI = replace(baseURI);
            return entityResolver2.getExternalSubset(name, baseURI);
        }

        @Override
        public InputSource resolveEntity(String name, String publicId, String baseURI,
                                         String systemId) throws SAXException, IOException {
            name = replace(name);
            publicId = replace(publicId);
            baseURI = replace(baseURI);
            systemId = replace(systemId);
            return entityResolver2.resolveEntity(name, publicId, baseURI, systemId);
        }
    }

    public SAXException createSAXException(Exception e) {
        if (e instanceof InvocationTargetException) {
            Throwable t = e.getCause();
            if (t instanceof ThreadDeath) {
                throw (ThreadDeath) t;
            }
            if (t instanceof VirtualMachineError) {
                throw (VirtualMachineError) t;
            }
            if (t instanceof Exception) {
                e = (Exception) t;
            }
        }
        return createSAXException(e.getMessage(), e);
    }

    public SAXException createSAXException(String message, Exception e) {
        if ((e != null) && (e instanceof InvocationTargetException)) {
            Throwable t = e.getCause();
            if (t instanceof ThreadDeath) {
                throw (ThreadDeath) t;
            }
            if (t instanceof VirtualMachineError) {
                throw (VirtualMachineError) t;
            }
            if (t instanceof Exception) {
                e = (Exception) t;
            }
        }
        if (locator != null) {
            String error = sm.getString("digester.errorLocation",
                    Integer.valueOf(locator.getLineNumber()),
                    Integer.valueOf(locator.getColumnNumber()), message);
            if (e != null) {
                return new SAXParseException(error, locator, e);
            } else {
                return new SAXParseException(error, locator);
            }
        }
        log.error(sm.getString("digester.noLocator"));
        if (e != null) {
            return new SAXException(message, e);
        } else {
            return new SAXException(message);
        }
    }

    /**
     * Create a SAX exception which also understands about the location in
     * the digester file where the exception occurs
     * @param message The error message
     * @return the new exception
     */
    public SAXException createSAXException(String message) {
        return createSAXException(message, null);
    }

    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes list)
            throws SAXException {
        boolean debug = log.isDebugEnabled();

        if (saxLog.isDebugEnabled()) {
            saxLog.debug("startElement(" + namespaceURI + "," + localName + "," + qName + ")");
        }

        // Parse system properties
        list = updateAttributes(list);

        // Save the body text accumulated for our surrounding element
        bodyTexts.push(bodyText);
        bodyText = new StringBuilder();

        // the actual element name is either in localName or qName, depending
        // on whether the parser is namespace aware
        String name = localName;
        if ((name == null) || (name.length() < 1)) {
            name = qName;
        }

        // Compute the current matching rule
        StringBuilder sb = new StringBuilder(match);
        if (match.length() > 0) {
            sb.append('/');
        }
        sb.append(name);
        match = sb.toString();
        if (debug) {
            log.debug("  New match='" + match + "'");
        }
        // Fire "begin" events for all relevant rules
        List<Rule> rules = getRules().match(namespaceURI, match);
        matches.push(rules);//why here it needs to push the rules TODO
        if ((rules != null) && (rules.size() > 0)) {
            for (Rule value : rules) {
                try {
                    Rule rule = value;
                    if (debug) {
                        log.debug("  Fire begin() for " + rule);
                    }
                    rule.begin(namespaceURI, name, list);
                } catch (Exception e) {
                    log.error(sm.getString("digester.error.begin"), e);
                    throw createSAXException(e);
                } catch (Error e) {
                    log.error(sm.getString("digester.error.begin"), e);
                    throw e;
                }
            }
        } else {
            if (debug) {
                log.debug(sm.getString("digester.noRulesFound", match));
            }
        }

    }

    /**
     * Process notification of the end of an XML element being reached.
     *
     * @param namespaceURI - The Namespace URI, or the empty string if the
     *   element has no Namespace URI or if Namespace processing is not
     *   being performed.
     * @param localName - The local name (without prefix), or the empty
     *   string if Namespace processing is not being performed.
     * @param qName - The qualified XML 1.0 name (with prefix), or the
     *   empty string if qualified names are not available.
     * @exception SAXException if a parsing error is to be reported
     */
    @Override
    public void endElement(String namespaceURI, String localName, String qName)
            throws SAXException {

        boolean debug = log.isDebugEnabled();

        if (debug) {
            if (saxLog.isDebugEnabled()) {
                saxLog.debug("endElement(" + namespaceURI + "," + localName + "," + qName + ")");
            }
            log.debug("  match='" + match + "'");
            log.debug("  bodyText='" + bodyText + "'");
        }

        // Parse system properties
        bodyText = updateBodyText(bodyText);

        // the actual element name is either in localName or qName, depending
        // on whether the parser is namespace aware
        String name = localName;
        if ((name == null) || (name.length() < 1)) {
            name = qName;
        }

        // Fire "body" events for all relevant rules
        List<Rule> rules = matches.pop();
        if ((rules != null) && (rules.size() > 0)) {
            String bodyText = this.bodyText.toString().intern();
            for (Rule value : rules) {
                try {
                    Rule rule = value;
                    if (debug) {
                        log.debug("  Fire body() for " + rule);
                    }
                    rule.body(namespaceURI, name, bodyText);
                } catch (Exception e) {
                    log.error(sm.getString("digester.error.body"), e);
                    throw createSAXException(e);
                } catch (Error e) {
                    log.error(sm.getString("digester.error.body"), e);
                    throw e;
                }
            }
        } else {
            if (debug) {
                log.debug(sm.getString("digester.noRulesFound", match));
            }
            if (rulesValidation) {
                log.warn(sm.getString("digester.noRulesFound", match));
            }
        }

        // Recover the body text from the surrounding element
        bodyText = bodyTexts.pop();

        // Fire "end" events for all relevant rules in reverse order
        if (rules != null) {
            for (int i = 0; i < rules.size(); i++) {
                int j = (rules.size() - i) - 1;
                try {
                    Rule rule = rules.get(j);
                    if (debug) {
                        log.debug("  Fire end() for " + rule);
                    }
                    rule.end(namespaceURI, name);
                } catch (Exception e) {
                    log.error(sm.getString("digester.error.end"), e);
                    throw createSAXException(e);
                } catch (Error e) {
                    log.error(sm.getString("digester.error.end"), e);
                    throw e;
                }
            }
        }

        // Recover the previous match expression
        int slash = match.lastIndexOf('/');
        if (slash >= 0) {
            match = match.substring(0, slash);
        } else {
            match = "";
        }

    }






    private Attributes updateAttributes(Attributes list) {

        if (list.getLength() == 0) {
            return list;
        }

        AttributesImpl newAttrs = new AttributesImpl(list);
        int nAttributes = newAttrs.getLength();
        for (int i = 0; i < nAttributes; ++i) {
            String value = newAttrs.getValue(i);
            try {
                newAttrs.setValue(i, IntrospectionUtils.replaceProperties(value, null, source, getClassLoader()).intern());
            } catch (Exception e) {
                log.warn(sm.getString("digester.failedToUpdateAttributes", newAttrs.getLocalName(i), value), e);
            }
        }

        return newAttrs;
    }


    /**
     * Return a new StringBuilder containing the same contents as the
     * input buffer, except that data of form ${varname} have been
     * replaced by the value of that var as defined in the system property.
     */
    private StringBuilder updateBodyText(StringBuilder bodyText) {
        String in = bodyText.toString();
        String out;
        try {
            out = IntrospectionUtils.replaceProperties(in, null, source, getClassLoader());
        } catch (Exception e) {
            return bodyText; // return unchanged data
        }

        if (out == in) {
            // No substitutions required. Don't waste memory creating
            // a new buffer
            return bodyText;
        } else {
            return new StringBuilder(out);
        }
    }


    public ClassLoader getClassLoader() {
        if (this.classLoader != null) {
            return this.classLoader;
        }
        if (this.useContextClassLoader) {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader != null) {
                return classLoader;
            }
        }
        return this.getClass().getClassLoader();
    }


    /**
     * Determine if an attribute is a fake attribute.
     * @param object The object
     * @param name The attribute name
     * @return <code>true</code> if this is a fake attribute
     */
    public boolean isFakeAttribute(Object object, String name) {
        if (fakeAttributes == null) {
            return false;
        }
        List<String> result = fakeAttributes.get(object.getClass());
        if (result == null) {
            result = fakeAttributes.get(Object.class);
        }
        if (result == null) {
            return false;
        } else {
            return result.contains(name);
        }
    }


    /**
     * Pop the top object off of the stack, and return it.  If there are
     * no objects on the stack, return <code>null</code>.
     * @return the top object
     */
    public Object pop() {
        try {
            return stack.pop();
        } catch (EmptyStackException e) {
            log.warn(sm.getString("digester.emptyStack"));
            return null;
        }
    }

}
