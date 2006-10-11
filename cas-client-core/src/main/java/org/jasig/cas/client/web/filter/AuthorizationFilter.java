/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.web.filter;

import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.client.authorization.AuthorizationException;
import org.jasig.cas.client.authorization.AuthorizedDecider;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.validation.Assertion;
import org.springframework.web.util.WebUtils;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Simple filter that attempts to determine if someone is authorized to use the
 * system. Assumes that you are protecting the application with the
 * AuthenticationFilter such that the Assertion is set in the session.
 * <p/>
 * If a user is not authorized to use the application, the response code of 403
 * will be sent to the browser.
 * <p/>
 * This filter needs to be configured after both the authentication filter and
 * the validation filter.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @see AuthorizedDecider
 * @since 3.0
 */
public final class AuthorizationFilter implements Filter {


    /**
     * Instance of Commons Logging.
     */
    private final Log log = LogFactory.getLog(this.getClass());

    /**
     * Decider that determines whether a specified principal has access to the
     * resource or not.
     */
    private final AuthorizedDecider decider;

    /**
     * @param casAuthorizedDecider the thing actually deciding to grant access or not.
     */
    public AuthorizationFilter(final AuthorizedDecider casAuthorizedDecider) {
        CommonUtils.assertNotNull(casAuthorizedDecider,
                "the casAuthorizedDecider cannot be null.");
        this.decider = casAuthorizedDecider;
    }

    public void destroy() {
        // nothing to do here
    }

    public void doFilter(final ServletRequest servletRequest,
                         final ServletResponse servletResponse, final FilterChain filterChain)
            throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;
        final Assertion assertion = (Assertion) WebUtils.getRequiredSessionAttribute(request, AbstractCasFilter.CONST_ASSERTION);
        final Principal principal = assertion.getPrincipal();

        final boolean authorized = this.decider
                .isAuthorizedToUseApplication(principal);

        if (!authorized) {
            log.debug("User not authorized to access application.");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            throw new AuthorizationException(principal.getId()
                    + " is not authorized to use this application.");
        }

        log.debug("User successfully authorized.");
        filterChain.doFilter(servletRequest, servletResponse);
    }

    public void init(final FilterConfig filterConfig) throws ServletException {
        // nothing to do here
    }
}
