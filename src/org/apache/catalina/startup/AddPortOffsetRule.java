package org.apache.catalina.startup;

import org.apache.catalina.Server;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.digester.Rule;
import org.xml.sax.Attributes;

public class AddPortOffsetRule extends Rule {
    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {

        Connector conn = (Connector) digester.peek();
        Server server = (Server) digester.peek(2);

        int portOffset = server.getPortOffset();
        conn.setPortOffset(portOffset);

        StringBuilder code = digester.getGeneratedCode();
        if (code != null) {
            code.append(digester.toVariableName(conn)).append(".setPortOffset(");
            code.append(digester.toVariableName(server)).append(".getPortOffset());");
            code.append(System.lineSeparator());
        }
    }
}
