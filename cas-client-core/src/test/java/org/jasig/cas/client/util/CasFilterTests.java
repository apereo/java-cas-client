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

import static org.junit.Assert.assertTrue;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.jasig.cas.client.Protocol;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.2.1
 */
public final class CasFilterTests {

    @Test
    public void serverName() {
        final String serverNameWithoutSlash = "http://www.cnn.com";
        final String serverNameWithSlash = "http://www.cnn.com/";

        final TestCasFilter testCasFilter = new TestCasFilter();
        testCasFilter.setServerName(serverNameWithoutSlash);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        request.setContextPath("/cas");
        request.setRequestURI("/cas/test");

        assertTrue(testCasFilter.constructServiceUrl(request, response).startsWith("http://www.cnn.com/cas/test"));

        testCasFilter.setServerName(serverNameWithSlash);
        assertTrue(testCasFilter.constructServiceUrl(request, response).startsWith("http://www.cnn.com/cas/test"));
    }

    private static class TestCasFilter extends AbstractCasFilter {

        public TestCasFilter() {
            super(Protocol.CAS2);
        }
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
                ServletException {
            // nothing to do
        }
    }
}
