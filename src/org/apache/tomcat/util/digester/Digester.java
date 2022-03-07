package org.apache.tomcat.util.digester;

import org.apache.catalina.startup.Catalina;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.tomcat.util.res.StringManager;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.ext.EntityResolver2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;

public class Digester extends DefaultHandler2 {
    protected static final StringManager sm = StringManager.getManager(Digester.class);
    private static GeneratedCodeLoader generatedCodeLoader;
    protected ArrayStack<Object> stack = new ArrayStack<>();
    private Object root = null;
    private Rules rules;
    /**
     * Generated code.
     */
    protected StringBuilder code = null;
    protected ArrayList<Object> known = new ArrayList<>();

    protected Log log = LogFactory.getLog(Digester.class);

    /**
     * The Log to which all SAX event related logging calls will be made.
     */
    protected Log saxLog = LogFactory.getLog("org.apache.tomcat.util.digester.Digester.sax");

    /**
     * Has this Digester been configured yet.
     */
    protected boolean configured = false;


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

    public Object parse(InputSource inputSource) {
        configure();
        getXMLReader().parse(inputSource);
        return root;
    }

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
     * what does here mean? TODO
     * @param b
     */
    public void setRulesValidation(boolean b) {

    }

    public void setFakeAttributes(HashMap<Class<?>, List<String>> fakeAttributes) {

    }

    public XMLReader getXMLReader() throws SAXException {
        if (reader == null) {
            reader = getParser().getXMLReader();
        }

        reader.setDTDHandler(this);
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

        reader.setProperty("http://xml.org/sax/properties/lexical-handler", this);

        reader.setErrorHandler(this);
        return reader;
    }

    public void setUseContextClassLoader(boolean b) {

    }

    public void addObjectCreate(String patter, String className, String attributeName) {

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

    public void addSetNext(String pattern, String methodName, String paramType) {
        addRule(pattern, new SetNextRule(methodName, paramType));
    }

    public void addRuleSet(RuleSet ruleSet) {
        ruleSet.addRuleInstances(this);
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

}
