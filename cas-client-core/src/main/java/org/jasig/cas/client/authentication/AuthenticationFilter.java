/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.authentication;

import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.util.AbstractCasFilter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * Filter implementation to intercept all requests and attempt to authenticate
 * the user by redirecting them to CAS (unless the user has a ticket).
 * <p>
 * This filter allows you to specify the following parameters (at either the context-level or the filter-level):
 * <ul>
 * <li><code>casServerLoginUrl</code> - the url to log into CAS, i.e. https://cas.rutgers.edu/login</li>
 * <li><code>renew</code> - true/false on whether to use renew or not.</li>
 * <li><code>gateway</code> - true/false on whether to use gateway or not.</li>
 * </ul>
 *
 * <p>Please see AbstractCasFilter for additional properties.</p>
 *
 * @author Scott Battaglia
 * @version $Revision: 11768 $ $Date: 2007-02-07 15:44:16 -0500 (Wed, 07 Feb 2007) $
 * @since 3.0
 */
public class AuthenticationFilter extends AbstractCasFilter {

    protected static final String CONST_CAS_GATEWAY = "_const_cas_gateway_";

    /**
     * The URL to the CAS Server login.
     */
    private String casServerLoginUrl;

    /**
     * Whether to send the renew request or not.
     */
    private boolean renew = false;

    /**
     * Whether to send the gateway request or not.
     */
    private boolean gateway = false;

    public void init(final FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
        setCasServerLoginUrl(getPropertyFromInitParams(filterConfig, "casServerLoginUrl", null));
        setRenew(Boolean.parseBoolean(getPropertyFromInitParams(filterConfig, "renew", "false")));
        setGateway(Boolean.parseBoolean(getPropertyFromInitParams(filterConfig, "gateway", "false")));
    }

    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;
        final HttpSession session = request.getSession(false);
        final String ticket = request.getParameter(getArtifactParameterName());
        final Assertion assertion = session != null ? (Assertion) session
                .getAttribute(CONST_CAS_ASSERTION) : null;
        final boolean wasGatewayed = session != null
                && session.getAttribute(CONST_CAS_GATEWAY) != null;

        if (CommonUtils.isBlank(ticket) && assertion == null && !wasGatewayed) {
            log.debug("no ticket and no assertion found");
            if (this.gateway) {
                log.debug("setting gateway attribute in session");
                request.getSession(true).setAttribute(CONST_CAS_GATEWAY, "yes");
            }

            final String serviceUrl = constructServiceUrl(request, response);
            final String urlToRedirectTo = this.casServerLoginUrl + "?" + getServiceParameterName() + "="
                    + URLEncoder.encode(serviceUrl, "UTF-8")
                    + (this.renew ? "&renew=true" : "")
                    + (this.gateway ? "&gateway=true" : "");

            if (log.isDebugEnabled()) {
                log.debug("redirecting to \"" + urlToRedirectTo + "\"");
            }

            response.sendRedirect(urlToRedirectTo);
            return;
        }

        if (session != null) {
            log.debug("removing gateway attribute from session");
            session.setAttribute(CONST_CAS_GATEWAY, null);
        }

        filterChain.doFilter(request, response);
    }

    public void setRenew(final boolean renew) {
        this.renew = renew;
    }

    public void setGateway(final boolean gateway) {
        this.gateway = gateway;
    }

    public void setCasServerLoginUrl(final String casServerLoginUrl) {
        this.casServerLoginUrl = casServerLoginUrl;
    }
}
