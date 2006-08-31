/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.client.web.filter;

import org.jasig.cas.client.validation.Assertion;
import org.springframework.web.util.WebUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;

/**
 * Implementation of a filter that wraps the normal HttpServletRequest with a
 * wrapper that overrides the getRemoteUser method to retrieve the user from the
 * CAS Assertion.
 * <p/>
 * This filter needs to be configured in the chain so that it executes after
 * both the authentication and the validation filters.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class HttpServletRequestWrapperFilter implements Filter {

    public void destroy() {
        // nothing to do
    }

    /**
     * Wraps the HttpServletRequest in a wrapper class that delegates
     * <code>request.getRemoteUser</code> to the underlying Assertion object
     * stored in the user session.
     */
    public void doFilter(final ServletRequest servletRequest,
                         final ServletResponse servletResponse, final FilterChain filterChain)
            throws IOException, ServletException {
        filterChain.doFilter(new CasHttpServletRequestWrapper(
                (HttpServletRequest) servletRequest), servletResponse);
    }

    public void init(final FilterConfig filterConfig) throws ServletException {
        // nothing to do
    }

    final class CasHttpServletRequestWrapper extends HttpServletRequestWrapper {

        CasHttpServletRequestWrapper(final HttpServletRequest request) {
            super(request);
        }

        public String getRemoteUser() {
            final org.jasig.cas.authentication.principal.Principal p = (org.jasig.cas.authentication.principal.Principal) this
                    .getAttribute(AbstractCasFilter.CONST_PRINCIPAL);

            if (p != null) {
                return p.getId();
            }

            final Assertion assertion = (Assertion) WebUtils
                    .getSessionAttribute(this,
                            AbstractCasFilter.CONST_ASSERTION);

            if (assertion != null) {
                return assertion.getPrincipal().getId();
            }

            return null;
        }
    }
}
