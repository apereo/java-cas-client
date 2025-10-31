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
package org.apereo.cas.client.session;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import jakarta.servlet.http.HttpSession;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.*;

/**
 * @author Matt Brown <matt.brown@citrix.com>
 * @since 3.2.1
 */
public final class SingleSignOutHandlerTests {

    private static final String ANOTHER_PARAMETER = "anotherParameter";

    private static final String TICKET = "ST-xxxxxxxx";

    private static final String JSONP_CALLBACK_PARAMETER_NAME = "jsonpCallback";

    private static final String LOGOUT_PARAMETER_NAME = "logoutRequest";

    private static final String RELAY_STATE_PARAMETER_NAME = "RelayState";

    private static final String ARTIFACT_PARAMETER_NAME = "ticket2";

    private SingleSignOutHandler handler;

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    @Before
    public void setUp() throws Exception {
        handler = new SingleSignOutHandler();
        handler.setJsonpCallbackParameterName(JSONP_CALLBACK_PARAMETER_NAME);
        handler.setLogoutParameterName(LOGOUT_PARAMETER_NAME);
        handler.setRelayStateParameterName(RELAY_STATE_PARAMETER_NAME);
        handler.setArtifactParameterName(ARTIFACT_PARAMETER_NAME);
        handler.init();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    public void tokenRequestFailsIfNoSession() {
        handler.setEagerlyCreateSessions(false);
        request.setSession(null);
        request.setParameter(ARTIFACT_PARAMETER_NAME, TICKET);
        request.setQueryString(ARTIFACT_PARAMETER_NAME + "=" + TICKET);
        assertTrue(handler.process(request, response));
        final var storage = handler.getSessionMappingStorage();
        assertNull(storage.removeSessionByMappingId(TICKET));
    }

    @Test
    public void tokenRequestFailsIfBadParameter() {
        final HttpSession session = new MockHttpSession();
        request.setSession(session);
        request.setParameter(ANOTHER_PARAMETER, TICKET);
        request.setQueryString(ANOTHER_PARAMETER + "=" + TICKET);
        assertTrue(handler.process(request, response));
        final var storage = handler.getSessionMappingStorage();
        assertNull(storage.removeSessionByMappingId(TICKET));
    }

    @Test
    public void tokenRequestOK() {
        final HttpSession session = new MockHttpSession();
        request.setSession(session);
        request.setParameter(ARTIFACT_PARAMETER_NAME, TICKET);
        request.setQueryString(ARTIFACT_PARAMETER_NAME + "=" + TICKET);
        assertTrue(handler.process(request, response));
        final var storage = handler.getSessionMappingStorage();
        assertEquals(session, storage.removeSessionByMappingId(TICKET));
    }

    @Test
    public void backChannelLogoutFailsIfMultipart() {
        final var logoutMessage = LogoutMessageGenerator.generateBackChannelLogoutMessage(TICKET);
        request.setParameter(LOGOUT_PARAMETER_NAME, logoutMessage);
        request.setMethod("POST");
        request.setContentType("multipart/form-data");
        final var session = new MockHttpSession();
        handler.getSessionMappingStorage().addSessionById(TICKET, session);
        assertTrue(handler.process(request, response));
        assertFalse(session.isInvalid());
    }

    @Test
    public void backChannelLogoutFailsIfNoSessionIndex() {
        final var logoutMessage = LogoutMessageGenerator.generateBackChannelLogoutMessage("");
        request.setParameter(LOGOUT_PARAMETER_NAME, logoutMessage);
        request.setMethod("POST");
        final var session = new MockHttpSession();
        handler.getSessionMappingStorage().addSessionById(TICKET, session);
        assertFalse(handler.process(request, response));
        assertFalse(session.isInvalid());
    }

    @Test
    public void backChannelLogoutOK() {
        final var session = doBackChannelLogout();
        assertFalse(handler.process(request, response));
        assertTrue(session.isInvalid());
    }

    @Test
    public void backChannelLogoutDoesNotRunIfPathIsNotEligibleForLogout() {
        handler.setLogoutCallbackPath("/logout");
        request.setServletPath("/not-a-logout");
        final var session = doBackChannelLogout();
        assertTrue(handler.process(request, response));
        assertFalse(session.isInvalid());
    }

    @Test
    public void backChannelLogoutRunsIfPathEqualsLogoutPath() {
        handler.setLogoutCallbackPath("/logout");
        request.setServletPath("/logout");
        final var session = doBackChannelLogout();
        assertFalse(handler.process(request, response));
        assertTrue(session.isInvalid());
    }

    @Test
    public void frontChannelLogoutFailsIfBadParameter() {
        final var logoutMessage = LogoutMessageGenerator.generateFrontChannelLogoutMessage(TICKET);
        request.setParameter(ANOTHER_PARAMETER, logoutMessage);
        request.setMethod("GET");
        request.setQueryString(ANOTHER_PARAMETER + "=" + logoutMessage);
        final var session = new MockHttpSession();
        handler.getSessionMappingStorage().addSessionById(TICKET, session);
        assertTrue(handler.process(request, response));
        assertFalse(session.isInvalid());
    }

    @Test
    public void frontChannelLogoutFailsIfNoSessionIndex() {
        final var logoutMessage = LogoutMessageGenerator.generateFrontChannelLogoutMessage("");
        request.setParameter(LOGOUT_PARAMETER_NAME, logoutMessage);
        request.setQueryString(LOGOUT_PARAMETER_NAME + "=" + logoutMessage);
        request.setMethod("GET");
        final var session = new MockHttpSession();
        handler.getSessionMappingStorage().addSessionById(TICKET, session);
        assertFalse(handler.process(request, response));
        assertFalse(session.isInvalid());
    }

    @Test
    public void frontChannelLogoutOK() {
        final var logoutMessage = LogoutMessageGenerator.generateFrontChannelLogoutMessage(TICKET);
        request.setParameter(LOGOUT_PARAMETER_NAME, logoutMessage);
        request.setQueryString(LOGOUT_PARAMETER_NAME + "=" + logoutMessage);
        request.setMethod("GET");
        final var session = new MockHttpSession();
        handler.getSessionMappingStorage().addSessionById(TICKET, session);
        assertFalse(handler.process(request, response));
        assertTrue(session.isInvalid());
        assertNull(response.getRedirectedUrl());
    }

    @Test
    public void frontChannelLogoutRelayStateOK() {
        final var logoutMessage = LogoutMessageGenerator.generateFrontChannelLogoutMessage(TICKET);
        request.setParameter(LOGOUT_PARAMETER_NAME, logoutMessage);
        request.setParameter(RELAY_STATE_PARAMETER_NAME, TICKET);
        request.setQueryString(LOGOUT_PARAMETER_NAME + "=" + logoutMessage + "&" + RELAY_STATE_PARAMETER_NAME + "=" + TICKET);
        request.setMethod("GET");
        final var session = new MockHttpSession();
        handler.getSessionMappingStorage().addSessionById(TICKET, session);
        assertFalse(handler.process(request, response));
        assertTrue(session.isInvalid());
    }

    @Test
    public void frontChannelLogoutCallbackOK() throws UnsupportedEncodingException {
        final var logoutMessage = LogoutMessageGenerator.generateFrontChannelLogoutMessage(TICKET);
        request.setParameter(LOGOUT_PARAMETER_NAME, logoutMessage);
        request.setParameter(JSONP_CALLBACK_PARAMETER_NAME, "testCallback");
        request.setQueryString(LOGOUT_PARAMETER_NAME + "=" + logoutMessage + "&" + JSONP_CALLBACK_PARAMETER_NAME + "=testCallback");
        request.setMethod("GET");
        final var session = new MockHttpSession();
        handler.getSessionMappingStorage().addSessionById(TICKET, session);
        assertFalse(handler.process(request, response));
        assertEquals("application/javascript", response.getContentType());
        assertEquals("testCallback(true)", response.getContentAsString());
        assertTrue(session.isInvalid());
    }

    @Test
    public void frontChannelLogoutCallbackNotPresentOK() throws UnsupportedEncodingException {
        final var logoutMessage = LogoutMessageGenerator.generateFrontChannelLogoutMessage(TICKET);
        request.setParameter(LOGOUT_PARAMETER_NAME, logoutMessage);
        request.setQueryString(LOGOUT_PARAMETER_NAME + "=" + logoutMessage);
        request.setMethod("GET");
        final var session = new MockHttpSession();
        handler.getSessionMappingStorage().addSessionById(TICKET, session);
        assertFalse(handler.process(request, response));
        assertTrue(response.getContentAsString().isEmpty());
        assertTrue(session.isInvalid());
    }

    private MockHttpSession doBackChannelLogout() {
        final var logoutMessage = LogoutMessageGenerator.generateBackChannelLogoutMessage(TICKET);
        request.setParameter(LOGOUT_PARAMETER_NAME, logoutMessage);
        request.setMethod("POST");
        final var session = new MockHttpSession();
        handler.getSessionMappingStorage().addSessionById(TICKET, session);
        return session;
    }
}
