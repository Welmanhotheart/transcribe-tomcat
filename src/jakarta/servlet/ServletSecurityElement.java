package jakarta.servlet;

import jakarta.servlet.annotation.HttpMethodConstraint;
import jakarta.servlet.annotation.ServletSecurity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ServletSecurityElement extends HttpConstraintElement {


    /**
     * Create from an annotation.
     * @param annotation Annotation to use as the basis for the new instance
     * @throws IllegalArgumentException if a method name is specified more than
     * once
     */
    public ServletSecurityElement(ServletSecurity annotation) {
        this(new HttpConstraintElement(annotation.value().value(),
                annotation.value().transportGuarantee(),
                annotation.value().rolesAllowed()));

        List<HttpMethodConstraintElement> l = new ArrayList<>();
        HttpMethodConstraint[] constraints = annotation.httpMethodConstraints();
        if (constraints != null) {
            for (HttpMethodConstraint constraint : constraints) {
                HttpMethodConstraintElement e =
                        new HttpMethodConstraintElement(constraint.value(),
                                new HttpConstraintElement(
                                        constraint.emptyRoleSemantic(),
                                        constraint.transportGuarantee(),
                                        constraint.rolesAllowed()));
                l.add(e);
            }
        }
        addHttpMethodConstraints(l);
    }


    private void addHttpMethodConstraints(
            Collection<HttpMethodConstraintElement> httpMethodConstraints) {
        if (httpMethodConstraints == null) {
            return;
        }
        for (HttpMethodConstraintElement constraint : httpMethodConstraints) {
            String method = constraint.getMethodName();
            if (methodConstraints.containsKey(method)) {
                throw new IllegalArgumentException(
                        "Duplicate method name: " + method);
            }
            methodConstraints.put(method, constraint);
        }
    }

}
