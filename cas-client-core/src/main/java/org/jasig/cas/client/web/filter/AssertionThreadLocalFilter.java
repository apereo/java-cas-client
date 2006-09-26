/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.web.filter;

import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.AssertionHolder;
import org.springframework.web.util.WebUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Places the assertion in a ThreadLocal such that other resources can access it that do not have access to the web tier session.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class AssertionThreadLocalFilter implements Filter {

    public void init(final FilterConfig filterConfig) throws ServletException {
        // nothing to do here
    }

    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final Assertion assertion = (Assertion) WebUtils
                .getSessionAttribute(request,
                        AbstractCasFilter.CONST_ASSERTION);

        try {
            AssertionHolder.setAssertion(assertion);
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            AssertionHolder.clear();
        }
    }

    public void destroy() {
        // nothing to do
    }
}
