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

import com.atlassian.jira.security.login.JiraSeraphAuthenticator;
import com.atlassian.seraph.auth.AuthenticatorException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.validation.Assertion;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.security.Principal;

/**
 * Extension of JiraSeraphAuthenticator to allow people to configure 
 * JIRA 4.4 and above to authenticate via Jasig CAS
 *
 * @author Scott Battaglia
 * @author Martin Stiborský
 * @version $Revision$ $Date$
 * @since 3.3.0
 */
public final class Jira44CasAuthenticator extends JiraSeraphAuthenticator {

    /** Jira43CasAuthenticator.java */
    private static final long serialVersionUID = 3852011252741183166L;

    private static final Log LOG = LogFactory.getLog(Jira44CasAuthenticator.class);

    public Principal getUser(final HttpServletRequest request, final HttpServletResponse response) {
        final HttpSession session = request.getSession();

        // user already exists
        if (session.getAttribute(LOGGED_IN_KEY) != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Session found; user already logged in.");
            }
            return (Principal) session.getAttribute(LOGGED_IN_KEY);
        }

        final Assertion assertion = (Assertion) session.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION);

        if (assertion != null) {
            final Principal p = getUser(assertion.getPrincipal().getName());

            if (LOG.isDebugEnabled()) {
                LOG.debug("Logging in [" + p.getName() + "] from CAS.");
            }

            session.setAttribute(LOGGED_IN_KEY, p);
            session.setAttribute(LOGGED_OUT_KEY, null);
            return p;
        }

        return super.getUser(request, response);
    }

    public boolean logout(final HttpServletRequest request, final HttpServletResponse response) throws AuthenticatorException {
        final HttpSession session = request.getSession();
        final Principal p = (Principal) session.getAttribute(LOGGED_IN_KEY);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Logging out [" + p.getName() + "] from CAS.");
        }

        session.setAttribute(LOGGED_OUT_KEY, p);
        session.setAttribute(LOGGED_IN_KEY, null);
        session.setAttribute(AbstractCasFilter.CONST_CAS_ASSERTION, null);
        return true;
    }
}