package org.apache.tomcat.util.digester;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RulesBase implements Rules{
    protected ArrayList<Rule> rules = new ArrayList<>();
    protected Digester digester;

    protected HashMap<String, List<Rule>> cache = new HashMap<>();


    @Override
    public void add(String pattern, Rule rule) {
        int patternLength = pattern.length();
        if (patternLength > 1 && pattern.endsWith("/")) {
            pattern = pattern.substring(0, patternLength - 1);
        }
        List<Rule> list = cache.get(pattern);
        if (list == null) {
            list = new ArrayList<>();
            cache.put(pattern, list);
        }
        list.add(rule);
        //Why here has add too , TODO
        rules.add(rule);
        if (this.digester != null) {
            rule.setDigester(this.digester);
        }
    }

    @Override
    public void setDigester(Digester digester) {
        this.digester = digester;
        for (Rule item : rules) {
            item.setDigester(digester);
        }
    }

    @Override
    public List<Rule> match(String namespaceURI, String pattern) {
        // List rulesList = (List) this.cache.get(pattern);
        List<Rule> rulesList = lookup(namespaceURI, pattern);
        if ((rulesList == null) || (rulesList.size() < 1)) {
            // Find the longest key, ie more discriminant
            String longKey = "";
            for (String key : this.cache.keySet()) {
                if (key.startsWith("*/")) {
                    if (pattern.equals(key.substring(2)) ||
                            pattern.endsWith(key.substring(1))) {
                        if (key.length() > longKey.length()) {
                            // rulesList = (List) this.cache.get(key);
                            rulesList = lookup(namespaceURI, key);
                            longKey = key;
                        }
                    }
                }
            }
        }
        if (rulesList == null) {
            rulesList = new ArrayList<>();
        }
        return rulesList;
    }

    protected List<Rule> lookup(String namespaceURI, String pattern) {
        // Optimize when no namespace URI is specified
        List<Rule> list = this.cache.get(pattern);
        if (list == null) {
            return null;
        }
        if ((namespaceURI == null) || (namespaceURI.length() == 0)) {
            return list;
        }

        // Select only Rules that match on the specified namespace URI
        List<Rule> results = new ArrayList<>();
        for (Rule item : list) {
            if ((namespaceURI.equals(item.getNamespaceURI())) ||
                    (item.getNamespaceURI() == null)) {
                results.add(item);
            }
        }
        return results;
    }

}
