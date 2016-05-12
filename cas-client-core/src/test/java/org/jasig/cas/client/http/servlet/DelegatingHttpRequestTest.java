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
package org.jasig.cas.client.http.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.jasig.cas.client.http.ClientSession;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;


/**
 * Unit tests for {@link DelegatingHttpRequest}.
 *
 * @author Carl Harris
 */
public class DelegatingHttpRequestTest {

    private static final String MOCK_PARAMETER = "someParameter";
    private static final String MOCK_HEADER = "X-Some-Header";
    private static final String MOCK_VALUE = "some value";
    private static final String MOCK_QUERY_STRING = "query";
    private static final int MOCK_PORT = -1;
    private static final String MOCK_USER = "some user";


    private MockHttpServletRequest delegate = new MockHttpServletRequest();

    private DelegatingHttpRequest request = new DelegatingHttpRequest(delegate);

    @Test(expected = RuntimeException.class)
    public void testDelegateRequired() throws Exception {
        new DelegatingHttpRequest(null);
    }

    @Test
    public void testGetMethod() throws Exception {
        assertNotNull(delegate.getMethod());
        assertEquals(delegate.getMethod(), request.getMethod());
    }

    @Test
    public void testGetRequestURL() throws Exception {
        delegate.setScheme("http");
        delegate.setServerName("localhost");
        delegate.setServerPort(80);
        delegate.setRequestURI("/path");
        assertEquals("http://localhost:80/path", request.getRequestURL());
    }

    @Test
    public void testGetRequestURI() throws Exception {
        delegate.setRequestURI("/path");
        assertEquals("/path", request.getRequestURI());
    }

    @Test
    public void testGetParameter() throws Exception {
        delegate.addParameter(MOCK_PARAMETER, MOCK_VALUE);
        assertEquals(MOCK_VALUE, request.getParameter(MOCK_PARAMETER));
    }

    @Test
    public void testGetContentType() throws Exception {
        delegate.setContentType(MOCK_VALUE);
        assertEquals(MOCK_VALUE, request.getContentType());
    }

    @Test
    public void testGetHeader() throws Exception {
        delegate.addHeader(MOCK_HEADER, MOCK_VALUE);
        assertEquals(MOCK_VALUE, request.getHeader(MOCK_HEADER));
    }

    @Test
    public void testGetQueryString() throws Exception {
        delegate.setQueryString(MOCK_QUERY_STRING);
        assertEquals(MOCK_QUERY_STRING, request.getQueryString());
    }

    @Test
    public void testGetSession() throws Exception {
        assertNull(delegate.getSession(false));
        assertNull(request.getSession());
        final MockHttpSession session = new MockHttpSession();
        delegate.setSession(session);
        final ClientSession clientSession = request.getSession();
        assertNotNull(clientSession);
        assertEquals(session.getId(), clientSession.getId());
    }

    @Test
    public void testGetOrCreateSession() throws Exception {
        final ClientSession clientSession = request.getOrCreateSession();
        assertNotNull(clientSession);
        assertEquals(delegate.getSession().getId(), clientSession.getId());
    }

    @Test
    public void testIsSecure() throws Exception {
        delegate.setSecure(true);
        assertTrue(request.isSecure());
    }

    @Test
    public void testGetServerPort() throws Exception {
        delegate.setServerPort(MOCK_PORT);
        assertEquals(MOCK_PORT, request.getServerPort());
    }

    @Test
    public void testLogout() throws Exception {
        // per servlet 3.0 spec, after HttpServletRequest.logout, the remote user should be null
        delegate.setRemoteUser(MOCK_USER);
        request.logout();
        assertNull(delegate.getRemoteUser());
    }

}
