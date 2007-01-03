/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.web.filter;

import junit.framework.TestCase;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.client.validation.AssertionImpl;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * Tests for the AuthenticationFilter.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class AuthenticationFilterTests extends TestCase {

    private static final String CAS_SERVICE_URL = "https://localhost:8443/service";

    private static final String CAS_LOGIN_URL = "https://localhost:8443/cas/login";

    private AuthenticationFilter filter;

    protected void setUp() throws Exception {
        this.filter = new AuthenticationFilter(CAS_SERVICE_URL, false, CAS_LOGIN_URL);
        this.filter.init(new MockFilterConfig());
    }

    protected void tearDown() throws Exception {
        this.filter.destroy();
    }

    public void testRedirect() throws Exception {
        final MockHttpSession session = new MockHttpSession();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final FilterChain filterChain = new FilterChain() {

            public void doFilter(ServletRequest arg0, ServletResponse arg1)
                    throws IOException, ServletException {
                // nothing to do
            }
        };

        request.setSession(session);
        this.filter.doFilter(request, response, filterChain);

        assertEquals(CAS_LOGIN_URL + "?service="
                + URLEncoder.encode(CAS_SERVICE_URL, "UTF-8"), response
                .getRedirectedUrl());
    }

    public void testRedirectWithQueryString() throws Exception {
        final MockHttpSession session = new MockHttpSession();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        request.setQueryString("test=12456");
        request.setRequestURI("/test");
        request.setSecure(true);
        final FilterChain filterChain = new FilterChain() {

            public void doFilter(ServletRequest arg0, ServletResponse arg1)
                    throws IOException, ServletException {
                // nothing to do
            }
        };

        request.setSession(session);
        this.filter = new AuthenticationFilter("localhost:8443", true, CAS_LOGIN_URL);
        this.filter.doFilter(request, response, filterChain);

        assertEquals(CAS_LOGIN_URL
                + "?service="
                + URLEncoder.encode("https://localhost:8443"
                + request.getRequestURI() + "?" + request.getQueryString(),
                "UTF-8"), response.getRedirectedUrl());
    }

    public void testAssertion() throws Exception {
        final MockHttpSession session = new MockHttpSession();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final FilterChain filterChain = new FilterChain() {

            public void doFilter(ServletRequest arg0, ServletResponse arg1)
                    throws IOException, ServletException {
                // nothing to do
            }
        };

        request.setSession(session);
        session.setAttribute(AbstractCasFilter.CONST_ASSERTION,
                new AssertionImpl(new SimplePrincipal("test")));
        this.filter.doFilter(request, response, filterChain);

        assertNull(response.getRedirectedUrl());
    }

    public void testRenew() throws Exception {
        final MockHttpSession session = new MockHttpSession();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final FilterChain filterChain = new FilterChain() {

            public void doFilter(ServletRequest arg0, ServletResponse arg1)
                    throws IOException, ServletException {
                // nothing to do
            }
        };

        this.filter = new AuthenticationFilter("localhost:8443", true, CAS_LOGIN_URL);
        this.filter.setRenew(true);
        request.setSession(session);
        this.filter.doFilter(request, response, filterChain);

        assertNotNull(response.getRedirectedUrl());
        assertTrue(response.getRedirectedUrl().indexOf("renew=true") != -1);
    }

    public void testGateway() throws Exception {
        final MockHttpSession session = new MockHttpSession();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final FilterChain filterChain = new FilterChain() {

            public void doFilter(ServletRequest arg0, ServletResponse arg1)
                    throws IOException, ServletException {
                // nothing to do
            }
        };

        request.setSession(session);
        this.filter = new AuthenticationFilter("localhost:8443", true, CAS_LOGIN_URL);
        this.filter.setRenew(true);
        this.filter.setGateway(true);;
        this.filter.doFilter(request, response, filterChain);
        assertNotNull(session.getAttribute(AbstractCasFilter.CONST_GATEWAY));
        assertNotNull(response.getRedirectedUrl());

        final MockHttpServletResponse response2 = new MockHttpServletResponse();
        this.filter.doFilter(request, response2, filterChain);
        assertNull(session.getAttribute(AbstractCasFilter.CONST_GATEWAY));
        assertNull(response2.getRedirectedUrl());
    }
}
