package org.apache.tomcat.util.buf;

public class CharChunk extends AbstractChunk implements CharSequence {



    protected int start;
    protected int end;

    // char[]
    private char[] buff;


    /**
     * @return the buffer.
     */
    public char[] getChars() {
        return getBuffer();
    }



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

    @Override
    protected int getBufferElement(int index) {
        return buff[index];
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

    /**
     * Compares the message bytes to the specified String object.
     *
     * @param s the String to compare
     * @return <code>true</code> if the comparison succeeded, <code>false</code>
     *         otherwise
     */
    public boolean equalsIgnoreCase(String s) {
        char[] c = buff;
        int len = end - start;
        if (c == null || len != s.length()) {
            return false;
        }
        int off = start;
        for (int i = 0; i < len; i++) {
            if (Ascii.toLower(c[off++]) != Ascii.toLower(s.charAt(i))) {
                return false;
            }
        }
        return true;
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
