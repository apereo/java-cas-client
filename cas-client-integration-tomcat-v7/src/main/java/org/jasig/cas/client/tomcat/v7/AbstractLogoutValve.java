/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.tomcat.v7;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.client.util.AbstractCasFilter;

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
public abstract class AbstractLogoutValve extends ValveBase {

    protected final Log log = LogFactory.getLog(getClass());

    public final void invoke(final Request request, final Response response) throws IOException, ServletException {

        if (!isLogoutRequest(request)) {
            log.debug("Current request URI [ " + request.getRequestURI() + "] is not a logout request.");
            getNext().invoke(request, response);
            return;
        }

        final HttpSession httpSession = request.getSession(false);

        if (httpSession != null) {
            httpSession.removeAttribute(AbstractCasFilter.CONST_CAS_ASSERTION);
        }

        final String redirectUrl = constructRedirectUrl(request);

        if (redirectUrl != null) {
            response.sendRedirect(redirectUrl);
        }
    }

    /**
     * Determines if this is a request to destroy the container-managed single sign on session.
     *
     * @param request the request.  CANNOT be NULL.
     * @return true if it is a logout request, false otherwise.
     */
    protected abstract boolean isLogoutRequest(Request request);

    /**
     * Constructs a url to redirect to.
     *
     * @param request the original request.
     * @return the url to redirect to. CAN be NULL.
     */
    protected abstract String constructRedirectUrl(Request request);
}
