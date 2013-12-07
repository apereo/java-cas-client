/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.cas.client.session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.client.util.SessionUtils;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Listener to detect when an HTTP session is destroyed and remove it from the map of
 * managed sessions.  Also allows for the programmatic removal of sessions.
 * <p/>
 * Enables the CAS Single Sign out feature.
 * <p/>
 * Scott Battaglia
 *
 * @version $Revision$ Date$
 * @since 3.1
 */
public final class SingleSignOutHttpSessionListener implements HttpSessionListener {
    private final Log LOG = LogFactory.getLog(getClass());
//	private SessionMappingStorage sessionMappingStorage;

    public void sessionCreated(final HttpSessionEvent event) {
        HttpSession session = event.getSession();
        String stripSessionId = SessionUtils.stripSessionIdPostfix(session.getId());
        SessionStorage storage = SessionStorage.getInstance();
        if (!storage.containsKey(stripSessionId)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("### map (SessionId: %s) to (session: %s) ###", session.getId(), session.toString()));
            }
            storage.put(stripSessionId, session);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("~~~ Session(SessionId: %s) is already in the Map ~~~", session.getId()));
            }
        }
    }

    public void sessionDestroyed(final HttpSessionEvent event) {
        SessionMappingStorage sessionMappingStorage = getSessionMappingStorage();
        final HttpSession session = event.getSession();
        String sessionId = session.getId();
        sessionMappingStorage.removeBySessionById(sessionId);
        String stripSessionId = SessionUtils.stripSessionIdPostfix(sessionId);
        SessionStorage.getInstance().remove(stripSessionId);
        if (LOG.isDebugEnabled()) {
            LOG.debug("^^^^ REMOVE Session(" + sessionId + ") from the map and the sessionMapping Storage ^^^^");
        }
    }

    /**
     * Obtains a {@link SessionMappingStorage} object. Assumes this method will always return the same
     * instance of the object.  It assumes this because it generally lazily calls the method.
     *
     * @return the SessionMappingStorage
     */
    protected static SessionMappingStorage getSessionMappingStorage() {
        return SingleSignOutFilter.getSingleSignOutHandler().getSessionMappingStorage();
    }
}
