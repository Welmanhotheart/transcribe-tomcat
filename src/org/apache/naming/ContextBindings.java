package org.apache.naming;

import javax.naming.Context;
import javax.naming.NamingException;
import java.util.Hashtable;
import java.util.MissingResourceException;

/***
 * TODO
 * @author <a href="zhiwei.wei@bintools.cn">zhiwei.wei</a>
 * @version 1.0.0 2022-04-2022/4/28-下午7:53
 */
public class ContextBindings {


    // -------------------------------------------------------------- Variables

    /**
     * Bindings object - naming context. Keyed by object.
     */
    private static final Hashtable<Object,Context> objectBindings = new Hashtable<>();


    /**
     * Bindings thread - naming context. Keyed by thread.
     */
    private static final Hashtable<Thread,Context> threadBindings = new Hashtable<>();

    /**
     * The string manager for this package.
     */
    protected static final StringManager sm = StringManager.getManager(ContextBindings.class);

    /**
     * Bindings thread - object. Keyed by thread.
     */
    private static final Hashtable<Thread,Object> threadObjectBindings = new Hashtable<>();

    /**
     * Binds a naming context to a thread.
     *
     * @param obj   Object bound to the required naming context
     * @param token Security token
     *
     * @throws NamingException If no naming context is bound to the provided
     *         object
     */
    public static void bindThread(Object obj, Object token) throws NamingException {
        if (ContextAccessController.checkSecurityToken(obj, token)) {
            Context context = objectBindings.get(obj);
            if (context == null) {
                throw new NamingException(
                        sm.getString("contextBindings.unknownContext", obj));
            }
            threadBindings.put(Thread.currentThread(), context);
            threadObjectBindings.put(Thread.currentThread(), obj);
        }
    }

    /**
     * Unbinds a thread and a naming context.
     *
     * @param obj   Object bound to the required naming context
     * @param token Security token
     */
    public static void unbindThread(Object obj, Object token) {
        if (ContextAccessController.checkSecurityToken(obj, token)) {
            threadBindings.remove(Thread.currentThread());
            threadObjectBindings.remove(Thread.currentThread());
        }
    }

}