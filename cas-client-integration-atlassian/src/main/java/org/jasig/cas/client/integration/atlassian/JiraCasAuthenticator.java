/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.integration.atlassian;

import com.atlassian.seraph.auth.DefaultAuthenticator;
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
 * Extension of ConfluenceAuthenticator to allow people to configure Confluence to authenticate
 * via CAS.
 *
 * @author Scott Battaglia
 * @author John Watson
 * @version $Revision$ $Date$
 * @since 3.1.3
 */
public final class JiraCasAuthenticator extends DefaultAuthenticator {

    private static final Log LOG = LogFactory.getLog(JiraCasAuthenticator.class);

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