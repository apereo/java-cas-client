/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.integration.atlassian;

import com.atlassian.confluence.user.ConfluenceAuthenticator;
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
 * @version $Revision$ $Date$
 * @since 3.1.2
 */
public final class ConfluenceCasAuthenticator extends ConfluenceAuthenticator {

    private static final Log log = LogFactory.getLog(ConfluenceCasAuthenticator.class);

    public Principal getUser(final HttpServletRequest request, final HttpServletResponse response) {
        final HttpSession session = request.getSession();

        if (session != null) {
        // user already exists
            if (session.getAttribute(ConfluenceCasAuthenticator.LOGGED_IN_KEY) != null) {
                log.info("Session found; user already logged in.");
                return (Principal) session.getAttribute(LOGGED_IN_KEY);
            }

            final Assertion assertion = (Assertion) session.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION);

            if (assertion != null) {
                if (assertion != null) {
                    final Principal p = getUser(assertion.getPrincipal().getName());
                    request.getSession().setAttribute(LOGGED_IN_KEY, p);
                    request.getSession().setAttribute(LOGGED_OUT_KEY, null);
                    return p;
                }
            }
        }

        return super.getUser(request, response);
    }
}