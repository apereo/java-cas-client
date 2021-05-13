/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.client.session;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionIdListener;
import javax.servlet.http.HttpSessionListener;

/**
 * Listener to detect when an HTTP session is destroyed and remove it from the map of
 * managed sessions.  Also allows for the programmatic removal of sessions.
 * <p>
 * Enables the CAS Single Sign out feature.
 *
 * Scott Battaglia
 * @version $Revision$ Date$
 * @since 3.1
 */
public final class SingleSignOutHttpSessionListener implements HttpSessionListener, HttpSessionIdListener {

    private SessionMappingStorage sessionMappingStorage;

    protected SessionMappingStorage getSessionMappingStorage() {
        if (sessionMappingStorage == null) {
            this.setSessionMappingStorage(SingleSignOutFilter.getSingleSignOutHandler().getSessionMappingStorage());
        }
        return sessionMappingStorage;
    }

    public void setSessionMappingStorage(SessionMappingStorage sessionMappingStorage) {
        this.sessionMappingStorage = sessionMappingStorage;
    }

    @Override
    public void sessionCreated(final HttpSessionEvent event) {
        // nothing to do at the moment
    }

    @Override
    public void sessionDestroyed(final HttpSessionEvent event) {
        final HttpSession session = event.getSession();
        getSessionMappingStorage().removeBySessionById(session.getId());
    }

    @Override
    public void sessionIdChanged(HttpSessionEvent event, String oldSessionId) {
        getSessionMappingStorage().changeSessionId(oldSessionId, event.getSession());
    }
}
