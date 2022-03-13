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
}
