package org.apache.tomcat.util.buf;

public class CharChunk extends AbstractChunk implements CharSequence {



    protected int start;
    protected int end;

    // char[]
    private char[] buff;


    // TODO: Deprecate offset and use start

    public int getOffset() {
        return start;
    }


    public void setOffset(int off) {
        if (end < off) {
            end = off;
        }
        start = off;
    }


    /**
     * @return the start position of the data in the buffer
     */
    public int getStart() {
        return start;
    }


    public int getEnd() {
        return end;
    }


    public void setEnd(int i) {
        end = i;
    }


    /**
     * @return the buffer.
     */
    public char[] getBuffer() {
        return buff;
    }


    @Override
    public int length() {
        return end - start;
    }


    // Char sequence impl

    @Override
    public char charAt(int index) {
        return buff[index + start];
    }


    @Override
    public CharSequence subSequence(int start, int end) {
        try {
            CharChunk result = (CharChunk) this.clone();
            result.setOffset(this.start + start);
            result.setEnd(this.start + end);
            return result;
        } catch (CloneNotSupportedException e) {
            // Cannot happen
            return null;
        }
    }
}
