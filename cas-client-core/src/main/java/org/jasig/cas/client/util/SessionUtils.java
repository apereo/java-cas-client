package org.jasig.cas.client.util;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 13-9-12
 * Time: 上午11:59
 * To change this template use File | Settings | File Templates.
 */
public class SessionUtils {

    public static String stripSessionIdPostfix(String sessionId) {
        int pos = sessionId.lastIndexOf(".");
        return pos == -1 ? sessionId : sessionId.substring(0, pos);
    }
}
