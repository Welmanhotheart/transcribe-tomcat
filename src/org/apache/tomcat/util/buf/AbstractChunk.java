package org.apache.tomcat.util.buf;

import org.apache.tomcat.util.res.StringManager;

import java.io.Serializable;

public abstract class AbstractChunk implements Cloneable, Serializable {

    private static final long serialVersionUID = 1L;
    protected static final StringManager sm = StringManager.getManager(AbstractChunk.class);


    /*
     * JVMs may limit the maximum array size to slightly less than
     * Integer.MAX_VALUE. On markt's desktop the limit is MAX_VALUE - 2.
     * Comments in the JRE source code for ArrayList and other classes indicate
     * that it may be as low as MAX_VALUE - 8 on some systems.
     */
    public static final int ARRAY_MAX_SIZE = Integer.MAX_VALUE - 8;


    private int hashCode = 0;
    protected boolean hasHashCode = false;

    protected boolean isSet;

    private int limit = -1;

    protected int start;
    protected int end;

    /**
     * Resets the chunk to an uninitialized state.
     */
    public void recycle() {
        hasHashCode = false;
        isSet = false;
        start = 0;
        end = 0;
    }

    /**
     * Maximum amount of data in this buffer. If -1 or not set, the buffer will
     * grow to {{@link #ARRAY_MAX_SIZE}. Can be smaller than the current buffer
     * size ( which will not shrink ). When the limit is reached, the buffer
     * will be flushed (if out is set) or throw exception.
     *
     * @param limit The new limit
     */
    public void setLimit(int limit) {
        this.limit = limit;
    }


    public int getLimit() {
        return limit;
    }


    protected int getLimitInternal() {
        if (limit > 0) {
            return limit;
        } else {
            return ARRAY_MAX_SIZE;
        }
    }

    public int getEnd() {
        return end;
    }


    public void setEnd(int i) {
        end = i;
    }


    public boolean isNull() {
        if (end > 0) {
            return false;
        }
        return !isSet;
    }

    /**
     * @return the length of the data in the buffer
     */
    public int getLength() {
        return end - start;
    }


    protected abstract int getBufferElement(int index);

    /**
     * @return the start position of the data in the buffer
     */
    public int getStart() {
        return start;
    }


}
