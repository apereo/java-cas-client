/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.client.authentication;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLEncoder;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.validation.AssertionImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.*;

/**
 * Tests for the AuthenticationFilter.
 *
 * @author Scott Battaglia
 * @since 3.0
 */
public final class AuthenticationFilterTests {

    private static final String CAS_SERVICE_URL = "https://localhost:8443/service";

    private static final String CAS_LOGIN_URL = "https://localhost:8443/cas/login";

    private AuthenticationFilter filter;

    @Before
    public void setUp() throws Exception {
        this.filter = new AuthenticationFilter();
        final MockFilterConfig config = new MockFilterConfig();
        config.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);
        config.addInitParameter("service", CAS_SERVICE_URL);
        this.filter.init(config);
    }

    @After
    public void tearDown() throws Exception {
        this.filter.destroy();
    }

    @Test
    public void testRedirect() throws Exception {
        final MockHttpSession session = new MockHttpSession();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final FilterChain filterChain = new FilterChain() {

            public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
                // nothing to do
            }
        };

        request.setSession(session);
        this.filter.doFilter(request, response, filterChain);

        assertEquals(CAS_LOGIN_URL + "?service=" + URLEncoder.encode(CAS_SERVICE_URL, "UTF-8"),
                response.getRedirectedUrl());
    }

    @Test
    public void testRedirectWithQueryString() throws Exception {
        final MockHttpSession session = new MockHttpSession();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        request.setQueryString("test=12456");
        request.setRequestURI("/test");
        request.setSecure(true);
        final FilterChain filterChain = new FilterChain() {

            public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
                // nothing to do
            }
        };

        request.setSession(session);
        this.filter = new AuthenticationFilter();

        final MockFilterConfig config = new MockFilterConfig();
        config.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);
        config.addInitParameter("serverName", "localhost:8443");
        this.filter.init(config);

        this.filter.doFilter(request, response, filterChain);

        assertEquals(
                CAS_LOGIN_URL
                        + "?service="
                        + URLEncoder.encode(
                                "https://localhost:8443" + request.getRequestURI() + "?" + request.getQueryString(),
                                "UTF-8"), response.getRedirectedUrl());
    }

    @Test
    public void testAssertion() throws Exception {
        final MockHttpSession session = new MockHttpSession();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final FilterChain filterChain = new FilterChain() {

            public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
                // nothing to do
            }
        };

        request.setSession(session);
        session.setAttribute(AbstractCasFilter.CONST_CAS_ASSERTION, new AssertionImpl("test"));
        this.filter.doFilter(request, response, filterChain);

        assertNull(response.getRedirectedUrl());
    }

    @Test
    public void testRenew() throws Exception {
        final MockHttpSession session = new MockHttpSession();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final FilterChain filterChain = new FilterChain() {

            public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
                // nothing to do
            }
        };

        this.filter.setRenew(true);
        request.setSession(session);
        this.filter.doFilter(request, response, filterChain);

        assertNotNull(response.getRedirectedUrl());
        assertTrue(response.getRedirectedUrl().indexOf("renew=true") != -1);
    }

    @Test
    public void testGateway() throws Exception {
        final MockHttpSession session = new MockHttpSession();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final FilterChain filterChain = new FilterChain() {

            public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
                // nothing to do
            }
        };

        request.setSession(session);
        this.filter.setRenew(true);
        this.filter.setGateway(true);
        this.filter.doFilter(request, response, filterChain);
        assertNotNull(session.getAttribute(DefaultGatewayResolverImpl.CONST_CAS_GATEWAY));
        assertNotNull(response.getRedirectedUrl());

        final MockHttpServletResponse response2 = new MockHttpServletResponse();
        this.filter.doFilter(request, response2, filterChain);
        assertNull(session.getAttribute(DefaultGatewayResolverImpl.CONST_CAS_GATEWAY));
        assertNull(response2.getRedirectedUrl());
    }

    @Test
    public void testRenewInitParamThrows() throws Exception {
        final AuthenticationFilter f = new AuthenticationFilter();
        final MockFilterConfig config = new MockFilterConfig();
        config.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);
        config.addInitParameter("service", CAS_SERVICE_URL);
        config.addInitParameter("renew", "true");
        try {
            f.init(config);
            fail("Should have thrown IllegalArgumentException.");
        } catch (final IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Renew MUST"));
        }
    }

    @Test
    public void testAllowsRenewContextParam() throws Exception {
        final AuthenticationFilter f = new AuthenticationFilter();
        final MockServletContext context = new MockServletContext();
        context.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);
        context.addInitParameter("service", CAS_SERVICE_URL);
        context.addInitParameter("renew", "true");
        f.init(new MockFilterConfig(context));
        final Field renewField = AuthenticationFilter.class.getDeclaredField("renew");
        renewField.setAccessible(true);
        assertTrue((Boolean) renewField.get(f));
    }

    @Test
    public void customRedirectStrategy() throws Exception {
        final AuthenticationFilter f = new AuthenticationFilter();
        final MockServletContext context = new MockServletContext();
        context.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);
        context.addInitParameter("service", CAS_SERVICE_URL);
        context.addInitParameter("authenticationRedirectStrategyClass",
                "org.jasig.cas.client.authentication.FacesCompatibleAuthenticationRedirectStrategy");
        f.init(new MockFilterConfig(context));
    }
    
    @Test
    public void testIgnorePatterns() throws Exception {
        final AuthenticationFilter f = new AuthenticationFilter();
        final MockServletContext context = new MockServletContext();
        context.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);
        
        context.addInitParameter("ignorePattern", "=valueTo(\\w+)");
        context.addInitParameter("service", CAS_SERVICE_URL);
        f.init(new MockFilterConfig(context));
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final String URL = CAS_SERVICE_URL + "?param=valueToIgnore";
        request.setRequestURI(URL);
        
        final MockHttpSession session = new MockHttpSession();
        request.setSession(session);
        
        final MockHttpServletResponse response = new MockHttpServletResponse();

        final FilterChain filterChain = new FilterChain() {
            public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
            }
        };

        f.doFilter(request, response, filterChain);
        assertNull(response.getRedirectedUrl());
    }
    
    @Test
    public void testIgnorePatternsWithContainsMatching() throws Exception {
        final AuthenticationFilter f = new AuthenticationFilter();
        final MockServletContext context = new MockServletContext();
        context.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);
        
        context.addInitParameter("ignorePattern", "=valueToIgnore");
        context.addInitParameter("ignoreUrlPatternType", "CONTAINS");
        context.addInitParameter("service", CAS_SERVICE_URL);
        f.init(new MockFilterConfig(context));
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final String URL = CAS_SERVICE_URL + "?param=valueToIgnore";
        request.setRequestURI(URL);
        
        final MockHttpSession session = new MockHttpSession();
        request.setSession(session);
        
        final MockHttpServletResponse response = new MockHttpServletResponse();

        final FilterChain filterChain = new FilterChain() {
            public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
            }
        };

        f.doFilter(request, response, filterChain);
        assertNull(response.getRedirectedUrl());
    }
    
    @Test
    public void testIgnorePatternsWithExactMatching() throws Exception {
        final AuthenticationFilter f = new AuthenticationFilter();
        final MockServletContext context = new MockServletContext();
        context.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);
        
        final URL url = new URL(CAS_SERVICE_URL + "?param=valueToIgnore");
        
        context.addInitParameter("ignorePattern", url.toExternalForm());
        context.addInitParameter("ignoreUrlPatternType", "EXACT");
        context.addInitParameter("service", CAS_SERVICE_URL);
        f.init(new MockFilterConfig(context));
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme(url.getProtocol());
        request.setServerName(url.getHost());
        request.setServerPort(url.getPort());
        request.setQueryString(url.getQuery());
        request.setRequestURI(url.getPath());
        
        final MockHttpSession session = new MockHttpSession();
        request.setSession(session);
        
        final MockHttpServletResponse response = new MockHttpServletResponse();

        final FilterChain filterChain = new FilterChain() {
            public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
            }
        };

        f.doFilter(request, response, filterChain);
        assertNull(response.getRedirectedUrl());
    }
    
    @Test
    public void testIgnorePatternsWithExactClassname() throws Exception {
        final AuthenticationFilter f = new AuthenticationFilter();
        final MockServletContext context = new MockServletContext();
        context.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);
        
        context.addInitParameter("ignorePattern", "=valueToIgnore");
        context.addInitParameter("ignoreUrlPatternType", ContainsPatternUrlPatternMatcherStrategy.class.getName());
        context.addInitParameter("service", CAS_SERVICE_URL);
        f.init(new MockFilterConfig(context));
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final String URL = CAS_SERVICE_URL + "?param=valueToIgnore";
        request.setRequestURI(URL);
        
        final MockHttpSession session = new MockHttpSession();
        request.setSession(session);
        
        final MockHttpServletResponse response = new MockHttpServletResponse();

        final FilterChain filterChain = new FilterChain() {
            public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
            }
        };

        f.doFilter(request, response, filterChain);
        assertNull(response.getRedirectedUrl());
    }
    
    @Test
    public void testIgnorePatternsWithInvalidClassname() throws Exception {
        final AuthenticationFilter f = new AuthenticationFilter();
        final MockServletContext context = new MockServletContext();
        context.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);
        
        context.addInitParameter("ignorePattern", "=valueToIgnore");
        context.addInitParameter("ignoreUrlPatternType", "unknown.class.name");
        context.addInitParameter("service", CAS_SERVICE_URL);
        f.init(new MockFilterConfig(context));
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final String URL = CAS_SERVICE_URL + "?param=valueToIgnore";
        request.setRequestURI(URL);
        
        final MockHttpSession session = new MockHttpSession();
        request.setSession(session);
        
        final MockHttpServletResponse response = new MockHttpServletResponse();

        final FilterChain filterChain = new FilterChain() {
            public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
            }
        };

        f.doFilter(request, response, filterChain);
        System.out.println(response.getRedirectedUrl());
    }
}
