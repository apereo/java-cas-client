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

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;

import static org.junit.Assert.*;

public final class ErrorRedirectFilterTests {

    private static final String REDIRECT_URL = "/ise.html";

    private ErrorRedirectFilter errorRedirectFilter;

    private FilterChain filterChain;


    @Before
    public void setUp() throws Exception {
        this.errorRedirectFilter = new ErrorRedirectFilter();

        final MockFilterConfig filterConfig = new MockFilterConfig();
        filterConfig.addInitParameter(IllegalStateException.class.getName(), REDIRECT_URL);
        this.errorRedirectFilter.init(filterConfig);
        this.filterChain = new MockFilterChain();
    }


    @Test
    public void noRootCause() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();

        // this should be okay as the mock filter chain allows one call
        this.errorRedirectFilter.doFilter(request, response, this.filterChain);

        // this will fail as the mock filter chain will throw IllegalStateException
        this.errorRedirectFilter.doFilter(request, response, this.filterChain);

        assertEquals(REDIRECT_URL, response.getRedirectedUrl());

    }
}
