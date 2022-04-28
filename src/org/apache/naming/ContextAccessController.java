package org.apache.naming;

import java.util.Hashtable;

/***
 * TODO
 * @author <a href="zhiwei.wei@bintools.cn">zhiwei.wei</a>
 * @version 1.0.0 2022-04-2022/4/28-下午7:54
 */
public class ContextAccessController {

    /**
     * Security tokens repository.
     */
    private static final Hashtable<Object,Object> securityTokens = new Hashtable<>();


    /**
     * Check a submitted security token.
     *
     * @param name Name of the Catalina context
     * @param token Submitted security token
     *
     * @return <code>true</code> if the submitted token is equal to the token
     *         in the repository or if no token is present in the repository.
     *         Otherwise, <code>false</code>
     */
    public static boolean checkSecurityToken
    (Object name, Object token) {
        Object refToken = securityTokens.get(name);
        return (refToken == null || refToken.equals(token));
    }


}