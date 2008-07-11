/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.session;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

/**
 * HashMap backed implementation of SessionMappingStorage.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 *
 */
public final class HashMapBackedSessionMappingStorage implements
		SessionMappingStorage {
	
    /**
     * Maps the ID from the CAS server to the Session.
     */
    private final Map MANAGED_SESSIONS = new HashMap();

    /**
     * Maps the Session ID to the key from the CAS Server.
     */
    private final Map ID_TO_SESSION_KEY_MAPPING = new HashMap();

	public synchronized void addSessionById(String mappingId, HttpSession session) {
        ID_TO_SESSION_KEY_MAPPING.put(session.getId(), mappingId);
        MANAGED_SESSIONS.put(mappingId, session);

	}

	public synchronized void removeBySessionById(String sessionId) {
        final String key = (String) ID_TO_SESSION_KEY_MAPPING.get(sessionId);
        MANAGED_SESSIONS.remove(key);
        ID_TO_SESSION_KEY_MAPPING.remove(sessionId);
	}

	public synchronized HttpSession removeSessionByMappingId(String mappingId) {
		final HttpSession session = (HttpSession) MANAGED_SESSIONS.get(mappingId);

        if (session != null) {
        	removeBySessionById(session.getId());
        }

        return session;
	}
}
