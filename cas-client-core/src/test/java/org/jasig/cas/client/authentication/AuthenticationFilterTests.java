/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.cas.client.authentication;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.validation.AssertionImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import javax.servlet.FilterChain;
import java.net.URLEncoder;
import java.util.regex.Pattern;

/**
 * Tests for the AuthenticationFilter.
 *
 * @author Scott Battaglia
 * @version $Revision: 11753 $ $Date: 2007-01-03 13:37:26 -0500 (Wed, 03 Jan 2007) $
 * @since 3.0
 */
public final class AuthenticationFilterTests {

    private static final String CAS_SERVICE_URL = "https://localhost:8443/service";

    private static final String CAS_LOGIN_URL = "https://localhost:8443/cas/login";

    private AuthenticationFilter filter;

    final MockHttpSession session = new MockHttpSession();
    final MockHttpServletRequest request = new MockHttpServletRequest();
    final MockHttpServletResponse response = new MockHttpServletResponse();
    final FilterChain filterChain = mock(FilterChain.class);

    @Before
    public void setUp() throws Exception {
        this.filter = new AuthenticationFilter();
        final MockFilterConfig config = new MockFilterConfig();
        config.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);
        config.addInitParameter("service", "https://localhost:8443/service");
        request.setSession(session);
        this.filter.init(config);
    }

    @After
    public void tearDown() throws Exception {
        this.filter.destroy();
    }

    @Test
    public void testRedirect() throws Exception {
        this.filter.doFilter(request, response, filterChain);

        assertEquals(CAS_LOGIN_URL + "?service=" + URLEncoder.encode(CAS_SERVICE_URL, "UTF-8"),
                response.getRedirectedUrl());
    }

    @Test
    public void testRedirectWithQueryString() throws Exception {
        request.setQueryString("test=12456");
        request.setRequestURI("/test");
        request.setSecure(true);
        this.filter = new AuthenticationFilter();

        final MockFilterConfig config = new MockFilterConfig();
        config.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);
        config.addInitParameter("serverName", "localhost:8443");
        this.filter.init(config);

        this.filter.doFilter(request, response, filterChain);

        assertEquals(
                CAS_LOGIN_URL
                        + "?service="
                        + URLEncoder.encode("https://localhost:8443" + request.getRequestURI() + "?"
                                + request.getQueryString(), "UTF-8"), response.getRedirectedUrl());
    }

    @Test
    public void testAssertion() throws Exception {
        session.setAttribute(AbstractCasFilter.CONST_CAS_ASSERTION, new AssertionImpl("test"));
        this.filter.doFilter(request, response, filterChain);

        assertNull(response.getRedirectedUrl());
    }

    @Test
    public void testRenew() throws Exception {
        this.filter.setRenew(true);
        this.filter.doFilter(request, response, filterChain);

        assertNotNull(response.getRedirectedUrl());
        assertTrue(response.getRedirectedUrl().indexOf("renew=true") != -1);
    }

    @Test
    public void testGateway() throws Exception {
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
    public void testInitExcludeURL_SinglePattern() throws Exception {
        final MockFilterConfig config = new MockFilterConfig();
        config.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);
        config.addInitParameter("serverName", "localhost:8443");
        config.addInitParameter(AuthenticationFilter.EXCLUDE_PARAMETERS_INIT_PARAM, ".*\\.action");
        this.filter.init(config);

        assertNotNull(filter.getExcludePatterns());
        assertEquals("Expected a single pattern but received: " + filter.getExcludePatterns(), 1,
                filter.getExcludePatterns().length);
    }

    @Test
    public void testInitExcludeURL_MultiplePattern() throws Exception {
        final MockFilterConfig config = new MockFilterConfig();
        config.addInitParameter("casServerLoginUrl", CAS_LOGIN_URL);
        config.addInitParameter("serverName", "localhost:8443");
        config.addInitParameter(AuthenticationFilter.EXCLUDE_PARAMETERS_INIT_PARAM, ".*\\.action,.*/ajax/.*");
        this.filter.init(config);

        assertNotNull(filter.getExcludePatterns());
        assertEquals("Expected a two patterns but received: " + filter.getExcludePatterns(), 2,
                filter.getExcludePatterns().length);
    }

    @Test
    public void testExcludedUrl_SingleException() throws Exception {
        request.setServletPath("/ajax/details.action");
        filter.setExcludePatterns(new Pattern[]{Pattern.compile(".*/ajax/.*")});
        filter.doFilter(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void testExcludedUrl_MultipleException() throws Exception {
        request.setServletPath("/ajax/details.action");
        filter.setExcludePatterns(new Pattern[]{Pattern.compile(".*\\.jsp"), Pattern.compile(".*/ajax/.*")});
        filter.doFilter(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void testNotExcludedUrl() throws Exception {
        request.setServletPath("/ajax/details.action");
        filter.setExcludePatterns(new Pattern[]{Pattern.compile(".*\\.jsp"), Pattern.compile(".*/protected/.*")});
        filter.doFilter(request, response, filterChain);
        verify(filterChain, never()).doFilter(request, response);
    }
}
