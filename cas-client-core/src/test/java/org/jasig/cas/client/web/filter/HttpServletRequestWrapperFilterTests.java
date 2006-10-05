/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.web.filter;

import junit.framework.TestCase;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.client.validation.AssertionImpl;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Tests for the HttpServletRequestWrapperFilter.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */

public final class HttpServletRequestWrapperFilterTests extends TestCase {

    private HttpServletRequestWrapperFilter filter = new HttpServletRequestWrapperFilter();

    protected HttpServletRequest mockRequest;

    protected void setUp() throws Exception {
        this.filter.init(null);
        this.filter.destroy();
    }

    public void testWrappedRequest() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpSession session = new MockHttpSession();
        final FilterChain filterChain = new FilterChain() {

            public void doFilter(ServletRequest request,
                                 ServletResponse response) throws IOException, ServletException {
                HttpServletRequestWrapperFilterTests.this.mockRequest = (HttpServletRequest) request;
            }

        };
        session.setAttribute(AbstractCasFilter.CONST_ASSERTION,
                new AssertionImpl(new SimplePrincipal("test")));

        request.setSession(session);

        this.filter.doFilter(request, new MockHttpServletResponse(),
                filterChain);
        assertEquals("test", this.mockRequest.getRemoteUser());
    }
}
