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
package org.jasig.cas.client.session;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.Assert.*;

/**
 * @author Matt Brown <matt.brown@citrix.com>
 * @version $Revision$ $Date$
 * @since 3.2.1
 */
public final class SingleSignoutHandlerTests {

    private SingleSignOutHandler handler;
    private MockHttpServletRequest request;
    private final static String logoutParameterName = "logoutRequest";

    @Before
    public void setUp() throws Exception {
        handler = new SingleSignOutHandler();
        handler.setLogoutParameterName(logoutParameterName);
        handler.init();
        request = new MockHttpServletRequest();
    }

    @Test
    public void isLogoutRequest() throws Exception {
        request.setParameter(logoutParameterName, "true");
        request.setMethod("POST");

        assertTrue(handler.isLogoutRequest(request));
    }

    /**
     * Tests that a multipart request is not considered logoutRequest. Verifies issue CASC-147.
     *
     * @throws Exception
     */
    @Test
    public void isLogoutRequestMultipart() throws Exception {
        request.setParameter(logoutParameterName, "true");
        request.setMethod("POST");
        request.setContentType("multipart/form-data");

        assertFalse(handler.isLogoutRequest(request));
    }

}
