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
package org.apereo.cas.client.util;

import org.apereo.cas.client.authentication.AttributePrincipal;
import org.apereo.cas.client.authentication.AttributePrincipalImpl;
import org.apereo.cas.client.validation.AssertionImpl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import junit.framework.TestCase;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests for the HttpServletRequestWrapperFilter.
 *
 * @author Scott Battaglia
 * @since 3.0
 */

public final class HttpServletRequestWrapperFilterTests extends TestCase {

    private HttpServletRequest mockRequest;

    public void testWrappedRequest() throws Exception {
        final var filter = new HttpServletRequestWrapperFilter();
        filter.init(new MockFilterConfig());
        final var request = new MockHttpServletRequest();
        final HttpSession session = new MockHttpSession();

        session.setAttribute(AbstractCasFilter.CONST_CAS_ASSERTION, new AssertionImpl("test"));
        request.setSession(session);

        filter.doFilter(request, new MockHttpServletResponse(), createFilterChain());
        assertEquals("test", this.mockRequest.getRemoteUser());

        filter.destroy();
    }

    public void testIsUserInRole() throws Exception {
        final var request = new MockHttpServletRequest();
        final HttpSession session = new MockHttpSession();
        final var config = new MockFilterConfig();

        config.addInitParameter("roleAttribute", "memberOf");
        final var filter = new HttpServletRequestWrapperFilter();
        filter.init(config);

        final Map<String, Object> attributes = new HashMap<>();
        attributes.put("memberOf", "administrators");
        final AttributePrincipal principal = new AttributePrincipalImpl("alice", attributes);
        session.setAttribute(AbstractCasFilter.CONST_CAS_ASSERTION, new AssertionImpl(principal));

        request.setSession(session);

        filter.doFilter(request, new MockHttpServletResponse(), createFilterChain());
        assertEquals("alice", this.mockRequest.getRemoteUser());
        assertTrue(this.mockRequest.isUserInRole("administrators"));
        assertFalse(this.mockRequest.isUserInRole("ADMINISTRATORS"));
        assertFalse(this.mockRequest.isUserInRole("users"));
        assertFalse(this.mockRequest.isUserInRole(null));

        filter.destroy();
    }

    public void testIsUserInRoleCaseInsensitive() throws Exception {
        final var request = new MockHttpServletRequest();
        final HttpSession session = new MockHttpSession();
        final var config = new MockFilterConfig();

        config.addInitParameter("roleAttribute", "groupMembership");
        config.addInitParameter("ignoreCase", "true");
        final var filter = new HttpServletRequestWrapperFilter();
        filter.init(config);

        final Map<String, Object> attributes = new HashMap<>();
        attributes.put("groupMembership", Arrays.asList(new Object[]{"animals", "ducks"}));
        final AttributePrincipal principal = new AttributePrincipalImpl("daffy", attributes);
        session.setAttribute(AbstractCasFilter.CONST_CAS_ASSERTION, new AssertionImpl(principal));

        request.setSession(session);

        filter.doFilter(request, new MockHttpServletResponse(), createFilterChain());
        assertEquals("daffy", this.mockRequest.getRemoteUser());
        assertTrue(this.mockRequest.isUserInRole("animals"));
        assertTrue(this.mockRequest.isUserInRole("ANIMALS"));
        assertTrue(this.mockRequest.isUserInRole("ducks"));
        assertTrue(this.mockRequest.isUserInRole("DUCKS"));
        assertFalse(this.mockRequest.isUserInRole("varmints"));
        assertFalse(this.mockRequest.isUserInRole(""));

        filter.destroy();
    }

    private FilterChain createFilterChain() {
        return (request, response) -> HttpServletRequestWrapperFilterTests.this.mockRequest = (HttpServletRequest) request;
    }
}
