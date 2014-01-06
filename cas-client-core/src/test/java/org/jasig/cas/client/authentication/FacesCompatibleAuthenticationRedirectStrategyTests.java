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
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class FacesCompatibleAuthenticationRedirectStrategyTests {

    private FacesCompatibleAuthenticationRedirectStrategy strategy;

    @Before
    public void setUp() throws Exception {
        this.strategy = new FacesCompatibleAuthenticationRedirectStrategy();
    }

    @Test
    public void didWeRedirect() throws Exception {
        final String redirectUrl = "http://www.jasig.org";
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();

        this.strategy.redirect(request, response, redirectUrl);
        assertEquals(redirectUrl, response.getRedirectedUrl());
    }

    @Test
    public void facesPartialResponse() throws Exception {
        final String redirectUrl = "http://www.jasig.org";
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        request.setParameter("javax.faces.partial.ajax", "true");
        this.strategy.redirect(request, response, redirectUrl);
        assertNull(response.getRedirectedUrl());
        assertTrue(response.getContentAsString().contains(redirectUrl));
    }
}
