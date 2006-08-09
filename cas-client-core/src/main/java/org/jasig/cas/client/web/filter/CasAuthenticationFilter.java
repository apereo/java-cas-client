/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.client.web.filter;

import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.validation.Assertion;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * Filter implementation to intercept all requests and attempt to authenticate
 * the user by redirecting them to CAS (unless the user has a ticket).
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class CasAuthenticationFilter extends AbstractCasFilter {

    /**
     * The URL to the CAS Server login.
     */
    private final String casServerLoginUrl;

    /**
     * Whether to send the renew request or not.
     */
    private final boolean renew;

    /**
     * Whether to send the gateway request or not.
     */
    private final boolean gateway;

    public CasAuthenticationFilter(final String serverName, final String serviceUrl, final String casServerLoginUrl) {
        this(serverName, serviceUrl, true, casServerLoginUrl, false, false);
    }

    public CasAuthenticationFilter(final String serverName, final String serviceUrl, final String casServerLoginUrl, boolean renew, boolean gateway) {
        this(serverName, serviceUrl, true, casServerLoginUrl, renew, gateway);
    }

    public CasAuthenticationFilter(String serverName, String serviceUrl, boolean useSession, String casServerLoginUrl, boolean renew, boolean gateway) {
        super(serverName, serviceUrl, useSession);
        CommonUtils.assertNotNull(casServerLoginUrl,
                "the CAS Server Login URL cannot be null.");
        this.casServerLoginUrl = casServerLoginUrl;
        this.renew = renew;
        this.gateway = gateway;
    }

    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response, final FilterChain filterChain)
            throws IOException, ServletException {
        final HttpSession session = request.getSession(isUseSession());
        final String ticket = request.getParameter(PARAM_TICKET);
        final Assertion assertion = session != null ? (Assertion) session
                .getAttribute(CONST_ASSERTION) : null;
        final boolean wasGatewayed = session != null
                && session.getAttribute(CONST_GATEWAY) != null;

        if (CommonUtils.isBlank(ticket) && assertion == null && !wasGatewayed) {
            if (this.gateway && session != null) {
                session.setAttribute(CONST_GATEWAY, "yes");
            }

            final String serviceUrl = constructServiceUrl(request, response);
            final String urlToRedirectTo = this.casServerLoginUrl + "?service="
                    + URLEncoder.encode(serviceUrl, "UTF-8")
                    + (this.renew ? "&renew=true" : "")
                    + (this.gateway ? "&gateway=true" : "");
            response.sendRedirect(urlToRedirectTo);
            return;
        }

        if (session != null) {
            session.setAttribute(CONST_GATEWAY, null);
        }

        filterChain.doFilter(request, response);
    }
}
