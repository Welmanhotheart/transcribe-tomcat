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

    public E peek(int n) throws EmptyStackException {
        int m = (size() - n) - 1;
        if (m < 0) {
            throw new EmptyStackException();
        } else {
            return get(m);
        }
    }

    /**
     * Return <code>true</code> if this stack is currently empty.
     * <p>
     * This method exists for compatibility with <code>java.util.Stack</code>.
     * New users of this class should use <code>isEmpty</code> instead.
     *
     * @return true if the stack is currently empty
     */
    public boolean empty() {
        return isEmpty();
    }

    /**
     * Pops the top item off of this stack and return it.
     *
     * @return the top item on the stack
     * @throws EmptyStackException  if the stack is empty
     */
    public E pop() throws EmptyStackException {
        int n = size();
        if (n <= 0) {
            throw new EmptyStackException();
        } else {
            return remove(n - 1);
        }
    }

}
