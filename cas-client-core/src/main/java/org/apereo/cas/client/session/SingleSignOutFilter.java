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

import org.apereo.cas.client.configuration.ConfigurationKeys;
import org.apereo.cas.client.util.AbstractConfigurationFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implements the Single Sign Out protocol.  It handles registering the session and destroying the session.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class SingleSignOutFilter extends AbstractConfigurationFilter {

    private static final SingleSignOutHandler HANDLER = new SingleSignOutHandler();

    private final AtomicBoolean handlerInitialized = new AtomicBoolean(false);

    static SingleSignOutHandler getSingleSignOutHandler() {
        return HANDLER;
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
        if (!isIgnoreInitConfiguration()) {
            setArtifactParameterName(getString(ConfigurationKeys.ARTIFACT_PARAMETER_NAME));
            setLogoutParameterName(getString(ConfigurationKeys.LOGOUT_PARAMETER_NAME));
            setRelayStateParameterName(getString(ConfigurationKeys.RELAY_STATE_PARAMETER_NAME));
            setLogoutCallbackPath(getString(ConfigurationKeys.LOGOUT_CALLBACK_PATH));
            HANDLER.setArtifactParameterOverPost(getBoolean(ConfigurationKeys.ARTIFACT_PARAMETER_OVER_POST));
            HANDLER.setEagerlyCreateSessions(getBoolean(ConfigurationKeys.EAGERLY_CREATE_SESSIONS));
        }
        HANDLER.init();
        handlerInitialized.set(true);
    }

    public static void setArtifactParameterName(final String name) {
        HANDLER.setArtifactParameterName(name);
    }

    public static void setLogoutParameterName(final String name) {
        HANDLER.setLogoutParameterName(name);
    }

    public static void setRelayStateParameterName(final String name) {
        HANDLER.setRelayStateParameterName(name);
    }

    public static void setLogoutCallbackPath(final String logoutCallbackPath) {
        HANDLER.setLogoutCallbackPath(logoutCallbackPath);
    }

    public static void setSessionMappingStorage(final SessionMappingStorage storage) {
        HANDLER.setSessionMappingStorage(storage);
    }

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
                         final FilterChain filterChain) throws IOException, ServletException {
        final var request = (HttpServletRequest) servletRequest;
        final var response = (HttpServletResponse) servletResponse;

        /**
         * <p>Workaround for now for the fact that Spring Security will fail since it doesn't call {@link #init(javax.servlet.FilterConfig)}.</p>
         * <p>Ultimately we need to allow deployers to actually inject their fully-initialized {@link SingleSignOutHandler}.</p>
         */
        if (!this.handlerInitialized.getAndSet(true)) {
            HANDLER.init();
        }

        if (HANDLER.process(request, response)) {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    @Override
    public void destroy() {
        // nothing to do
    }
}
