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
import java.util.List;

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

    // === Tests for surviving PIT mutants ===

    @Test(expected = IllegalArgumentException.class)
    public void initFailsWithNullArtifactParameterName() {
        final SingleSignOutHandler h = new SingleSignOutHandler();
        h.setArtifactParameterName(null);
        h.init();
    }

    @Test(expected = IllegalArgumentException.class)
    public void initFailsWithNullLogoutParameterName() {
        final SingleSignOutHandler h = new SingleSignOutHandler();
        h.setLogoutParameterName(null);
        h.init();
    }

    @Test(expected = IllegalArgumentException.class)
    public void initFailsWithNullRelayStateParameterName() {
        final SingleSignOutHandler h = new SingleSignOutHandler();
        h.setRelayStateParameterName(null);
        h.init();
    }

    @Test(expected = IllegalArgumentException.class)
    public void initFailsWithNullSessionMappingStorage() {
        final SingleSignOutHandler h = new SingleSignOutHandler();
        h.setSessionMappingStorage(null);
        h.init();
    }

    @Test
    public void destroySessionCallsRequestLogout() throws Exception {
        final MockHttpServletRequest req = new MockHttpServletRequest();
        req.setMethod("POST");
        req.setRemoteUser("testUser");
        req.setAuthType("CAS");

        final MockHttpSession session = new MockHttpSession();
        req.setSession(session);
        session.setAttribute("testKey", "testValue");

        final String token = "ST-1234567890";
        handler.getSessionMappingStorage().addSessionById(token, session);

        final String logoutMessage = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>"
            + "<authenticationSuccess>"
            + "<user>testUser</user>"
            + "<SessionIndex>" + token + "</SessionIndex>"
            + "</authenticationSuccess>"
            + "</cas:serviceResponse>";

        req.addParameter(LOGOUT_PARAMETER_NAME, logoutMessage);

        final MockHttpServletResponse resp = new MockHttpServletResponse();
        handler.process(req, resp);

        assertTrue("Session should be invalidated", session.isInvalid());
        assertNull("request.logout() should clear remoteUser", req.getRemoteUser());
        assertNull("request.logout() should clear authType", req.getAuthType());
    }

    @Test
    public void recordSessionRemovesExistingMappingForSameSession() throws Exception {
        final MockHttpServletRequest req = new MockHttpServletRequest();
        final HttpSession session = req.getSession();

        final String oldToken = "ST-old-token";
        final String newToken = "ST-new-token";

        handler.getSessionMappingStorage().addSessionById(oldToken, session);

        req.setParameter(ARTIFACT_PARAMETER_NAME, newToken);
        req.setQueryString(ARTIFACT_PARAMETER_NAME + "=" + newToken);
        handler.process(req, new MockHttpServletResponse());

        assertNull("Old token should be removed when same session gets new token",
            handler.getSessionMappingStorage().removeSessionByMappingId(oldToken));
        assertNotNull("New token should be mapped",
            handler.getSessionMappingStorage().removeSessionByMappingId(newToken));
    }

    // === Additional tests to kill surviving PIT mutants ===

    @Test
    public void destroySessionWithBlankLogoutMessageReturnsFalse() {
        final var session = new MockHttpSession();
        handler.getSessionMappingStorage().addSessionById(TICKET, session);

        request.setParameter(LOGOUT_PARAMETER_NAME, "");
        request.setQueryString(LOGOUT_PARAMETER_NAME + "=");
        request.setMethod("GET");

        assertTrue(handler.process(request, response));
        assertFalse(session.isInvalid());
    }

    @Test
    public void destroySessionWithNonExistentTokenDoesNotInvalidate() {
        final var logoutMessage = LogoutMessageGenerator.generateBackChannelLogoutMessage("ST-nonexistent");
        request.setParameter(LOGOUT_PARAMETER_NAME, logoutMessage);
        request.setMethod("POST");

        final var session = new MockHttpSession();
        handler.getSessionMappingStorage().addSessionById(TICKET, session);

        assertFalse(handler.process(request, response));
        assertFalse(session.isInvalid());
    }

    @Test
    public void postRequestWithoutLogoutParameterIsNotLogout() {
        request.setMethod("POST");
        request.setParameter(ANOTHER_PARAMETER, "someValue");
        assertTrue(handler.process(request, response));
    }

    @Test
    public void getRequestWithoutLogoutParameterIsNotLogout() {
        request.setMethod("GET");
        request.setParameter(ANOTHER_PARAMETER, "someValue");
        request.setQueryString(ANOTHER_PARAMETER + "=someValue");
        assertTrue(handler.process(request, response));
    }

    @Test
    public void putRequestIsNotLogout() {
        request.setMethod("PUT");
        request.setParameter(LOGOUT_PARAMETER_NAME, "someLogoutMessage");
        assertTrue(handler.process(request, response));
    }

    @Test
    public void deleteRequestIsNotLogout() {
        request.setMethod("DELETE");
        request.setParameter(LOGOUT_PARAMETER_NAME, "someLogoutMessage");
        assertTrue(handler.process(request, response));
    }

    @Test
    public void logoutWithPathInfoMatchesCallbackPath() {
        handler.setLogoutCallbackPath("/logout/callback");
        request.setServletPath("/logout");
        request.setPathInfo("/callback");

        final var logoutMessage = LogoutMessageGenerator.generateBackChannelLogoutMessage(TICKET);
        request.setParameter(LOGOUT_PARAMETER_NAME, logoutMessage);
        request.setMethod("POST");

        final var session = new MockHttpSession();
        handler.getSessionMappingStorage().addSessionById(TICKET, session);

        assertFalse(handler.process(request, response));
        assertTrue(session.isInvalid());
    }

    @Test
    public void multipartWithUppercaseContentTypeIsNotLogout() {
        final var logoutMessage = LogoutMessageGenerator.generateBackChannelLogoutMessage(TICKET);
        request.setParameter(LOGOUT_PARAMETER_NAME, logoutMessage);
        request.setMethod("POST");
        request.setContentType("MULTIPART/FORM-DATA");

        final var session = new MockHttpSession();
        handler.getSessionMappingStorage().addSessionById(TICKET, session);

        assertTrue(handler.process(request, response));
        assertFalse(session.isInvalid());
    }

    @Test
    public void multipartWithMixedCaseContentTypeIsNotLogout() {
        final var logoutMessage = LogoutMessageGenerator.generateBackChannelLogoutMessage(TICKET);
        request.setParameter(LOGOUT_PARAMETER_NAME, logoutMessage);
        request.setMethod("POST");
        request.setContentType("Multipart/Form-Data; boundary=something");

        final var session = new MockHttpSession();
        handler.getSessionMappingStorage().addSessionById(TICKET, session);

        assertTrue(handler.process(request, response));
        assertFalse(session.isInvalid());
    }

    @Test
    public void logoutWithoutJsonpCallbackProducesNoResponse() throws Exception {
        final var logoutMessage = LogoutMessageGenerator.generateFrontChannelLogoutMessage(TICKET);
        request.setParameter(LOGOUT_PARAMETER_NAME, logoutMessage);
        request.setQueryString(LOGOUT_PARAMETER_NAME + "=" + logoutMessage);
        request.setMethod("GET");

        final var session = new MockHttpSession();
        handler.getSessionMappingStorage().addSessionById(TICKET, session);

        assertFalse(handler.process(request, response));
        assertTrue(session.isInvalid());
        assertEquals("", response.getContentAsString());
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
