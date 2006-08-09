/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
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
        assertEquals(request.getAttributeNames(), this.mockRequest
                .getAttributeNames());
        assertEquals(request.getAuthType(), this.mockRequest.getAuthType());

        this.mockRequest.setCharacterEncoding("test");

        assertEquals(request.getCharacterEncoding(), this.mockRequest
                .getCharacterEncoding());
        assertNotSame(request.getClass(), this.mockRequest.getClass());
        assertEquals(request.getContentLength(), this.mockRequest
                .getContentLength());
        assertEquals(request.getContentType(), this.mockRequest
                .getContentType());
        assertEquals(request.getContextPath(), this.mockRequest
                .getContextPath());
        assertEquals(request.getCookies(), this.mockRequest.getCookies());
        assertEquals(request.getHeaderNames(), this.mockRequest
                .getHeaderNames());
        assertEquals(request.getInputStream(), this.mockRequest
                .getInputStream());
        assertEquals(request.getLocalAddr(), this.mockRequest.getLocalAddr());
        assertEquals(request.getLocale(), this.mockRequest.getLocale());
        assertEquals(request.getLocales().hasMoreElements(), this.mockRequest
                .getLocales().hasMoreElements());
        assertEquals(request.getLocalName(), this.mockRequest.getLocalName());
        assertEquals(request.getLocalPort(), this.mockRequest.getLocalPort());
        assertEquals(request.getMethod(), this.mockRequest.getMethod());
        assertEquals(request.getParameterMap(), this.mockRequest
                .getParameterMap());
        assertEquals(request.getParameterNames().hasMoreElements(),
                this.mockRequest.getParameterNames().hasMoreElements());
        assertEquals(request.getPathInfo(), this.mockRequest.getPathInfo());
        assertEquals(request.getPathTranslated(), this.mockRequest
                .getPathTranslated());
        assertEquals(request.getProtocol(), this.mockRequest.getProtocol());
        assertEquals(request.getQueryString(), this.mockRequest
                .getQueryString());
        assertEquals(request.getReader(), this.mockRequest.getReader());
        assertEquals(request.getRemoteAddr(), this.mockRequest.getRemoteAddr());
        assertEquals(request.getRemoteHost(), this.mockRequest.getRemoteHost());
        assertEquals(request.getRemotePort(), this.mockRequest.getRemotePort());
        assertEquals(request.getRequestedSessionId(), this.mockRequest
                .getRequestedSessionId());
        assertEquals(request.getRequestURI(), this.mockRequest.getRequestURI());
        assertEquals(request.getRequestURL().toString(), this.mockRequest
                .getRequestURL().toString());
        assertEquals(request.getScheme(), this.mockRequest.getScheme());
        assertEquals(request.getServerName(), this.mockRequest.getServerName());
        assertEquals(request.getServerPort(), this.mockRequest.getServerPort());
        assertEquals(request.getServletPath(), this.mockRequest
                .getServletPath());
        assertEquals(request.getSession(), this.mockRequest.getSession());
        assertEquals(request.getSession(false), this.mockRequest
                .getSession(false));
        assertEquals(request.getUserPrincipal(), this.mockRequest
                .getUserPrincipal());
        assertEquals(request.isRequestedSessionIdFromCookie(), this.mockRequest
                .isRequestedSessionIdFromCookie());
        assertEquals(request.isRequestedSessionIdFromUrl(), this.mockRequest
                .isRequestedSessionIdFromUrl());
        assertEquals(request.isRequestedSessionIdFromURL(), this.mockRequest
                .isRequestedSessionIdFromURL());
        assertEquals(request.isRequestedSessionIdValid(), this.mockRequest
                .isRequestedSessionIdValid());
        assertEquals(request.isSecure(), this.mockRequest.isSecure());
        assertEquals(request.isUserInRole("test"), this.mockRequest
                .isUserInRole("test"));
        assertEquals(request.getDateHeader("test"), this.mockRequest
                .getDateHeader("test"));
        assertEquals(request.getHeader("test"), this.mockRequest
                .getHeader("test"));
        assertEquals(request.getHeaders("test").hasMoreElements(),
                this.mockRequest.getHeaders("test").hasMoreElements());
        assertEquals(request.getIntHeader("test"), this.mockRequest
                .getIntHeader("test"));

        this.mockRequest.setAttribute("test", "test");

        assertEquals(request.getAttribute("test"), this.mockRequest
                .getAttribute("test"));

        this.mockRequest.removeAttribute("test");

        assertEquals(request.getAttribute("test"), this.mockRequest
                .getAttribute("test"));
        assertEquals(request.getParameter("test"), this.mockRequest
                .getParameter("test"));
        assertEquals(request.getParameterValues("test"), this.mockRequest
                .getParameterValues("test"));
        assertEquals(request.getRealPath("test"), this.mockRequest
                .getRealPath("test"));
        assertEquals(request.getRequestDispatcher("test").getClass(),
                this.mockRequest.getRequestDispatcher("test").getClass());

    }
}
