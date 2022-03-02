package org.apache.tomcat.util.digester;

import java.util.ArrayList;

public class ArrayStack<E> extends ArrayList<E> {

    public E push(E item) {
        add(item);
        return item;
    }
}
