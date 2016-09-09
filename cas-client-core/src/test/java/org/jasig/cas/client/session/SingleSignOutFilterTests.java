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
import static org.junit.Assert.assertNull;

import java.io.IOException;

import javax.servlet.ServletException;

import org.jasig.cas.client.Protocol;
import org.jasig.cas.client.configuration.ConfigurationKeys;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

/**
 * Tests {@link SingleSignOutFilter}.
 *
 * @author Jerome Leleu
 * @since 3.3.1
 */
public class SingleSignOutFilterTests {

    private final static String CAS_SERVER_URL_PREFIX = "http://myhost.com/mycasserver";
    private final static String TICKET = "ST-yyyyy";
    private final static String RELAY_STATE = "e1s1";

    private SingleSignOutFilter filter = new SingleSignOutFilter();
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @Before
    public void setUp() throws Exception {
        filter = new SingleSignOutFilter();
        filter.setCasServerUrlPrefix(CAS_SERVER_URL_PREFIX);
        filter.setIgnoreInitConfiguration(true);
        filter.init(new MockFilterConfig());
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
    }

    @Test
    public void tokenRequest() throws IOException, ServletException {
        request.setParameter(Protocol.CAS2.getArtifactParameterName(), TICKET);
        request.setQueryString(Protocol.CAS2.getArtifactParameterName() + "=" + TICKET);
        final MockHttpSession session = new MockHttpSession();
        request.setSession(session);
        filter.doFilter(request, response, filterChain);
        assertEquals(session, SingleSignOutFilter.getSingleSignOutHandler().getSessionMappingStorage().removeSessionByMappingId(TICKET));
    }

    @Test
    public void backChannelRequest() throws IOException, ServletException {
        request.setParameter(ConfigurationKeys.LOGOUT_PARAMETER_NAME.getDefaultValue(),
                LogoutMessageGenerator.generateBackChannelLogoutMessage(TICKET));
        request.setMethod("POST");
        final MockHttpSession session = new MockHttpSession();
        SingleSignOutFilter.getSingleSignOutHandler().getSessionMappingStorage().addSessionById(TICKET, session);
        filter.doFilter(request, response, filterChain);
        assertNull(SingleSignOutFilter.getSingleSignOutHandler().getSessionMappingStorage().removeSessionByMappingId(TICKET));
    }

    @Test
    public void frontChannelRequest() throws IOException, ServletException {
        final String logoutMessage = LogoutMessageGenerator.generateFrontChannelLogoutMessage(TICKET);
        request.setParameter(ConfigurationKeys.LOGOUT_PARAMETER_NAME.getDefaultValue(), logoutMessage);
        request.setQueryString(ConfigurationKeys.LOGOUT_PARAMETER_NAME.getDefaultValue() + "=" + logoutMessage);
        request.setMethod("GET");
        final MockHttpSession session = new MockHttpSession();
        SingleSignOutFilter.getSingleSignOutHandler().getSessionMappingStorage().addSessionById(TICKET, session);
        filter.doFilter(request, response, filterChain);
        assertNull(SingleSignOutFilter.getSingleSignOutHandler().getSessionMappingStorage().removeSessionByMappingId(TICKET));
        assertNull(response.getRedirectedUrl());
    }

    @Test
    public void frontChannelRequestRelayState() throws IOException, ServletException {
        final String logoutMessage = LogoutMessageGenerator.generateFrontChannelLogoutMessage(TICKET);
        request.setParameter(ConfigurationKeys.LOGOUT_PARAMETER_NAME.getDefaultValue(), logoutMessage);
        request.setParameter(ConfigurationKeys.RELAY_STATE_PARAMETER_NAME.getDefaultValue(), RELAY_STATE);
        request.setQueryString(ConfigurationKeys.LOGOUT_PARAMETER_NAME.getDefaultValue() + "=" + logoutMessage + "&" +
                ConfigurationKeys.RELAY_STATE_PARAMETER_NAME.getDefaultValue() + "=" + RELAY_STATE);
        request.setMethod("GET");
        final MockHttpSession session = new MockHttpSession();
        SingleSignOutFilter.getSingleSignOutHandler().getSessionMappingStorage().addSessionById(TICKET, session);
        filter.doFilter(request, response, filterChain);
        assertNull(SingleSignOutFilter.getSingleSignOutHandler().getSessionMappingStorage().removeSessionByMappingId(TICKET));
    }
}
