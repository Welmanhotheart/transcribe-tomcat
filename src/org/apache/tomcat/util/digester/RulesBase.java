package org.apache.tomcat.util.digester;

import java.util.ArrayList;

public class RulesBase implements Rules{
    protected ArrayList<Rule> rules = new ArrayList<>();
    private Digester digester;

    @Override
    public void add(String pattern, Rule rule) {

    }

    @Override
    public void setDigester(Digester digester) {
        this.digester = digester;
        for (Rule item : rules) {
            item.setDigester(digester);
        }
    }
}
