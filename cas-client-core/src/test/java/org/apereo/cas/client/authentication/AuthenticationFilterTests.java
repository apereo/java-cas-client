/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apereo.cas.client.authentication;

import org.apereo.cas.client.util.AbstractCasFilter;
import org.apereo.cas.client.validation.AssertionImpl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpSession;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;

import static org.junit.Assert.*;

/**
 * Tests for the AuthenticationFilter.
 *
 * @author Scott Battaglia
 * @since 3.0
 */
public final class AuthenticationFilterTests {

    private static final String CAS_SERVICE_URL = "https://localhost:8443/service";

    private static final String CAS_PREFIX = "https://localhost:8443/cas";

    private static final String CAS_LOGIN_URL = CAS_PREFIX + "/login";

    private AuthenticationFilter filter;

    @Before
    public void setUp() throws Exception {
        this.filter = new AuthenticationFilter();
        final var config = new MockFilterConfig();
        config.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);
        config.addInitParameter("service", CAS_SERVICE_URL);
        this.filter.init(config);
    }

    @After
    public void tearDown() throws Exception {
        this.filter.destroy();
    }

    @Test
    public void testRedirectWithLoginUrlConfig() throws Exception {
        doRedirectTest();
    }

    @Test
    public void testRedirectWithCasServerPrefixConfig() throws Exception {
        replaceFilterWithPrefixConfiguredFilter();
        doRedirectTest();
    }

    @Test
    public void testRedirectWithQueryString() throws Exception {
        final HttpSession session = new MockHttpSession();
        final var request = new MockHttpServletRequest();
        final var response = new MockHttpServletResponse();
        request.setQueryString("test=12456");
        request.setRequestURI("/test");
        request.setSecure(true);
        final var filterChain = new FilterChain() {

            @Override
            public void doFilter(final ServletRequest request, final ServletResponse response) throws IOException, ServletException {
                // nothing to do
            }
        };

        request.setSession(session);
        this.filter = new AuthenticationFilter();

        final var config = new MockFilterConfig();
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
        final HttpSession session = new MockHttpSession();
        final var request = new MockHttpServletRequest();
        final var response = new MockHttpServletResponse();
        final var filterChain = new FilterChain() {

            @Override
            public void doFilter(final ServletRequest request, final ServletResponse response) throws IOException, ServletException {
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
        final HttpSession session = new MockHttpSession();
        final var request = new MockHttpServletRequest();
        final var response = new MockHttpServletResponse();
        final var filterChain = new FilterChain() {

            @Override
            public void doFilter(final ServletRequest request, final ServletResponse response) throws IOException, ServletException {
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
        final HttpSession session = new MockHttpSession();
        final var request = new MockHttpServletRequest();
        final var response = new MockHttpServletResponse();
        final var filterChain = new FilterChain() {

            @Override
            public void doFilter(final ServletRequest request, final ServletResponse response) throws IOException, ServletException {
                // nothing to do
            }
        };

        request.setSession(session);
        this.filter.setRenew(true);
        this.filter.setGateway(true);
        this.filter.doFilter(request, response, filterChain);
        assertNotNull(session.getAttribute(DefaultGatewayResolverImpl.CONST_CAS_GATEWAY));
        assertNotNull(response.getRedirectedUrl());

        final var response2 = new MockHttpServletResponse();
        this.filter.doFilter(request, response2, filterChain);
        assertNotNull(session.getAttribute(DefaultGatewayResolverImpl.CONST_CAS_GATEWAY));
        assertNull(response2.getRedirectedUrl());

        final var response3 = new MockHttpServletResponse();
        this.filter.doFilter(request, response3, filterChain);
        assertNotNull(session.getAttribute(DefaultGatewayResolverImpl.CONST_CAS_GATEWAY));
        assertNull(response3.getRedirectedUrl());
    }

    @Test
    public void testRenewInitParamThrows() throws Exception {
        final var f = new AuthenticationFilter();
        final var config = new MockFilterConfig();
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
        final var f = new AuthenticationFilter();
        final var context = new MockServletContext();
        context.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);
        context.addInitParameter("service", CAS_SERVICE_URL);
        context.addInitParameter("renew", "true");
        f.init(new MockFilterConfig(context));
        final var renewField = AuthenticationFilter.class.getDeclaredField("renew");
        renewField.setAccessible(true);
        assertTrue((Boolean) renewField.get(f));
    }

    @Test
    public void customRedirectStrategy() throws Exception {
        final var f = new AuthenticationFilter();
        final var context = new MockServletContext();
        context.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);
        context.addInitParameter("service", CAS_SERVICE_URL);
        context.addInitParameter("authenticationRedirectStrategyClass",
            "org.apereocas.client.authentication.FacesCompatibleAuthenticationRedirectStrategy");
        f.init(new MockFilterConfig(context));
    }

    @Test
    public void testIgnorePatterns() throws Exception {
        final var f = new AuthenticationFilter();
        final var context = new MockServletContext();
        context.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);

        context.addInitParameter("ignorePattern", "=valueTo(\\w+)");
        context.addInitParameter("service", CAS_SERVICE_URL);
        f.init(new MockFilterConfig(context));

        final var request = new MockHttpServletRequest();
        final var URL = CAS_SERVICE_URL + "?param=valueToIgnore";
        request.setRequestURI(URL);

        final HttpSession session = new MockHttpSession();
        request.setSession(session);

        final var response = new MockHttpServletResponse();

        final var filterChain = new FilterChain() {
            @Override
            public void doFilter(final ServletRequest request, final ServletResponse response) throws IOException, ServletException {
            }
        };

        f.doFilter(request, response, filterChain);
        assertNull(response.getRedirectedUrl());
    }

    @Test
    public void testIgnorePatternsWithContainsMatching() throws Exception {
        final var f = new AuthenticationFilter();
        final var context = new MockServletContext();
        context.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);

        context.addInitParameter("ignorePattern", "=valueToIgnore");
        context.addInitParameter("ignoreUrlPatternType", "CONTAINS");
        context.addInitParameter("service", CAS_SERVICE_URL);
        f.init(new MockFilterConfig(context));

        final var request = new MockHttpServletRequest();
        final var URL = CAS_SERVICE_URL + "?param=valueToIgnore";
        request.setRequestURI(URL);

        final HttpSession session = new MockHttpSession();
        request.setSession(session);

        final var response = new MockHttpServletResponse();

        final var filterChain = new FilterChain() {
            @Override
            public void doFilter(final ServletRequest request, final ServletResponse response) throws IOException, ServletException {
            }
        };

        f.doFilter(request, response, filterChain);
        assertNull(response.getRedirectedUrl());
    }

    @Test
    public void testIgnorePatternsWithExactMatching() throws Exception {
        final var f = new AuthenticationFilter();
        final var context = new MockServletContext();
        context.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);

        final var url = new URL(CAS_SERVICE_URL + "?param=valueToIgnore");

        context.addInitParameter("ignorePattern", url.toExternalForm());
        context.addInitParameter("ignoreUrlPatternType", "EXACT");
        context.addInitParameter("service", CAS_SERVICE_URL);
        f.init(new MockFilterConfig(context));

        final var request = new MockHttpServletRequest();
        request.setScheme(url.getProtocol());
        request.setServerName(url.getHost());
        request.setServerPort(url.getPort());
        request.setQueryString(url.getQuery());
        request.setRequestURI(url.getPath());

        final HttpSession session = new MockHttpSession();
        request.setSession(session);

        final var response = new MockHttpServletResponse();

        final var filterChain = new FilterChain() {
            @Override
            public void doFilter(final ServletRequest request, final ServletResponse response) throws IOException, ServletException {
            }
        };

        f.doFilter(request, response, filterChain);
        assertNull(response.getRedirectedUrl());
    }

    @Test
    public void testIgnorePatternsWithExactClassname() throws Exception {
        final var f = new AuthenticationFilter();
        final var context = new MockServletContext();
        context.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);

        context.addInitParameter("ignorePattern", "=valueToIgnore");
        context.addInitParameter("ignoreUrlPatternType", ContainsPatternUrlPatternMatcherStrategy.class.getName());
        context.addInitParameter("service", CAS_SERVICE_URL);
        f.init(new MockFilterConfig(context));

        final var request = new MockHttpServletRequest();
        final var URL = CAS_SERVICE_URL + "?param=valueToIgnore";
        request.setRequestURI(URL);

        final HttpSession session = new MockHttpSession();
        request.setSession(session);

        final var response = new MockHttpServletResponse();

        final var filterChain = new FilterChain() {
            @Override
            public void doFilter(final ServletRequest request, final ServletResponse response) throws IOException, ServletException {
            }
        };

        f.doFilter(request, response, filterChain);
        assertNull(response.getRedirectedUrl());
    }

    @Test
    public void testIgnorePatternsWithInvalidClassname() throws Exception {
        final var f = new AuthenticationFilter();
        final var context = new MockServletContext();
        context.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);

        context.addInitParameter("ignorePattern", "=valueToIgnore");
        context.addInitParameter("ignoreUrlPatternType", "unknown.class.name");
        context.addInitParameter("service", CAS_SERVICE_URL);
        f.init(new MockFilterConfig(context));

        final var request = new MockHttpServletRequest();
        final var URL = CAS_SERVICE_URL + "?param=valueToIgnore";
        request.setRequestURI(URL);

        final HttpSession session = new MockHttpSession();
        request.setSession(session);

        final var response = new MockHttpServletResponse();

        final var filterChain = new FilterChain() {
            @Override
            public void doFilter(final ServletRequest request, final ServletResponse response) throws IOException, ServletException {
            }
        };

        f.doFilter(request, response, filterChain);
        System.out.println(response.getRedirectedUrl());
    }

    private void replaceFilterWithPrefixConfiguredFilter() throws ServletException {
        this.filter = new AuthenticationFilter();
        final var config = new MockFilterConfig();
        config.addInitParameter("casServerUrlPrefix", CAS_PREFIX);
        config.addInitParameter("service", CAS_SERVICE_URL);
        this.filter.init(config);
    }

    private void doRedirectTest() throws IOException, ServletException {
        final HttpSession session = new MockHttpSession();
        final var request = new MockHttpServletRequest();
        final var response = new MockHttpServletResponse();
        final var filterChain = new FilterChain() {

            @Override
            public void doFilter(final ServletRequest request, final ServletResponse response) throws IOException, ServletException {
                // nothing to do
            }
        };

        request.setSession(session);
        this.filter.doFilter(request, response, filterChain);

        assertEquals(CAS_LOGIN_URL + "?service=" + URLEncoder.encode(CAS_SERVICE_URL, "UTF-8"),
            response.getRedirectedUrl());
    }
}
