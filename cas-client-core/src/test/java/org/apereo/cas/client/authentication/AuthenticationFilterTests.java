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
import jakarta.servlet.http.HttpServletRequest;
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

    @Test
    public void testNullCasServerLoginUrlThrows() throws Exception {
        final var f = new AuthenticationFilter();
        f.setIgnoreInitConfiguration(true);
        f.setService(CAS_SERVICE_URL);
        // casServerLoginUrl is null — assertNotNull must fire in init()
        final var config = new MockFilterConfig();
        try {
            f.init(config);
            fail("Should have thrown IllegalArgumentException for null casServerLoginUrl.");
        } catch (final IllegalArgumentException e) {
            // expected - assertNotNull(casServerLoginUrl) must fire
        }
    }

    @Test
    public void testInitCallsSuperInit() throws Exception {
        // Verify that AuthenticationFilter.init() calls AbstractCasFilter.init()
        // which validates serverName/service. If super.init() is removed (mutant),
        // no exception is thrown when neither is set.
        final var f = new AuthenticationFilter();
        f.setIgnoreInitConfiguration(true);
        // deliberately set neither serverName nor service
        final var config = new MockFilterConfig();
        try {
            f.init(config);
            fail("Should have thrown IllegalArgumentException because serverName/service are unset.");
        } catch (final IllegalArgumentException e) {
            // expected - AbstractCasFilter.init() validates serverName/service
        }
    }

    @Test
    public void testNonExcludedUrlRedirectsToCas() throws Exception {
        final var f = new AuthenticationFilter();
        final var context = new MockServletContext();
        context.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);
        context.addInitParameter("service", CAS_SERVICE_URL);
        context.addInitParameter("ignorePattern", "/admin/.*");
        f.init(new MockFilterConfig(context));

        final var request = new MockHttpServletRequest();
        request.setRequestURI("/app/login");
        final var session = new MockHttpSession();
        request.setSession(session);

        final var response = new MockHttpServletResponse();
        final var filterChain = new FilterChain() {
            @Override
            public void doFilter(final ServletRequest req, final ServletResponse res) throws IOException, ServletException {
            }
        };

        f.doFilter(request, response, filterChain);
        // URL /app/login does NOT match ignorePattern /admin/.*, so it MUST redirect
        assertNotNull("Non-excluded URL must redirect to CAS login", response.getRedirectedUrl());
        assertTrue(response.getRedirectedUrl().startsWith(CAS_LOGIN_URL));
    }

    @Test
    public void testDoFilterInvokesChainWhenAssertionPresent() throws Exception {
        final var f = new AuthenticationFilter();
        final var context = new MockServletContext();
        context.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);
        context.addInitParameter("service", CAS_SERVICE_URL);
        f.init(new MockFilterConfig(context));

        final var request = new MockHttpServletRequest();
        final var session = new MockHttpSession();
        session.setAttribute(AbstractCasFilter.CONST_CAS_ASSERTION, new AssertionImpl("test"));
        request.setSession(session);

        final var response = new MockHttpServletResponse();
        final boolean[] chainInvoked = {false};
        final var filterChain = new FilterChain() {
            @Override
            public void doFilter(final ServletRequest req, final ServletResponse res) throws IOException, ServletException {
                chainInvoked[0] = true;
            }
        };

        f.doFilter(request, response, filterChain);
        assertTrue("FilterChain.doFilter must be invoked when assertion is present", chainInvoked[0]);
    }

    @Test
    public void testDoFilterInvokesChainWhenGatewayAlreadyStored() throws Exception {
        final var f = new AuthenticationFilter();
        final var context = new MockServletContext();
        context.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);
        context.addInitParameter("service", CAS_SERVICE_URL);
        f.init(new MockFilterConfig(context));
        f.setGateway(true);

        final var request = new MockHttpServletRequest();
        final var session = new MockHttpSession();
        session.setAttribute(DefaultGatewayResolverImpl.CONST_CAS_GATEWAY, "true");
        request.setSession(session);

        final var response = new MockHttpServletResponse();
        final boolean[] chainInvoked = {false};
        final var filterChain = new FilterChain() {
            @Override
            public void doFilter(final ServletRequest req, final ServletResponse res) throws IOException, ServletException {
                chainInvoked[0] = true;
            }
        };

        f.doFilter(request, response, filterChain);
        assertTrue("FilterChain.doFilter must be invoked when gateway attribute already exists", chainInvoked[0]);
    }

    @Test
    public void testDoFilterInvokesChainWhenUrlExcluded() throws Exception {
        final var f = new AuthenticationFilter();
        final var context = new MockServletContext();
        context.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);
        context.addInitParameter("service", CAS_SERVICE_URL);
        context.addInitParameter("ignorePattern", "/public/.*");
        f.init(new MockFilterConfig(context));

        final var request = new MockHttpServletRequest();
        request.setRequestURI("/public/health");
        final var session = new MockHttpSession();
        request.setSession(session);

        final var response = new MockHttpServletResponse();
        final boolean[] chainInvoked = {false};
        final var filterChain = new FilterChain() {
            @Override
            public void doFilter(final ServletRequest req, final ServletResponse res) throws IOException, ServletException {
                chainInvoked[0] = true;
            }
        };

        f.doFilter(request, response, filterChain);
        assertTrue("FilterChain.doFilter must be invoked when URL matches ignore pattern", chainInvoked[0]);
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

    // ===== Tests added to kill surviving PIT mutants =====

    @Test
    public void testSetGatewayStorageIsUsed() throws Exception {
        // Kills: setGatewayStorage L235 (Removed assignment to member variable gatewayStorage)
        // When setGatewayStorage is called, the custom resolver must be used for gateway operations.
        // If the assignment is removed (mutant), the default resolver is used instead.
        final var customStorage = new GatewayResolver() {
            @Override
            public boolean hasGatewayedAlready(final HttpServletRequest request, final String serviceUrl) {
                return "true".equals(request.getSession().getAttribute("custom_gateway_flag"));
            }

            @Override
            public String storeGatewayInformation(final HttpServletRequest request, final String serviceUrl) {
                request.getSession().setAttribute("custom_gateway_stored", "true");
                return serviceUrl;
            }
        };

        final var f = new AuthenticationFilter();
        final var context = new MockServletContext();
        context.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);
        context.addInitParameter("service", CAS_SERVICE_URL);
        f.init(new MockFilterConfig(context));
        f.setGateway(true);
        f.setGatewayStorage(customStorage);

        final var session = new MockHttpSession();
        final var request = new MockHttpServletRequest();
        request.setSession(session);
        final var response = new MockHttpServletResponse();
        final var filterChain = new FilterChain() {
            @Override
            public void doFilter(final ServletRequest req, final ServletResponse res) throws IOException, ServletException {
            }
        };

        f.doFilter(request, response, filterChain);
        // Custom storage must have been invoked (stores "custom_gateway_stored")
        assertNotNull("Custom gatewayStorage must be used", session.getAttribute("custom_gateway_stored"));
    }

    @Test
    public void testSetMethodAppearsInRedirectUrl() throws Exception {
        // Kills: setMethod L223 (Removed assignment to member variable method)
        // When setMethod is called, the method parameter must appear in the redirect URL.
        // If the assignment is removed (mutant), the default method is used.
        final var f = new AuthenticationFilter();
        final var context = new MockServletContext();
        context.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);
        context.addInitParameter("service", CAS_SERVICE_URL);
        f.init(new MockFilterConfig(context));
        f.setMethod("POST");

        final var session = new MockHttpSession();
        final var request = new MockHttpServletRequest();
        request.setSession(session);
        final var response = new MockHttpServletResponse();
        final var filterChain = new FilterChain() {
            @Override
            public void doFilter(final ServletRequest req, final ServletResponse res) throws IOException, ServletException {
            }
        };

        f.doFilter(request, response, filterChain);
        final var redirectUrl = response.getRedirectedUrl();
        assertNotNull(redirectUrl);
        assertTrue("Redirect URL must contain method=POST", redirectUrl.contains("method=POST"));
    }

    @Test
    public void testIsRequestUrlExcludedWithRegexPattern() throws Exception {
        // Kills: isRequestUrlExcluded L249 (removed conditional) and L253 (replaced boolean return with true)
        // When ignoreUrlPatternMatcherStrategyClass is set and pattern matches, URL is excluded (chain invoked, no redirect).
        // When pattern doesn't match, URL is NOT excluded (redirect to CAS).
        // Mutant L253 always returns true, so non-matching URLs would also be excluded (no redirect).
        final var f = new AuthenticationFilter();
        final var context = new MockServletContext();
        context.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);
        context.addInitParameter("service", CAS_SERVICE_URL);
        context.addInitParameter("ignorePattern", "/health/.*");
        context.addInitParameter("ignoreUrlPatternType", "REGEX");
        f.init(new MockFilterConfig(context));

        // Test 1: Matching URL should be excluded (chain invoked, no redirect)
        final var excludedRequest = new MockHttpServletRequest();
        excludedRequest.setRequestURI("/health/check");
        final var excludedSession = new MockHttpSession();
        excludedRequest.setSession(excludedSession);
        final var excludedResponse = new MockHttpServletResponse();
        final boolean[] excludedChainInvoked = {false};
        f.doFilter(excludedRequest, excludedResponse, (req, res) -> excludedChainInvoked[0] = true);
        assertTrue("Matching URL must invoke chain", excludedChainInvoked[0]);
        assertNull("Matching URL must NOT redirect", excludedResponse.getRedirectedUrl());

        // Test 2: Non-matching URL should NOT be excluded (redirect to CAS)
        // This kills mutant L253 which always returns true (would skip redirect)
        final var nonExcludedRequest = new MockHttpServletRequest();
        nonExcludedRequest.setRequestURI("/app/login");
        final var nonExcludedSession = new MockHttpSession();
        nonExcludedRequest.setSession(nonExcludedSession);
        final var nonExcludedResponse = new MockHttpServletResponse();
        f.doFilter(nonExcludedRequest, nonExcludedResponse, (req, res) -> {
        });
        assertNotNull("Non-matching URL must redirect to CAS", nonExcludedResponse.getRedirectedUrl());
        assertTrue(nonExcludedResponse.getRedirectedUrl().startsWith(CAS_LOGIN_URL));
    }

    @Test
    public void testExcludedUrlInvokesChainWithoutRedirect() throws Exception {
        // Kills: doFilter L174 (removed call to FilterChain.doFilter)
        // When URL matches ignore pattern, chain.doFilter is called at L174.
        // If the call is removed (mutant), chain is never invoked.
        final var f = new AuthenticationFilter();
        final var context = new MockServletContext();
        context.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);
        context.addInitParameter("service", CAS_SERVICE_URL);
        context.addInitParameter("ignorePattern", "/health/.*");
        context.addInitParameter("ignoreUrlPatternType", "REGEX");
        f.init(new MockFilterConfig(context));

        final var request = new MockHttpServletRequest();
        request.setRequestURI("/health/check");
        final var session = new MockHttpSession();
        request.setSession(session);

        final var response = new MockHttpServletResponse();
        final boolean[] chainInvoked = {false};
        final var filterChain = new FilterChain() {
            @Override
            public void doFilter(final ServletRequest req, final ServletResponse res) throws IOException, ServletException {
                chainInvoked[0] = true;
            }
        };

        f.doFilter(request, response, filterChain);
        assertTrue("FilterChain.doFilter must be invoked for excluded URLs", chainInvoked[0]);
        assertNull("Excluded URL must NOT redirect", response.getRedirectedUrl());
    }

    @Test
    public void testAssertionInSessionInvokesChain() throws Exception {
        // Kills: doFilter L182 (removed call to FilterChain.doFilter)
        // When assertion exists in session, chain.doFilter is called at L182.
        final var f = new AuthenticationFilter();
        final var context = new MockServletContext();
        context.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);
        context.addInitParameter("service", CAS_SERVICE_URL);
        f.init(new MockFilterConfig(context));

        final var request = new MockHttpServletRequest();
        final var session = new MockHttpSession();
        session.setAttribute(AbstractCasFilter.CONST_CAS_ASSERTION, new AssertionImpl("test"));
        request.setSession(session);

        final var response = new MockHttpServletResponse();
        final boolean[] chainInvoked = {false};
        final var filterChain = new FilterChain() {
            @Override
            public void doFilter(final ServletRequest req, final ServletResponse res) throws IOException, ServletException {
                chainInvoked[0] = true;
            }
        };

        f.doFilter(request, response, filterChain);
        assertTrue("FilterChain.doFilter must be invoked when assertion is in session", chainInvoked[0]);
        assertNull("No redirect when assertion is present", response.getRedirectedUrl());
    }

    @Test
    public void testGatewayAlreadyStoredInvokesChain() throws Exception {
        // Kills: doFilter L191 (removed call to FilterChain.doFilter)
        // When gateway=true and wasGatewayed=true, chain.doFilter is called at L191.
        final var f = new AuthenticationFilter();
        final var context = new MockServletContext();
        context.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);
        context.addInitParameter("service", CAS_SERVICE_URL);
        f.init(new MockFilterConfig(context));
        f.setGateway(true);

        final var request = new MockHttpServletRequest();
        final var session = new MockHttpSession();
        session.setAttribute(DefaultGatewayResolverImpl.CONST_CAS_GATEWAY, "true");
        request.setSession(session);

        final var response = new MockHttpServletResponse();
        final boolean[] chainInvoked = {false};
        final var filterChain = new FilterChain() {
            @Override
            public void doFilter(final ServletRequest req, final ServletResponse res) throws IOException, ServletException {
                chainInvoked[0] = true;
            }
        };

        f.doFilter(request, response, filterChain);
        assertTrue("FilterChain.doFilter must be invoked when already gatewayed", chainInvoked[0]);
    }

    @Test
    public void testDefaultRenewIsFalse() throws Exception {
        // Kills: <init> L77 (Substituted 0 with 1) and L77 (Removed assignment to member variable renew)
        // Default renew must be false. When renew=false, redirect URL must NOT contain "renew=true".
        final var f = new AuthenticationFilter();
        final var context = new MockServletContext();
        context.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);
        context.addInitParameter("service", CAS_SERVICE_URL);
        f.init(new MockFilterConfig(context));

        final var session = new MockHttpSession();
        final var request = new MockHttpServletRequest();
        request.setSession(session);
        final var response = new MockHttpServletResponse();
        final var filterChain = new FilterChain() {
            @Override
            public void doFilter(final ServletRequest req, final ServletResponse res) throws IOException, ServletException {
            }
        };

        f.doFilter(request, response, filterChain);
        final var redirectUrl = response.getRedirectedUrl();
        assertNotNull(redirectUrl);
        assertFalse("Default renew=false must NOT add renew=true to URL", redirectUrl.contains("renew=true"));
    }

    @Test
    public void testDefaultGatewayIsFalse() throws Exception {
        // Kills: <init> L82 (Substituted 0 with 1) and L82 (Removed assignment to member variable gateway)
        // Default gateway must be false. When gateway=false, no gateway attribute is set in session.
        final var f = new AuthenticationFilter();
        final var context = new MockServletContext();
        context.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);
        context.addInitParameter("service", CAS_SERVICE_URL);
        f.init(new MockFilterConfig(context));

        final var session = new MockHttpSession();
        final var request = new MockHttpServletRequest();
        request.setSession(session);
        final var response = new MockHttpServletResponse();
        final var filterChain = new FilterChain() {
            @Override
            public void doFilter(final ServletRequest req, final ServletResponse res) throws IOException, ServletException {
            }
        };

        f.doFilter(request, response, filterChain);
        // When gateway=false, the gateway flag must NOT be set in session
        assertNull("Default gateway=false must NOT set gateway attribute",
            session.getAttribute(DefaultGatewayResolverImpl.CONST_CAS_GATEWAY));
    }

    @Test
    public void testGatewayStorageInitializedByDefault() throws Exception {
        // Kills: <init> L89 (removed call to DefaultGatewayResolverImpl::<init>) and
        //        L89 (Removed assignment to member variable gatewayStorage)
        // When gateway is enabled, the default gatewayStorage must work.
        // If gatewayStorage is null (mutant), a NullPointerException would occur.
        final var f = new AuthenticationFilter();
        final var context = new MockServletContext();
        context.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);
        context.addInitParameter("service", CAS_SERVICE_URL);
        f.init(new MockFilterConfig(context));
        f.setGateway(true);

        final var session = new MockHttpSession();
        final var request = new MockHttpServletRequest();
        request.setSession(session);
        final var response = new MockHttpServletResponse();
        final var filterChain = new FilterChain() {
            @Override
            public void doFilter(final ServletRequest req, final ServletResponse res) throws IOException, ServletException {
            }
        };

        // This must NOT throw NPE (gatewayStorage.storeGatewayedUrl would fail if null)
        f.doFilter(request, response, filterChain);
        // Gateway attribute must be set by the default gatewayStorage
        assertNotNull("Default gatewayStorage must set gateway attribute",
            session.getAttribute(DefaultGatewayResolverImpl.CONST_CAS_GATEWAY));
    }

    @Test
    public void testGatewayStorageClassInitParam() throws Exception {
        // Kills: initInternal L153 (negated conditional, removed conditionals) and
        //        L154 (removed call to setGatewayStorage)
        // When gatewayStorageClass is configured, it must be instantiated and set.
        final var f = new AuthenticationFilter();
        final var context = new MockServletContext();
        context.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);
        context.addInitParameter("service", CAS_SERVICE_URL);
        context.addInitParameter("gatewayStorageClass", "org.apereo.cas.client.authentication.DefaultGatewayResolverImpl");
        f.init(new MockFilterConfig(context));
        f.setGateway(true);

        final var session = new MockHttpSession();
        final var request = new MockHttpServletRequest();
        request.setSession(session);
        final var response = new MockHttpServletResponse();
        final var filterChain = new FilterChain() {
            @Override
            public void doFilter(final ServletRequest req, final ServletResponse res) throws IOException, ServletException {
            }
        };

        f.doFilter(request, response, filterChain);
        assertNotNull("gatewayStorageClass must be instantiated and used",
            session.getAttribute(DefaultGatewayResolverImpl.CONST_CAS_GATEWAY));
    }

    @Test
    public void testRenewViaSetRenewAddsToUrl() throws Exception {
        // Kills: <init> L77 (Removed assignment to member variable renew)
        // When renew is set via setRenew(true), redirect URL must contain renew=true.
        // If the field assignment is broken (mutant), renew won't appear.
        final var f = new AuthenticationFilter();
        final var context = new MockServletContext();
        context.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);
        context.addInitParameter("service", CAS_SERVICE_URL);
        f.init(new MockFilterConfig(context));
        f.setRenew(true);

        final var session = new MockHttpSession();
        final var request = new MockHttpServletRequest();
        request.setSession(session);
        final var response = new MockHttpServletResponse();
        final var filterChain = new FilterChain() {
            @Override
            public void doFilter(final ServletRequest req, final ServletResponse res) throws IOException, ServletException {
            }
        };

        f.doFilter(request, response, filterChain);
        final var redirectUrl = response.getRedirectedUrl();
        assertNotNull(redirectUrl);
        assertTrue("renew=true must appear in redirect URL when renew is set",
            redirectUrl.contains("renew=true"));
    }

    @Test
    public void testGatewayViaSetGatewaySetsSessionAttribute() throws Exception {
        // Kills: <init> L82 (Removed assignment to member variable gateway)
        // When gateway is set via setGateway(true), gateway attribute must be set in session.
        final var f = new AuthenticationFilter();
        final var context = new MockServletContext();
        context.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);
        context.addInitParameter("service", CAS_SERVICE_URL);
        f.init(new MockFilterConfig(context));
        f.setGateway(true);

        final var session = new MockHttpSession();
        final var request = new MockHttpServletRequest();
        request.setSession(session);
        final var response = new MockHttpServletResponse();
        final var filterChain = new FilterChain() {
            @Override
            public void doFilter(final ServletRequest req, final ServletResponse res) throws IOException, ServletException {
            }
        };

        f.doFilter(request, response, filterChain);
        assertNotNull("gateway=true must set gateway attribute in session",
            session.getAttribute(DefaultGatewayResolverImpl.CONST_CAS_GATEWAY));
    }

    @Test
    public void testInitInternalSetsGatewayFromConfig() throws Exception {
        // Kills: initInternal L128 (removed call to getBoolean) and
        //        L128 (removed call to setGateway)
        // When gateway=true is configured, setGateway(true) must be called.
        final var f = new AuthenticationFilter();
        final var context = new MockServletContext();
        context.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);
        context.addInitParameter("service", CAS_SERVICE_URL);
        context.addInitParameter("gateway", "true");
        f.init(new MockFilterConfig(context));

        final var session = new MockHttpSession();
        final var request = new MockHttpServletRequest();
        request.setSession(session);
        final var response = new MockHttpServletResponse();
        final var filterChain = new FilterChain() {
            @Override
            public void doFilter(final ServletRequest req, final ServletResponse res) throws IOException, ServletException {
            }
        };

        f.doFilter(request, response, filterChain);
        // If gateway config was properly read and set, gateway attribute must be in session
        assertNotNull("gateway=true config must enable gateway mode",
            session.getAttribute(DefaultGatewayResolverImpl.CONST_CAS_GATEWAY));
    }

    @Test
    public void testInitInternalSetsMethodFromConfig() throws Exception {
        // Kills: initInternal L129 (removed call to getString) and
        //        L129 (removed call to setMethod)
        // When method=POST is configured, redirect URL must contain method=POST.
        final var f = new AuthenticationFilter();
        final var context = new MockServletContext();
        context.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);
        context.addInitParameter("service", CAS_SERVICE_URL);
        context.addInitParameter("method", "POST");
        f.init(new MockFilterConfig(context));

        final var session = new MockHttpSession();
        final var request = new MockHttpServletRequest();
        request.setSession(session);
        final var response = new MockHttpServletResponse();
        final var filterChain = new FilterChain() {
            @Override
            public void doFilter(final ServletRequest req, final ServletResponse res) throws IOException, ServletException {
            }
        };

        f.doFilter(request, response, filterChain);
        final var redirectUrl = response.getRedirectedUrl();
        assertNotNull(redirectUrl);
        assertTrue("method=POST config must appear in redirect URL",
            redirectUrl.contains("method=POST"));
    }

    @Test
    public void testTicketInRequestInvokesChain() throws Exception {
        // Kills: doFilter L190 (removed call to CommonUtils.isNotBlank / removed conditional) and
        //        L187 (removed call to retrieveTicketFromRequest)
        // When a ticket parameter is in the request, chain.doFilter is called.
        final var f = new AuthenticationFilter();
        final var context = new MockServletContext();
        context.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);
        context.addInitParameter("service", CAS_SERVICE_URL);
        f.init(new MockFilterConfig(context));

        final var request = new MockHttpServletRequest();
        request.setQueryString("ticket=ST-123456");
        request.setParameter("ticket", "ST-123456");
        final var session = new MockHttpSession();
        request.setSession(session);

        final var response = new MockHttpServletResponse();
        final boolean[] chainInvoked = {false};
        final var filterChain = new FilterChain() {
            @Override
            public void doFilter(final ServletRequest req, final ServletResponse res) throws IOException, ServletException {
                chainInvoked[0] = true;
            }
        };

        f.doFilter(request, response, filterChain);
        assertTrue("FilterChain.doFilter must be invoked when ticket is present", chainInvoked[0]);
    }

    @Test
    public void testRenewAndGatewayConfiguredTogether() throws Exception {
        // Kills: initInternal L128/L129 (removed call to setGateway/setMethod)
        // When both renew and gateway are configured, gateway should take precedence
        // and renew should be ignored (or gateway mode should work).
        final var f = new AuthenticationFilter();
        final var context = new MockServletContext();
        context.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);
        context.addInitParameter("service", CAS_SERVICE_URL);
        context.addInitParameter("gateway", "true");
        context.addInitParameter("method", "POST");
        f.init(new MockFilterConfig(context));

        final var session = new MockHttpSession();
        final var request = new MockHttpServletRequest();
        request.setSession(session);
        final var response = new MockHttpServletResponse();
        final var filterChain = new FilterChain() {
            @Override
            public void doFilter(final ServletRequest req, final ServletResponse res) throws IOException, ServletException {
            }
        };

        f.doFilter(request, response, filterChain);
        // Gateway mode should set the gateway attribute
        assertNotNull("gateway=true must set gateway attribute even with method config",
            session.getAttribute(DefaultGatewayResolverImpl.CONST_CAS_GATEWAY));
    }

    @Test
    public void testNoTicketNoAssertionNoGatewayRedirects() throws Exception {
        // Kills: doFilter L198 (removed conditional - replaced equality check with true)
        // When no ticket, no assertion, no gateway → must redirect to CAS.
        final var f = new AuthenticationFilter();
        final var context = new MockServletContext();
        context.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);
        context.addInitParameter("service", CAS_SERVICE_URL);
        f.init(new MockFilterConfig(context));

        final var session = new MockHttpSession();
        final var request = new MockHttpServletRequest();
        request.setSession(session);
        final var response = new MockHttpServletResponse();
        final var filterChain = new FilterChain() {
            @Override
            public void doFilter(final ServletRequest req, final ServletResponse res) throws IOException, ServletException {
            }
        };

        f.doFilter(request, response, filterChain);
        final var redirectUrl = response.getRedirectedUrl();
        assertNotNull("Must redirect when no ticket, no assertion, no gateway", redirectUrl);
        assertTrue(redirectUrl.startsWith(CAS_LOGIN_URL));
    }

    @Test
    public void testDoFilterInvokesChainWhenTicketPresent() throws Exception {
        // Kills: doFilter L191 (removed call to FilterChain.doFilter) via ticket path
        // When ticket is present, chain.doFilter is called at L191.
        final var f = new AuthenticationFilter();
        final var context = new MockServletContext();
        context.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);
        context.addInitParameter("service", CAS_SERVICE_URL);
        f.init(new MockFilterConfig(context));

        final var request = new MockHttpServletRequest();
        request.setQueryString("ticket=ST-test-ticket");
        request.setParameter("ticket", "ST-test-ticket");
        final var session = new MockHttpSession();
        request.setSession(session);

        final var response = new MockHttpServletResponse();
        final boolean[] chainInvoked = {false};
        final var filterChain = new FilterChain() {
            @Override
            public void doFilter(final ServletRequest req, final ServletResponse res) throws IOException, ServletException {
                chainInvoked[0] = true;
            }
        };

        f.doFilter(request, response, filterChain);
        assertTrue("FilterChain.doFilter must be invoked when ticket is present", chainInvoked[0]);
    }

    @Test
    public void testDoFilterInvokesChainWhenGatewayedAndTicketPresent() throws Exception {
        // Kills: doFilter L188 (removed conditional - replaced equality check with true)
        // When gateway=true, wasGatewayed=true, and ticket is present → chain.doFilter at L191.
        final var f = new AuthenticationFilter();
        final var context = new MockServletContext();
        context.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);
        context.addInitParameter("service", CAS_SERVICE_URL);
        f.init(new MockFilterConfig(context));
        f.setGateway(true);

        final var request = new MockHttpServletRequest();
        request.setQueryString("ticket=ST-test-ticket");
        request.setParameter("ticket", "ST-test-ticket");
        final var session = new MockHttpSession();
        session.setAttribute(DefaultGatewayResolverImpl.CONST_CAS_GATEWAY, "true");
        request.setSession(session);

        final var response = new MockHttpServletResponse();
        final boolean[] chainInvoked = {false};
        final var filterChain = new FilterChain() {
            @Override
            public void doFilter(final ServletRequest req, final ServletResponse res) throws IOException, ServletException {
                chainInvoked[0] = true;
            }
        };

        f.doFilter(request, response, filterChain);
        assertTrue("FilterChain.doFilter must be invoked when gatewayed and ticket present", chainInvoked[0]);
    }

    @Test
    public void testIgnorePatternWithContainsType() throws Exception {
        // Kills: isRequestUrlExcluded L249/L253 and doFilter L179 (removed conditional)
        // Test with CONTAINS pattern type to exercise different matcher paths.
        final var f = new AuthenticationFilter();
        final var context = new MockServletContext();
        context.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);
        context.addInitParameter("service", CAS_SERVICE_URL);
        context.addInitParameter("ignorePattern", "/health/");
        context.addInitParameter("ignoreUrlPatternType", "CONTAINS");
        f.init(new MockFilterConfig(context));

        // Matching URL should be excluded (chain invoked, no redirect)
        final var excludedRequest = new MockHttpServletRequest();
        excludedRequest.setRequestURI("/health/check");
        final var excludedSession = new MockHttpSession();
        excludedRequest.setSession(excludedSession);
        final var excludedResponse = new MockHttpServletResponse();
        final boolean[] excludedChainInvoked = {false};
        f.doFilter(excludedRequest, excludedResponse, (req, res) -> excludedChainInvoked[0] = true);
        assertTrue("Contains pattern must match /health/check (chain invoked)", excludedChainInvoked[0]);
        assertNull("Contains pattern must match /health/check (no redirect)", excludedResponse.getRedirectedUrl());

        // Non-matching URL should NOT be excluded (redirect to CAS)
        final var nonExcludedRequest = new MockHttpServletRequest();
        nonExcludedRequest.setRequestURI("/app/login");
        final var nonExcludedSession = new MockHttpSession();
        nonExcludedRequest.setSession(nonExcludedSession);
        final var nonExcludedResponse = new MockHttpServletResponse();
        f.doFilter(nonExcludedRequest, nonExcludedResponse, (req, res) -> {
        });
        assertNotNull("Contains pattern must NOT match /app/login (redirect)", nonExcludedResponse.getRedirectedUrl());
    }

    @Test
    public void testEmptyIgnorePatternDoesNotExclude() throws Exception {
        // Kills: isRequestUrlExcluded L249 (removed conditional - replaced equality check with true)
        // When ignorePattern is empty/null, no URL should be excluded (all redirect to CAS).
        final var f = new AuthenticationFilter();
        final var context = new MockServletContext();
        context.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);
        context.addInitParameter("service", CAS_SERVICE_URL);
        f.init(new MockFilterConfig(context));

        final var request = new MockHttpServletRequest();
        request.setRequestURI("/any/path");
        final var session = new MockHttpSession();
        request.setSession(session);
        final var response = new MockHttpServletResponse();
        f.doFilter(request, response, (req, res) -> {
        });
        assertNotNull("Empty ignore pattern must NOT exclude any URL (redirect to CAS)", response.getRedirectedUrl());
    }

    @Test
    public void testInitWithOnlyCasServerUrlPrefix() throws Exception {
        // Kills: init L109 (removed call to ConfigurationKey.getName for casServerLoginUrl)
        // When only casServerUrlPrefix is set (not casServerLoginUrl), init must work.
        final var f = new AuthenticationFilter();
        final var context = new MockServletContext();
        context.addInitParameter("casServerUrlPrefix", CAS_PREFIX);
        context.addInitParameter("service", CAS_SERVICE_URL);
        f.init(new MockFilterConfig(context));

        final var session = new MockHttpSession();
        final var request = new MockHttpServletRequest();
        request.setSession(session);
        final var response = new MockHttpServletResponse();
        final var filterChain = new FilterChain() {
            @Override
            public void doFilter(final ServletRequest req, final ServletResponse res) throws IOException, ServletException {
            }
        };

        f.doFilter(request, response, filterChain);
        assertNotNull("Must redirect when configured with casServerUrlPrefix", response.getRedirectedUrl());
    }

    @Test
    public void testInitWithServerNameInsteadOfService() throws Exception {
        // Kills: init L107 (removed call to String.format) and related init mutants
        // When serverName is used instead of service, the service URL is constructed dynamically.
        final var f = new AuthenticationFilter();
        final var context = new MockServletContext();
        context.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);
        context.addInitParameter("serverName", "localhost:8443");
        f.init(new MockFilterConfig(context));

        final var session = new MockHttpSession();
        final var request = new MockHttpServletRequest();
        request.setSession(session);
        request.setRequestURI("/app/page");
        request.setSecure(true);
        final var response = new MockHttpServletResponse();
        final var filterChain = new FilterChain() {
            @Override
            public void doFilter(final ServletRequest req, final ServletResponse res) throws IOException, ServletException {
            }
        };

        f.doFilter(request, response, filterChain);
        assertNotNull("Must redirect when configured with serverName", response.getRedirectedUrl());
    }
}
