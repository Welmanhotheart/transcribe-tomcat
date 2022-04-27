package org.apache.tomcat.util.buf;

import java.io.Serializable;

/***
 * TODO
 * @author <a href="zhiwei.wei@bintools.cn">zhiwei.wei</a>
 * @version 1.0.0 2022-04-2022/4/27-下午3:26
 */
public final class MessageBytes implements Cloneable, Serializable {

    // primary type ( whatever is set as original value )
    private int type = T_NULL;

    public static final int T_NULL = 0;
    /** getType() is T_STR if the the object used to create the MessageBytes
     was a String */
    public static final int T_STR  = 1;
    /** getType() is T_BYTES if the the object used to create the MessageBytes
     was a byte[] */
    public static final int T_BYTES = 2;
    /** getType() is T_CHARS if the the object used to create the MessageBytes
     was a char[] */
    public static final int T_CHARS = 3;

    // Internal objects to represent array + offset, and specific methods
    private final ByteChunk byteC=new ByteChunk();
    private final CharChunk charC=new CharChunk();
    // true if a String value was computed. Probably not needed,
    // strValue!=null is the same
    private boolean hasStrValue=false;

    // did we compute the hashcode ?
    private boolean hasHashCode=false;
    private boolean hasLongValue=false;
    // String
    private String strValue;

    /**
     * Construct a new MessageBytes instance.
     * @return the instance
     */
    public static MessageBytes newInstance() {
        return factory.newInstance();
    }

    /**
     * Compares the message bytes to the specified String object.
     * @param s the String to compare
     * @return <code>true</code> if the comparison succeeded, <code>false</code> otherwise
     */
    public boolean equalsIgnoreCase(String s) {
        switch (type) {
            case T_STR:
                if (strValue == null) {
                    return s == null;
                }
                return strValue.equalsIgnoreCase( s );
            case T_CHARS:
                return charC.equalsIgnoreCase( s );
            case T_BYTES:
                return byteC.equalsIgnoreCase( s );
            default:
                return false;
        }
    }

    /**
     * Resets the message bytes to an uninitialized (NULL) state.
     */
    public void recycle() {
        type=T_NULL;
        byteC.recycle();
        charC.recycle();

        strValue=null;

        hasStrValue=false;
        hasHashCode=false;
        hasLongValue=false;
    }


    // -------------------- Future may be different --------------------

    private static final MessageBytesFactory factory=new MessageBytesFactory();

    private static class MessageBytesFactory {
        protected MessageBytesFactory() {
        }
        public MessageBytes newInstance() {
            return new MessageBytes();
        }
    }

}