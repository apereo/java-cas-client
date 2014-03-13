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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

/**
 * @author Matt Brown <matt.brown@citrix.com>
 * @version $Revision$ $Date$
 * @since 3.2.1
 */
public final class SingleSignOutHandlerTests {

    private final static String ANOTHER_PARAMETER = "anotherParameter";
    private final static String TICKET = "ST-xxxxxxxx";

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
    public void isBackChannelLogoutRequest() throws Exception {
        request.setParameter(logoutParameterName, TICKET);
        request.setMethod("POST");

        assertTrue(handler.isBackChannelLogoutRequest(request));
    }

    /**
     * Tests that a multipart request is not considered logoutRequest. Verifies issue CASC-147.
     *
     * @throws Exception
     */
    @Test
    public void isBackChannelLogoutRequestMultipart() throws Exception {
        request.setParameter(logoutParameterName, TICKET);
        request.setMethod("POST");
        request.setContentType("multipart/form-data");

        assertFalse(handler.isBackChannelLogoutRequest(request));
    }

    @Test
    public void isFrontChannelLogoutRequest() {
        request.setParameter(logoutParameterName, TICKET);
        request.setMethod("GET");
        request.setQueryString(logoutParameterName + "=" + TICKET);
        
        assertTrue(handler.isFrontChannelLogoutRequest(request));
    }

    @Test
    public void isFrontChannelLogoutRequestKO() {
        request.setParameter(ANOTHER_PARAMETER, TICKET);
        request.setMethod("GET");
        request.setQueryString(ANOTHER_PARAMETER + "=" + TICKET);
        
        assertFalse(handler.isFrontChannelLogoutRequest(request));
    }
    
    @Test
    public void recordSessionKOIfNoSession() {
        handler.setEagerlyCreateSessions(false);
        request.setSession(null);
        request.setParameter(SingleSignOutHandler.DEFAULT_ARTIFACT_PARAMETER_NAME, TICKET);
        request.setQueryString(SingleSignOutHandler.DEFAULT_ARTIFACT_PARAMETER_NAME + "=" + TICKET);
        handler.recordSession(request);
        final SessionMappingStorage storage = handler.getSessionMappingStorage();
        assertNull(storage.removeSessionByMappingId(TICKET));
    }

    @Test
    public void recordSessionOK() {
        final MockHttpSession session = new MockHttpSession();
        request.setSession(session);
        request.setParameter(SingleSignOutHandler.DEFAULT_ARTIFACT_PARAMETER_NAME, TICKET);
        request.setQueryString(SingleSignOutHandler.DEFAULT_ARTIFACT_PARAMETER_NAME + "=" + TICKET);
        handler.recordSession(request);
        final SessionMappingStorage storage = handler.getSessionMappingStorage();
        assertEquals(session, storage.removeSessionByMappingId(TICKET));
    }
    
    @Test
    public void destorySessionPOSTKONoSessionIndex() {
        final String logoutMessage = LogoutMessageGenerator.generateLogoutMessage("");
        request.setParameter(logoutParameterName, logoutMessage);
        request.setMethod("POST");
        final MockHttpSession session = new MockHttpSession();
        handler.getSessionMappingStorage().addSessionById(TICKET, session);
        handler.destroySession(request);
        assertFalse(session.isInvalid());
    }

    @Test
    public void destorySessionPOST() {
        final String logoutMessage = LogoutMessageGenerator.generateLogoutMessage(TICKET);
        request.setParameter(logoutParameterName, logoutMessage);
        request.setMethod("POST");
        final MockHttpSession session = new MockHttpSession();
        handler.getSessionMappingStorage().addSessionById(TICKET, session);
        handler.destroySession(request);
        assertTrue(session.isInvalid());
    }

    @Test
    public void destorySessionGETNoSessionIndex() {
        final String logoutMessage = LogoutMessageGenerator.generateCompressedLogoutMessage("");
        request.setParameter(logoutParameterName, logoutMessage);
        request.setQueryString(logoutParameterName + "=" + logoutMessage);
        request.setMethod("GET");
        final MockHttpSession session = new MockHttpSession();
        handler.getSessionMappingStorage().addSessionById(TICKET, session);
        handler.destroySession(request);
        assertFalse(session.isInvalid());
    }

    @Test
    public void destorySessionGET() {
        final String logoutMessage = LogoutMessageGenerator.generateCompressedLogoutMessage(TICKET);
        request.setParameter(logoutParameterName, logoutMessage);
        request.setQueryString(logoutParameterName + "=" + logoutMessage);
        request.setMethod("GET");
        final MockHttpSession session = new MockHttpSession();
        handler.getSessionMappingStorage().addSessionById(TICKET, session);
        handler.destroySession(request);
        assertTrue(session.isInvalid());
    }
}
