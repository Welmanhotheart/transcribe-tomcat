package org.apache.tomcat.util.buf;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CharsetCache {

    private ConcurrentMap<String,Charset> cache = new ConcurrentHashMap<>();

    private static final Charset DUMMY_CHARSET = new DummyCharset("Dummy",  null);


    private void addToCache(String name, Charset charset) {
        cache.put(name, charset);
        for (String alias : charset.aliases()) {
            cache.put(alias.toLowerCase(Locale.ENGLISH), charset);
        }
    }

    public Charset getCharset(String charsetName) {
        String lcCharsetName = charsetName.toLowerCase(Locale.ENGLISH);

        Charset result = cache.get(lcCharsetName);

        if (result == DUMMY_CHARSET) {
            // Name is known but the Charset is not in the cache
            Charset charset = Charset.forName(lcCharsetName);
            if (charset == null) {
                // Charset not available in this JVM - remove cache entry
                cache.remove(lcCharsetName);
                result = null;
            } else {
                // Charset is available - populate cache entry
                addToCache(lcCharsetName, charset);
                result = charset;
            }
        }

        return result;
    }

    /*
     * Placeholder Charset implementation for entries that will be loaded lazily
     * into the cache.
     */
    private static class DummyCharset extends Charset {

        protected DummyCharset(String canonicalName, String[] aliases) {
            super(canonicalName, aliases);
        }

        @Override
        public boolean contains(Charset cs) {
            return false;
        }

        @Override
        public CharsetDecoder newDecoder() {
            return null;
        }

        @Override
        public CharsetEncoder newEncoder() {
            return null;
        }
    }

}
