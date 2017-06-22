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

import com.atlassian.jira.security.login.JiraSeraphAuthenticator;
import com.atlassian.seraph.auth.AuthenticationContextAwareAuthenticator;
import com.atlassian.seraph.auth.AuthenticatorException;
import com.atlassian.seraph.auth.LoginReason;
import java.security.Principal;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.validation.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension of JiraSeraphAuthenticator to allow people to configure 
 * JIRA 4.4 and above to authenticate via Jasig CAS
 *
 * @author Scott Battaglia
 * @author Martin Stiborsky
 * @author Jozef Kotlar
 * @version $Revision$ $Date$
 * @since 3.3.0
 */
@AuthenticationContextAwareAuthenticator
public final class Jira44CasAuthenticator extends JiraSeraphAuthenticator {

    /** Jira43CasAuthenticator.java */
    private static final long serialVersionUID = 3852011252741183166L;

    private static final Logger LOGGER = LoggerFactory.getLogger(Jira44CasAuthenticator.class);

    public Principal getUser(final HttpServletRequest request, final HttpServletResponse response) {
        // First, check to see if this session has already been authenticated during a previous request.
        Principal existingUser = getUserFromSession(request);
        if (existingUser != null) {
            LOGGER.debug("Session found; user already logged in.");
            return existingUser;
        }

        final HttpSession session = request.getSession();
        final Assertion assertion = (Assertion) session.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION);

        if (assertion != null) {
            final String username = assertion.getPrincipal().getName();
            final Principal user = getUser(username);

            if (user != null) {
                putPrincipalInSessionContext(request, user);
                getElevatedSecurityGuard().onSuccessfulLoginAttempt(request, username);
                LoginReason.OK.stampRequestResponse(request, response);
                LOGGER.debug("Logging in [{}] from CAS.", username);
            } else {
                LOGGER.debug("Failed logging [{}] from CAS.", username);
                getElevatedSecurityGuard().onFailedLoginAttempt(request, username);
            }
            return user;
        }

        return super.getUser(request, response);
    }

    public boolean logout(final HttpServletRequest request, final HttpServletResponse response)
            throws AuthenticatorException {
        final HttpSession session = request.getSession();
        final Principal p = (Principal) session.getAttribute(LOGGED_IN_KEY);

        if (p != null) {
            LOGGER.debug("Logging out [{}] from CAS.", p.getName());
        }

        removePrincipalFromSessionContext(request);
        session.setAttribute(AbstractCasFilter.CONST_CAS_ASSERTION, null);
        return true;
    }
}
