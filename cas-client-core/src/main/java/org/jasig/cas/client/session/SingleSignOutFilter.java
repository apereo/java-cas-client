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

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.client.configuration.ConfigurationKey;
import org.jasig.cas.client.util.AbstractConfigurationFilter;

/**
 * Implements the Single Sign Out protocol.  It handles registering the session and destroying the session.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class SingleSignOutFilter extends AbstractConfigurationFilter {

    private static final SingleSignOutHandler handler = new SingleSignOutHandler();

    public void init(final FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
        if (!isIgnoreInitConfiguration()) {
            setArtifactParameterName(getString(ConfigurationKey.ARTIFACT_PARAMETER_NAME, SingleSignOutHandler.DEFAULT_ARTIFACT_PARAMETER_NAME));
            setLogoutParameterName(getString(ConfigurationKey.LOGOUT_PARAMETER_NAME, SingleSignOutHandler.DEFAULT_LOGOUT_PARAMETER_NAME));
            setFrontLogoutParameterName(getString(ConfigurationKey.FRONT_LOGOUT_PARAMETER_NAME, SingleSignOutHandler.DEFAULT_FRONT_LOGOUT_PARAMETER_NAME));
            setRelayStateParameterName(getString(ConfigurationKey.RELAY_STATE_PARAMETER_NAME, SingleSignOutHandler.DEFAULT_RELAY_STATE_PARAMETER_NAME));
            setCasServerUrlPrefix(getString(ConfigurationKey.CAS_SERVER_LOGIN_URL, null));
            handler.setArtifactParameterOverPost(getBoolean(ConfigurationKey.ARTIFACT_PARAMETER_OVER_POST, false));
            handler.setEagerlyCreateSessions(getBoolean(ConfigurationKey.EAGERLY_CREATE_SESSIONS, true));
        }
        handler.init();
    }

    public void setArtifactParameterName(final String name) {
        handler.setArtifactParameterName(name);
    }

    public void setLogoutParameterName(final String name) {
        handler.setLogoutParameterName(name);
    }

    public void setFrontLogoutParameterName(final String name) {
        handler.setFrontLogoutParameterName(name);
    }

    public void setRelayStateParameterName(final String name) {
        handler.setRelayStateParameterName(name);
    }

    public void setCasServerUrlPrefix(final String casServerUrlPrefix) {
        handler.setCasServerUrlPrefix(casServerUrlPrefix);
    }

    public void setSessionMappingStorage(final SessionMappingStorage storage) {
        handler.setSessionMappingStorage(storage);
    }

    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
            final FilterChain filterChain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;

        if (handler.process(request, response)) {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    public void destroy() {
        // nothing to do
    }

    protected static SingleSignOutHandler getSingleSignOutHandler() {
        return handler;
    }
}
