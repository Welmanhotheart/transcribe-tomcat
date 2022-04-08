package org.apache.tomcat.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultiThrowable extends Throwable{
    private static final long serialVersionUID = 1L;

    private List<Throwable> throwables = new ArrayList<>();

    /**
     * Add a throwable to the list of wrapped throwables.
     *
     * @param t The throwable to add
     */
    public void add(Throwable t) {
        throwables.add(t);
    }

    /**
     * @return A read-only list of the wrapped throwables.
     */
    public List<Throwable> getThrowables() {
        return Collections.unmodifiableList(throwables);
    }


    /**
     * @return {@code null} if there are no wrapped throwables, the Throwable if
     *         there is a single wrapped throwable or the current instance of
     *         there are multiple wrapped throwables
     */
    public Throwable getThrowable() {
        if (size() == 0) {
            return null;
        } else if (size() == 1) {
            return throwables.get(0);
        } else {
            return this;
        }
    }

    /**
     * @return The number of throwables currently wrapped by this instance.
     */
    public int size() {
        return throwables.size();
    }



}
