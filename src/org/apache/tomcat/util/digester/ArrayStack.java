package org.apache.tomcat.util.digester;

import java.util.ArrayList;
import java.util.EmptyStackException;

public class ArrayStack<E> extends ArrayList<E> {

    public ArrayStack() {
        super();
    }

    public ArrayStack(int initialSize) {
        super(initialSize);
    }

    public E push(E item) {
        add(item);
        return item;
    }

    public E peek() throws EmptyStackException {
        int n = size();
        if (n <= 0) {
            throw new EmptyStackException();
        } else {
            return get(n - 1);
        }
    }
}
