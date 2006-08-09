/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.client.web.filter;

import org.jasig.cas.client.validation.Assertion;
import org.springframework.web.util.WebUtils;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

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

    final class CasHttpServletRequestWrapper implements
            HttpServletRequest {

        private final HttpServletRequest request;

        CasHttpServletRequestWrapper(final HttpServletRequest request) {
            this.request = request;
        }

        public String getAuthType() {
            return this.request.getAuthType();
        }

        public Cookie[] getCookies() {
            return this.request.getCookies();
        }

        public long getDateHeader(String s) {
            return this.request.getDateHeader(s);
        }

        public String getHeader(String s) {
            return this.request.getHeader(s);
        }

        public Enumeration getHeaders(String s) {
            return this.request.getHeaders(s);
        }

        public Enumeration getHeaderNames() {
            return this.request.getHeaderNames();
        }

        public int getIntHeader(String s) {
            return this.request.getIntHeader(s);
        }

        public String getMethod() {
            return this.request.getMethod();
        }

        public String getPathInfo() {
            return this.request.getPathInfo();
        }

        public String getPathTranslated() {
            return this.request.getPathTranslated();
        }

        public String getContextPath() {
            return this.request.getContextPath();
        }

        public String getQueryString() {
            return this.request.getQueryString();
        }

        public String getRemoteUser() {
            final org.jasig.cas.authentication.principal.Principal p = (org.jasig.cas.authentication.principal.Principal) this.request
                    .getAttribute(AbstractCasFilter.CONST_PRINCIPAL);

            if (p != null) {
                return p.getId();
            }

            final Assertion assertion = (Assertion) WebUtils
                    .getSessionAttribute(this.request,
                            AbstractCasFilter.CONST_ASSERTION);

            if (assertion != null) {
                return assertion.getPrincipal().getId();
            }

            return null;
        }

        public boolean isUserInRole(String s) {
            return this.request.isUserInRole(s);
        }

        public Principal getUserPrincipal() {
            return this.request.getUserPrincipal();
        }

        public String getRequestedSessionId() {
            return this.request.getRequestedSessionId();
        }

        public String getRequestURI() {
            return this.request.getRequestURI();
        }

        public StringBuffer getRequestURL() {
            return this.request.getRequestURL();
        }

        public String getServletPath() {
            return this.request.getServletPath();
        }

        public HttpSession getSession(boolean b) {
            return this.request.getSession(b);
        }

        public HttpSession getSession() {
            return this.request.getSession();
        }

        public boolean isRequestedSessionIdValid() {
            return this.request.isRequestedSessionIdValid();
        }

        public boolean isRequestedSessionIdFromCookie() {
            return this.request.isRequestedSessionIdFromCookie();
        }

        public boolean isRequestedSessionIdFromURL() {
            return this.request.isRequestedSessionIdFromURL();
        }

        public boolean isRequestedSessionIdFromUrl() {
            return this.request.isRequestedSessionIdFromUrl();
        }

        public Object getAttribute(String s) {
            return this.request.getAttribute(s);
        }

        public Enumeration getAttributeNames() {
            return this.request.getAttributeNames();
        }

        public String getCharacterEncoding() {
            return this.request.getCharacterEncoding();
        }

        public void setCharacterEncoding(String s)
                throws UnsupportedEncodingException {
            this.request.setCharacterEncoding(s);
        }

        public int getContentLength() {
            return this.request.getContentLength();
        }

        public String getContentType() {
            return this.request.getContentType();
        }

        public ServletInputStream getInputStream() throws IOException {
            return this.request.getInputStream();
        }

        public String getParameter(String s) {
            return this.request.getParameter(s);
        }

        public Enumeration getParameterNames() {
            return this.request.getParameterNames();
        }

        public String[] getParameterValues(String s) {
            return this.request.getParameterValues(s);
        }

        public Map getParameterMap() {
            return this.request.getParameterMap();
        }

        public String getProtocol() {
            return this.request.getProtocol();
        }

        public String getScheme() {
            return this.request.getScheme();
        }

        public String getServerName() {
            return this.request.getServerName();
        }

        public int getServerPort() {
            return this.request.getServerPort();
        }

        public BufferedReader getReader() throws IOException {
            return this.request.getReader();
        }

        public String getRemoteAddr() {
            return this.request.getRemoteAddr();
        }

        public String getRemoteHost() {
            return this.request.getRemoteHost();
        }

        public void setAttribute(String s, Object o) {
            this.request.setAttribute(s, o);
        }

        public void removeAttribute(String s) {
            this.request.removeAttribute(s);
        }

        public Locale getLocale() {
            return this.request.getLocale();
        }

        public Enumeration getLocales() {
            return this.request.getLocales();
        }

        public boolean isSecure() {
            return this.request.isSecure();
        }

        public RequestDispatcher getRequestDispatcher(String s) {
            return this.request.getRequestDispatcher(s);
        }

        public String getRealPath(String s) {
            return this.request.getRealPath(s);
        }

        public int getRemotePort() {
            return this.request.getRemotePort();
        }

        public String getLocalName() {
            return this.request.getLocalName();
        }

        public String getLocalAddr() {
            return this.request.getLocalAddr();
        }

        public int getLocalPort() {
            return this.request.getLocalPort();
        }
    }
}
