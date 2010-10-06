/*
 * Copyright 2010 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.tomcat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.validation.Assertion;

/**
 * Base class for all logout handlers.
 *
 * @author Marvin S. Addison
 * @version $Revision$
 *
 */
public abstract class AbstractLogoutHandler implements LogoutHandler {

    protected final Log log = LogFactory.getLog(getClass());

    protected String redirectUrl;

    public void setRedirectUrl(final String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    /** {@inheritDoc} */
    public void logout(final HttpServletRequest request, final HttpServletResponse response) {
        this.log.debug("Processing logout request from CAS server.");

        final Assertion assertion;
        final HttpSession httpSession = request.getSession(false);
        if (httpSession != null && (assertion = (Assertion) httpSession.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION)) != null) {
            httpSession.removeAttribute(AbstractCasFilter.CONST_CAS_ASSERTION);
            this.log.info("Successfully logged out " + assertion.getPrincipal());
        } else {
            this.log.info("Session already ended.");
        }

        final String redirectUrl = constructRedirectUrl(request);
        if (redirectUrl != null) {
            this.log.debug("Redirecting to " + redirectUrl);
            CommonUtils.sendRedirect(response, redirectUrl);
        }
    }

    /**
     * Constructs a url to redirect to.
     *
     * @param request the original request.
     * @return the url to redirect to. CAN be NULL.
     */
    protected String constructRedirectUrl(final HttpServletRequest request) {
        return redirectUrl;
    }
}
