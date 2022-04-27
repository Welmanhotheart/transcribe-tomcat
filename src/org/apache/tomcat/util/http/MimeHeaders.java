/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomcat.util.http;

import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.res.StringManager;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;

/**
 * This class is used to contain standard internet message headers,
 * used for SMTP (RFC822) and HTTP (RFC2068) messages as well as for
 * MIME (RFC 2045) applications such as transferring typed data and
 * grouping related items in multipart message bodies.
 *
 * <P> Message headers, as specified in RFC822, include a field name
 * and a field body.  Order has no semantic significance, and several
 * fields with the same name may exist.  However, most fields do not
 * (and should not) exist more than once in a header.
 *
 * <P> Many kinds of field body must conform to a specified syntax,
 * including the standard parenthesized comment syntax.  This class
 * supports only two simple syntaxes, for dates and integers.
 *
 * <P> When processing headers, care must be taken to handle the case of
 * multiple same-name fields correctly.  The values of such fields are
 * only available as strings.  They may be accessed by index (treating
 * the header as an array of fields), or by name (returning an array
 * of string values).
 */

/* Headers are first parsed and stored in the order they are
   received. This is based on the fact that most servlets will not
   directly access all headers, and most headers are single-valued.
   ( the alternative - a hash or similar data structure - will add
   an overhead that is not needed in most cases )

   Apache seems to be using a similar method for storing and manipulating
   headers.

   Future enhancements:
   - hash the headers the first time a header is requested ( i.e. if the
   servlet needs direct access to headers).
   - scan "common" values ( length, cookies, etc ) during the parse
   ( addHeader hook )

*/


/**
 *  Memory-efficient repository for Mime Headers. When the object is recycled, it
 *  will keep the allocated headers[] and all the MimeHeaderField - no GC is generated.
 *
 *  For input headers it is possible to use the MessageByte for Fields - so no GC
 *  will be generated.
 *
 *  The only garbage is generated when using the String for header names/values -
 *  this can't be avoided when the servlet calls header methods, but is easy
 *  to avoid inside tomcat. The goal is to use _only_ MessageByte-based Fields,
 *  and reduce to 0 the memory overhead of tomcat.
 *
 *  TODO:
 *  XXX one-buffer parsing - for http ( other protocols don't need that )
 *  XXX remove unused methods
 *  XXX External enumerations, with 0 GC.
 *  XXX use HeaderName ID
 *
 *
 * @author dac@eng.sun.com
 * @author James Todd [gonzo@eng.sun.com]
 * @author Costin Manolache
 * @author kevin seguin
 */
public class MimeHeaders {

    /** Initial size - should be == average number of headers per request
     *  XXX  make it configurable ( fine-tuning of web-apps )
     */
    public static final int DEFAULT_HEADER_SIZE=8;

    private static final StringManager sm =
            StringManager.getManager("org.apache.tomcat.util.http");

    /**
     * The header fields.
     */
    private MimeHeaderField[] headers = new
            MimeHeaderField[DEFAULT_HEADER_SIZE];

    /**
     * The current number of header fields.
     */
    private int count;

    // -------------------- Idx access to headers ----------

    /**
     * @return the current number of header fields.
     */
    public int size() {
        return count;
    }

    /**
     * @param n The header index
     * @return the Nth header name, or null if there is no such header.
     * This may be used to iterate through all header fields.
     */
    public MessageBytes getName(int n) {
        return n >= 0 && n < count ? headers[n].getName() : null;
    }

    /**
     * @param n The header index
     * @return the Nth header value, or null if there is no such header.
     * This may be used to iterate through all header fields.
     */
    public MessageBytes getValue(int n) {
        return n >= 0 && n < count ? headers[n].getValue() : null;
    }

}

/** Enumerate the distinct header names.
    Each nextElement() is O(n) ( a comparison is
    done with all previous elements ).

    This is less frequent than add() -
    we want to keep add O(1).
*/
class NamesEnumerator implements Enumeration<String> {
    private int pos;
    private final int size;
    private String next;
    private final MimeHeaders headers;




    public NamesEnumerator(MimeHeaders headers) {
        this.headers=headers;
        pos=0;
        size = headers.size();
        findNext();
    }

    private void findNext() {
        next=null;
        for(; pos< size; pos++ ) {
            next=headers.getName( pos ).toString();
            for( int j=0; j<pos ; j++ ) {
                if( headers.getName( j ).equalsIgnoreCase( next )) {
                    // duplicate.
                    next=null;
                    break;
                }
            }
            if( next!=null ) {
                // it's not a duplicate
                break;
            }
        }
        // next time findNext is called it will try the
        // next element
        pos++;
    }

    @Override
    public boolean hasMoreElements() {
        return next!=null;
    }

    @Override
    public String nextElement() {
        String current=next;
        findNext();
        return current;
    }
}

/** Enumerate the values for a (possibly ) multiple
    value element.
*/
class ValuesEnumerator implements Enumeration<String> {
    private int pos;
    private final int size;
    private MessageBytes next;
    private final MimeHeaders headers;
    private final String name;

    ValuesEnumerator(MimeHeaders headers, String name) {
        this.name=name;
        this.headers=headers;
        pos=0;
        size = headers.size();
        findNext();
    }

    private void findNext() {
        next=null;
        for(; pos< size; pos++ ) {
            MessageBytes n1=headers.getName( pos );
            if( n1.equalsIgnoreCase( name )) {
                next=headers.getValue( pos );
                break;
            }
        }
        pos++;
    }

    @Override
    public boolean hasMoreElements() {
        return next!=null;
    }

    @Override
    public String nextElement() {
        MessageBytes current=next;
        findNext();
        return current.toString();
    }
}

class MimeHeaderField {

    private final MessageBytes nameB = MessageBytes.newInstance();
    private final MessageBytes valueB = MessageBytes.newInstance();

    /**
     * Creates a new, uninitialized header field.
     */
    public MimeHeaderField() {
        // NO-OP
    }

    public void recycle() {
        nameB.recycle();
        valueB.recycle();
    }

    public MessageBytes getName() {
        return nameB;
    }

    public MessageBytes getValue() {
        return valueB;
    }

    @Override
    public String toString() {
        return nameB + ": " + valueB;
    }
}
