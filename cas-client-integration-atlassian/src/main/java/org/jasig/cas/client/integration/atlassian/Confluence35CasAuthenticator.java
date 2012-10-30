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

package org.jasig.cas.client.integration.atlassian;

import com.atlassian.confluence.event.events.security.LoginEvent;
import com.atlassian.confluence.event.events.security.LoginFailedEvent;
import com.atlassian.confluence.user.ConfluenceAuthenticator;
import com.atlassian.seraph.auth.AuthenticatorException;
import com.atlassian.seraph.auth.LoginReason;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.validation.Assertion;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.security.Principal;

/**
 * Extension of ConfluenceAuthenticator to allow people to configure Confluence 3.5+ to authenticate
 * via CAS.
 *
 * Based on https://bitbucket.org/jaysee00/example-confluence-sso-authenticator
 *
 * @author Scott Battaglia
 * @author John Watson
 * @author Jozef Kotlar
 * @version $Revision$ $Date$
 * @since 3.3.0
 */
public final class Confluence35CasAuthenticator extends ConfluenceAuthenticator {
    private static final long serialVersionUID = -6097438206488390678L;

    private static final Log LOG = LogFactory.getLog(Confluence35CasAuthenticator.class);

    public Principal getUser(final HttpServletRequest request, final HttpServletResponse response) {
        Principal existingUser = getUserFromSession(request);
        if (existingUser != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Session found; user already logged in.");
            }
            return existingUser;
        }

        final HttpSession session = request.getSession();
        final Assertion assertion = (Assertion) session.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION);

        if (assertion != null) {
            final String username = assertion.getPrincipal().getName();
            final Principal user = getUser(username);
            final String remoteIP = request.getRemoteAddr();
            final String remoteHost = request.getRemoteHost();

            if (user != null) {
                putPrincipalInSessionContext(request, user);
                getElevatedSecurityGuard().onSuccessfulLoginAttempt(request, username);
                // Firing this event is necessary to ensure the user's personal information is intialised correctly.
                getEventPublisher().publish(new LoginEvent(this, username, request.getSession().getId(), remoteHost, remoteIP));
                LoginReason.OK.stampRequestResponse(request, response);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Logging in [" + username + "] from CAS.");
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Failed logging [" + username + "] from CAS.");
                }
                getElevatedSecurityGuard().onFailedLoginAttempt(request, username);
                getEventPublisher().publish(new LoginFailedEvent(this, username, request.getSession().getId(), remoteHost, remoteIP));
            }
            return user;
        }

        return super.getUser(request, response);
    }

    public boolean logout(final HttpServletRequest request, final HttpServletResponse response) throws AuthenticatorException {
        final HttpSession session = request.getSession();

        final Principal principal = (Principal) session.getAttribute(LOGGED_IN_KEY);

        if (LOG.isDebugEnabled() && principal != null) {
            LOG.debug("Logging out [" + principal.getName() + "] from CAS.");
        }

        removePrincipalFromSessionContext(request);
        session.setAttribute(AbstractCasFilter.CONST_CAS_ASSERTION, null);
        return true;
    }
}