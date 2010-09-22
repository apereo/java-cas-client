/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.tomcat.v6;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.validation.Assertion;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Abstract base class for Container-managed log out.  Removes the attributes
 * from the session.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1.12
 */
public abstract class AbstractLogoutValve extends AbstractLifecycleValve {

    protected String redirectUrl;

    public void setRedirectUrl(final String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public final void invoke(final Request request, final Response response) throws IOException, ServletException {
        if (!isLogoutRequest(request)) {
            this.log.debug("URI is not a logout request: " + request.getRequestURI());
            getNext().invoke(request, response);
            return;
        }
        this.log.debug("Processing logout request from CAS server.");

        Assertion assertion = null;
        final HttpSession httpSession = request.getSession(false);
        if (httpSession != null) {
            assertion = (Assertion) httpSession.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION);
            if (assertion != null) {
	            httpSession.removeAttribute(AbstractCasFilter.CONST_CAS_ASSERTION);
            }
        }

        if (assertion != null) {
	        this.log.info("Successfully logged out " + assertion.getPrincipal());
        } else {
            this.log.info("Session already ended.");
        }

        final String redirectUrl = constructRedirectUrl(request);
        if (redirectUrl != null) {
            response.sendRedirect(redirectUrl);
        }
    }

    /**
     * Constructs a url to redirect to.
     *
     * @param request the original request.
     * @return the url to redirect to. CAN be NULL.
     */
    protected String constructRedirectUrl(final Request request) {
        return redirectUrl;
    }
    
    /**
     * Determines if this is a request to destroy the container-managed single sign on session.
     *
     * @param request the request.  CANNOT be NULL.
     * @return true if it is a logout request, false otherwise.
     */
    protected abstract boolean isLogoutRequest(Request request);

}
