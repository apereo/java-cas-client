/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
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
package org.jasig.cas.client.integration.atlassian;

import com.atlassian.seraph.auth.AuthenticatorException;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.opensymphony.user.EntityNotFoundException;
import com.opensymphony.user.UserManager;
import java.security.Principal;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.validation.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension of ConfluenceAuthenticator to allow people to configure Confluence to authenticate
 * via CAS.
 *
 * @author Scott Battaglia
 * @author John Watson
 * @version $Revision$ $Date$
 * @since 3.1.3
 */
public final class JiraCasAuthenticator extends DefaultAuthenticator {

    /** JiraCasAuthenticator.java */
    private static final long serialVersionUID = 3452011252741183166L;

    private static final Logger LOGGER = LoggerFactory.getLogger(JiraCasAuthenticator.class);

    @Override
    protected boolean authenticate(final Principal principal, final String password) throws AuthenticatorException {
        return true;
    }

    @Override
    protected Principal getUser(final String username) {
        try {
            return UserManager.getInstance().getUser(username);
        } catch (final EntityNotFoundException e) {
            LOGGER.warn("Could not find user '{}' in UserManager : {}", username, e);
        }
        return null;
    }

    public Principal getUser(final HttpServletRequest request, final HttpServletResponse response) {
        final HttpSession session = request.getSession();

        // user already exists
        if (session.getAttribute(LOGGED_IN_KEY) != null) {
            LOGGER.debug("Session found; user already logged in.");
            return (Principal) session.getAttribute(LOGGED_IN_KEY);
        }

        final Assertion assertion = (Assertion) session.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION);

        if (assertion != null) {
            final Principal p = getUser(assertion.getPrincipal().getName());

            LOGGER.debug("Logging in [{}] from CAS.", p.getName());

            session.setAttribute(LOGGED_IN_KEY, p);
            session.setAttribute(LOGGED_OUT_KEY, null);
            return p;
        }

        return super.getUser(request, response);
    }

    public boolean logout(final HttpServletRequest request, final HttpServletResponse response)
            throws AuthenticatorException {
        final HttpSession session = request.getSession();
        final Principal p = (Principal) session.getAttribute(LOGGED_IN_KEY);

        LOGGER.debug("Logging out [{}] from CAS.", p.getName());

        session.setAttribute(LOGGED_OUT_KEY, p);
        session.setAttribute(LOGGED_IN_KEY, null);
        session.setAttribute(AbstractCasFilter.CONST_CAS_ASSERTION, null);
        return true;
    }
}
