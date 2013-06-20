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
package org.jasig.cas.client.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import junit.framework.TestCase;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.jasig.cas.client.validation.AssertionImpl;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

/**
 * Tests for the HttpServletRequestWrapperFilter.
 *
 * @author Scott Battaglia
 * @version $Revision: 11742 $ $Date: 2007-10-05 14:03:58 -0400 (Thu, 05 Oct 2007) $
 * @since 3.0
 */

public final class HttpServletRequestWrapperFilterTests extends TestCase {

    protected HttpServletRequest mockRequest;

    public void testWrappedRequest() throws Exception {
        final HttpServletRequestWrapperFilter filter = new HttpServletRequestWrapperFilter();
        filter.init(new MockFilterConfig());
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpSession session = new MockHttpSession();

        session.setAttribute(AbstractCasFilter.CONST_CAS_ASSERTION, new AssertionImpl("test"));
        request.setSession(session);

        filter.doFilter(request, new MockHttpServletResponse(), createFilterChain());
        assertEquals("test", this.mockRequest.getRemoteUser());

        filter.destroy();
    }

    public void testIsUserInRole() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpSession session = new MockHttpSession();
        final MockFilterConfig config = new MockFilterConfig();

        config.addInitParameter("roleAttribute", "memberOf");
        final HttpServletRequestWrapperFilter filter = new HttpServletRequestWrapperFilter();
        filter.init(config);

        final Map<String, Object> attributes = new HashMap<String, Object>();
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
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpSession session = new MockHttpSession();
        final MockFilterConfig config = new MockFilterConfig();

        config.addInitParameter("roleAttribute", "groupMembership");
        config.addInitParameter("ignoreCase", "true");
        final HttpServletRequestWrapperFilter filter = new HttpServletRequestWrapperFilter();
        filter.init(config);

        final Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("groupMembership", Arrays.asList(new Object[] { "animals", "ducks" }));
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
        return new FilterChain() {
            public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
                HttpServletRequestWrapperFilterTests.this.mockRequest = (HttpServletRequest) request;
            }

        };
    }
}
