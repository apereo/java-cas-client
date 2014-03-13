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

import org.apache.commons.lang.StringUtils;
import org.jasig.cas.client.util.AbstractConfigurationFilter;
import org.jasig.cas.client.util.CommonUtils;

/**
 * Implements the Single Sign Out protocol.  It handles registering the session and destroying the session.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class SingleSignOutFilter extends AbstractConfigurationFilter {

    private static final SingleSignOutHandler handler = new SingleSignOutHandler();

    /** The prefix url of the CAS server */
    private String casServerUrlPrefix;
    
    /** Parameter name that stores the state of the CAS server webflow for the callback */
    private String relayStateParameterName = SingleSignOutHandler.DEFAULT_RELAY_STATE_PARAMETER_NAME;

    public void init(final FilterConfig filterConfig) throws ServletException {
        if (!isIgnoreInitConfiguration()) {
            handler.setArtifactParameterName(getPropertyFromInitParams(filterConfig, "artifactParameterName",
                    SingleSignOutHandler.DEFAULT_ARTIFACT_PARAMETER_NAME));
            handler.setLogoutParameterName(getPropertyFromInitParams(filterConfig, "logoutParameterName",
                    SingleSignOutHandler.DEFAULT_LOGOUT_PARAMETER_NAME));
            setRelayStateParameterName(getPropertyFromInitParams(filterConfig, "relayStateParameterName",
                    SingleSignOutHandler.DEFAULT_RELAY_STATE_PARAMETER_NAME));
            handler.setArtifactParameterOverPost(parseBoolean(getPropertyFromInitParams(filterConfig,
                    "artifactParameterOverPost", "false")));
            handler.setEagerlyCreateSessions(parseBoolean(getPropertyFromInitParams(filterConfig,
                    "eagerlyCreateSessions", "true")));
            setCasServerUrlPrefix(getPropertyFromInitParams(filterConfig, "casServerUrlPrefix", null));
        }
        handler.init();
    }

    public void setArtifactParameterName(final String name) {
        handler.setArtifactParameterName(name);
    }

    public void setLogoutParameterName(final String name) {
        handler.setLogoutParameterName(name);
    }

    public void setRelayStateParameterName(final String name) {
        this.relayStateParameterName = name;
        handler.setRelayStateParameterName(name);
    }

    public void setSessionMappingStorage(final SessionMappingStorage storage) {
        handler.setSessionMappingStorage(storage);
    }

    public void setCasServerUrlPrefix(final String casServerUrlPrefix) {
        CommonUtils.assertNotNull(casServerUrlPrefix, "casServerUrlPrefix cannot be null.");
        this.casServerUrlPrefix = casServerUrlPrefix;
    }

    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
            final FilterChain filterChain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;

        if (handler.isTokenRequest(request)) {
            handler.recordSession(request);
        } else if (handler.isBackChannelLogoutRequest(request)) {
            handler.destroySession(request);
            // Do not continue up filter chain
            return;
        } else if (handler.isFrontChannelLogoutRequest(request)) {
            handler.destroySession(request);
            // relay state value
            final String relayStateValue = CommonUtils.safeGetParameter(request, this.relayStateParameterName);
            // if we have a state value -> redirect to the CAS server to continue the logout process
            if (StringUtils.isNotBlank(relayStateValue)) {
                final StringBuffer buffer = new StringBuffer();
                buffer.append(casServerUrlPrefix);
                if (!this.casServerUrlPrefix.endsWith("/")) {
                    buffer.append("/");
                }
                buffer.append("logout?_eventId=next&");
                buffer.append(this.relayStateParameterName);
                buffer.append("=");
                buffer.append(CommonUtils.urlEncode(relayStateValue));
                final String redirectUrl = buffer.toString(); 
                logger.debug("Redirecting back to the CAS server: {}", redirectUrl);
                CommonUtils.sendRedirect(response, redirectUrl);
            }
            return;
        } else {
            logger.trace("Ignoring URI {}", request.getRequestURI());
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    public void destroy() {
        // nothing to do
    }

    protected static SingleSignOutHandler getSingleSignOutHandler() {
        return handler;
    }
}
