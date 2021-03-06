package org.apache.catalina.startup;

import org.apache.tomcat.util.digester.Rule;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;
import org.apache.tomcat.util.net.SSLHostConfigCertificate.Type;
import org.xml.sax.Attributes;

public class CertificateCreateRule extends Rule {
    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        SSLHostConfig sslHostConfig = (SSLHostConfig) digester.peek();

        SSLHostConfigCertificate.Type type;
        String typeValue = attributes.getValue("type");
        if (typeValue == null || typeValue.length() == 0) {
            type = Type.UNDEFINED;
        } else {
            type = Type.valueOf(typeValue);
        }

        SSLHostConfigCertificate certificate = new SSLHostConfigCertificate(sslHostConfig, type);

        digester.push(certificate);

        StringBuilder code = digester.getGeneratedCode();
        if (code != null) {
            code.append(SSLHostConfigCertificate.class.getName()).append(' ').append(digester.toVariableName(certificate));
            code.append(" = new ").append(SSLHostConfigCertificate.class.getName());
            code.append('(').append(digester.toVariableName(sslHostConfig));
            code.append(", ").append(Type.class.getName().replace('$', '.')).append('.').append(type).append(");");
            code.append(System.lineSeparator());
        }
    }


    /**
     * Process the end of this element.
     *
     * @param namespace the namespace URI of the matching element, or an
     *   empty string if the parser is not namespace aware or the element has
     *   no namespace
     * @param name the local name if the parser is namespace aware, or just
     *   the element name otherwise
     */
    @Override
    public void end(String namespace, String name) throws Exception {
        digester.pop();
    }
}
