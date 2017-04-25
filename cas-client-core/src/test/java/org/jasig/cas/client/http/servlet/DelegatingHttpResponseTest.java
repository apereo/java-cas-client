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

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Unit tests for {@link DelegatingHttpResponse}
 *
 * @author Carl Harris
 */
public class DelegatingHttpResponseTest {

    private static final String MOCK_BODY = "some body";
    private static final String MOCK_URL = "http://localhost/";

    private MockHttpServletResponse delegate = new MockHttpServletResponse();
    private DelegatingHttpResponse response = new DelegatingHttpResponse(delegate);

    @Test(expected = RuntimeException.class)
    public void testDelegateRequired() throws Exception {
        new DelegatingHttpResponse(null);
    }

    @Test
    public void testGetWriter() throws Exception {
        response.getWriter().write(MOCK_BODY);
        assertEquals(MOCK_BODY, delegate.getContentAsString());
    }

    @Test
    public void testSendRedirect() throws Exception {
        response.sendRedirect(MOCK_URL);
        assertEquals(MOCK_URL, delegate.getRedirectedUrl());
    }

}
