package org.apache.tomcat.util.res;



import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * What does StringManager mean?
 */
public class StringManager {

    private static final int LOCALE_CACHE_SIZE = 10;

    /**
     * stringManager has sth to with locale and packagename
     * @param packageName
     * @param locale
     */
    public StringManager(String packageName, Locale locale) {

    }
    public static final StringManager getManager(Class<?> clazz) {
        return getManager(clazz.getPackage().getName());
    }
    public static final StringManager getManager(String packageName) {

        return getManager(packageName, Locale.getDefault());
    }


    private static final Map<String, Map<Locale, StringManager>> managers =
            new Hashtable<>();

    /**
     * has sth to do with locale
     * @param packageName
     * @param locale
     * @return
     */
    private static StringManager getManager(String packageName, Locale locale) {
        Map<Locale, StringManager> map = managers.get(packageName);
        if (map == null) {
            map = new LinkedHashMap<>(LOCALE_CACHE_SIZE, 1, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<Locale, StringManager> eldest) {
                    if (size() > (LOCALE_CACHE_SIZE  - 1)) {
                        return true;
                    }
                    return false;
                }
            };
            managers.put(packageName, map);
        }
        StringManager mgr = map.get(locale);
        if (mgr == null) {
            mgr = new StringManager(packageName, locale);
            map.put(locale, mgr);
        }
        return mgr;
    }

    public String getString(final String key, final Object... args) {
        return null;
    }
}
