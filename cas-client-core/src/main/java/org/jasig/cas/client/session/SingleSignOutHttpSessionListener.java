package org.jasig.cas.client.session;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Listener to detect when an HTTP session is destroyed and remove it from the map of
 * managed sessions.  Also allows for the programmatic removal of sessions.
 * <p>
 * Enables the CAS Single Sign out feature.
 * <p>
 * Note that this class does not scale to multiple machines. 
 *
 * Scott Battaglia
 * @version $Revision$ Date$
 * @since 3.1
 */
public final class SingleSignOutHttpSessionListener implements HttpSessionListener {

    /**
     * Maps the ID from the CAS server to the Session.
     */
    private static final Map MANAGED_SESSIONS = new HashMap();

    /**
     * Maps the Session ID to the key from the CAS Server.
     */
    private static final Map ID_TO_SESSION_KEY_MAPPING = new HashMap();

    public void sessionCreated(final HttpSessionEvent event) {
        // nothing to do at the moment
    }

    public void sessionDestroyed(final HttpSessionEvent event) {
        final HttpSession session = event.getSession();
        final String key = (String) ID_TO_SESSION_KEY_MAPPING.get(session.getId());
        MANAGED_SESSIONS.remove(key);
        ID_TO_SESSION_KEY_MAPPING.remove(session.getId());
    }

    public static void addSession(final String key, final HttpSession value) {
        ID_TO_SESSION_KEY_MAPPING.put(value.getId(), key);
        MANAGED_SESSIONS.put(key, value);
    }

    /**
     * Method to remove the session from the mapping based on the key returned from the
     * CAS server.
     *
     * @param key the key to look up in the map of sessions.
     */
    public static void removeSession(final String key) {
        final HttpSession session = (HttpSession) MANAGED_SESSIONS.get(key);

        if (session == null) {
            return;
        }

        final String id = session.getId();
        MANAGED_SESSIONS.remove(key);
        ID_TO_SESSION_KEY_MAPPING.remove(id);

        session.invalidate();
    }
}
