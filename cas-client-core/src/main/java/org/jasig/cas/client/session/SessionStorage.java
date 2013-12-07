package org.jasig.cas.client.session;

import javax.servlet.http.HttpSession;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 13-9-12
 * Time: 上午11:55
 * To change this template use File | Settings | File Templates.
 */
public class SessionStorage extends ConcurrentHashMap<String, HttpSession> {
    private final static SessionStorage instance = new SessionStorage();

    private SessionStorage() {

    }

    public static SessionStorage getInstance() {
        return instance;
    }

}
