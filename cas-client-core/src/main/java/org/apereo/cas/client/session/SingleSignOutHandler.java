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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import org.apereo.cas.client.Protocol;
import org.apereo.cas.client.configuration.ConfigurationKeys;
import org.apereo.cas.client.util.CommonUtils;
import org.apereo.cas.client.util.WebUtils;
import org.apereo.cas.client.util.XmlUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.zip.Inflater;

/**
 * Performs CAS single sign-out operations in an API-agnostic fashion.
 *
 * @author Marvin S. Addison
 * @version $Revision$ $Date$
 * @since 3.1.12
 *
 */
public final class SingleSignOutHandler {

    private static final int DECOMPRESSION_FACTOR = 10;

    /** Logger instance */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final LogoutStrategy logoutStrategy = isServlet30() ? new Servlet30LogoutStrategy() : new Servlet25LogoutStrategy();

    private final ObjectMapper mapper = new ObjectMapper();

    /** Mapping of token IDs and session IDs to HTTP sessions */
    private SessionMappingStorage sessionMappingStorage = new HashMapBackedSessionMappingStorage();

    /** The name of the artifact parameter.  This is used to capture the session identifier. */
    private String artifactParameterName = Protocol.CAS2.getArtifactParameterName();

    /**
     * The name of the JSONP callback parameter for front-channel logout requests
     */
    private String jsonpCallbackParameterName = ConfigurationKeys.JSONP_CALLBACK_PARAMETER_NAME.getDefaultValue();

    /** Parameter name that stores logout request for SLO */
    private String logoutParameterName = ConfigurationKeys.LOGOUT_PARAMETER_NAME.getDefaultValue();

    /** Parameter name that stores the state of the CAS server webflow for the callback */
    private String relayStateParameterName = ConfigurationKeys.RELAY_STATE_PARAMETER_NAME.getDefaultValue();

    /** The logout callback path configured at the CAS server, if there is one */
    private String logoutCallbackPath;

    private boolean artifactParameterOverPost = false;

    private boolean eagerlyCreateSessions = true;

    private List<String> safeParameters;

    private static boolean isServlet30() {
        try {
            return HttpServletRequest.class.getMethod("logout") != null;
        } catch (final NoSuchMethodException e) {
            return false;
        }
    }

    public void setArtifactParameterOverPost(final boolean artifactParameterOverPost) {
        this.artifactParameterOverPost = artifactParameterOverPost;
    }

    public SessionMappingStorage getSessionMappingStorage() {
        return this.sessionMappingStorage;
    }

    public void setSessionMappingStorage(final SessionMappingStorage storage) {
        this.sessionMappingStorage = storage;
    }

    /**
     * @param name Name of the authentication token parameter.
     */
    public void setArtifactParameterName(final String name) {
        this.artifactParameterName = name;
    }

    /**
     * @param name Name of the JSONP callback parameter for front-channel logout requests.
     */
    public void setJsonpCallbackParameterName(final String name) {
        this.jsonpCallbackParameterName = name;
    }

    /**
     * @param name Name of parameter containing CAS logout request message for SLO.
     */
    public void setLogoutParameterName(final String name) {
        this.logoutParameterName = name;
    }

    /**
     * @param logoutCallbackPath The logout callback path configured at the CAS server.
     */
    public void setLogoutCallbackPath(final String logoutCallbackPath) {
        this.logoutCallbackPath = logoutCallbackPath;
    }

    /**
     * @param name Name of parameter containing the state of the CAS server webflow.
     */
    public void setRelayStateParameterName(final String name) {
        this.relayStateParameterName = name;
    }

    public void setEagerlyCreateSessions(final boolean eagerlyCreateSessions) {
        this.eagerlyCreateSessions = eagerlyCreateSessions;
    }

    /**
     * Initializes the component for use.
     */
    public synchronized void init() {
        if (this.safeParameters == null) {
            CommonUtils.assertNotNull(this.artifactParameterName, "artifactParameterName cannot be null.");
            CommonUtils.assertNotNull(this.logoutParameterName, "logoutParameterName cannot be null.");
            CommonUtils.assertNotNull(this.sessionMappingStorage, "sessionMappingStorage cannot be null.");
            CommonUtils.assertNotNull(this.relayStateParameterName, "relayStateParameterName cannot be null.");

            if (this.artifactParameterOverPost) {
                this.safeParameters = Arrays.asList(this.logoutParameterName, this.artifactParameterName);
            } else {
                this.safeParameters = Collections.singletonList(this.logoutParameterName);
            }
        }
    }

    /**
     * Process a request regarding the SLO process: record the session or destroy it.
     *
     * @param request the incoming HTTP request.
     * @param response the HTTP response.
     * @return if the request should continue to be processed.
     */
    public boolean process(final HttpServletRequest request, final HttpServletResponse response) {
        if (isTokenRequest(request)) {
            logger.trace("Received a token request");
            recordSession(request);
            return true;
        }

        if (isLogoutRequest(request)) {
            logger.trace("Received a logout request");
            destroySession(request);
            final var callback = WebUtils.safeGetParameter(request, this.jsonpCallbackParameterName, this.safeParameters);
            if (callback != null) {
                try {
                    response.setContentType("application/javascript");
                    mapper.writeValue(response.getWriter(), new JSONPObject(callback, true));
                } catch (final Exception e) {
                    logger.debug("Error writing JSONP logout response.", e);
                }
            }
            return false;
        }
        logger.trace("Ignoring URI for logout: {}", request.getRequestURI());
        return true;
    }

    /**
     * Abstracts the ways we can force logout with the Servlet spec.
     */
    @FunctionalInterface
    private interface LogoutStrategy {

        void logout(HttpServletRequest request);
    }

    private static class Servlet25LogoutStrategy implements LogoutStrategy {

        @Override
        public void logout(final HttpServletRequest request) {
            // nothing additional to do here
        }
    }

    private class Servlet30LogoutStrategy implements LogoutStrategy {

        @Override
        public void logout(final HttpServletRequest request) {
            try {
                request.logout();
            } catch (final ServletException e) {
                logger.debug("Error performing request.logout.");
            }
        }
    }

    /**
     * Determines whether the given request contains an authentication token.
     *
     * @param request HTTP reqest.
     *
     * @return True if request contains authentication token, false otherwise.
     */
    private boolean isTokenRequest(final HttpServletRequest request) {
        return CommonUtils.isNotBlank(WebUtils.safeGetParameter(request, this.artifactParameterName,
            this.safeParameters));
    }

    /**
     * Determines whether the given request is a CAS  logout request.
     *
     * @param request HTTP request.
     *
     * @return True if request is logout request, false otherwise.
     */
    private boolean isLogoutRequest(final HttpServletRequest request) {
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            return !isMultipartRequest(request)
                   && pathEligibleForLogout(request)
                   && CommonUtils.isNotBlank(WebUtils.safeGetParameter(request, this.logoutParameterName,
                this.safeParameters));
        }

        if ("GET".equalsIgnoreCase(request.getMethod())) {
            return CommonUtils.isNotBlank(WebUtils.safeGetParameter(request, this.logoutParameterName, this.safeParameters));
        }
        return false;
    }

    private boolean pathEligibleForLogout(final HttpServletRequest request) {
        return logoutCallbackPath == null || logoutCallbackPath.equals(getPath(request));
    }

    private static String getPath(final HttpServletRequest request) {
        return request.getServletPath() + CommonUtils.nullToEmpty(request.getPathInfo());
    }

    /**
     * Associates a token request with the current HTTP session by recording the mapping
     * in the the configured {@link SessionMappingStorage} container.
     *
     * @param request HTTP request containing an authentication token.
     */
    private void recordSession(final HttpServletRequest request) {
        final var session = request.getSession(this.eagerlyCreateSessions);

        if (session == null) {
            logger.debug("No session currently exists (and none created).  Cannot record session information for single sign out.");
            return;
        }

        final var token = WebUtils.safeGetParameter(request, this.artifactParameterName, this.safeParameters);
        logger.debug("Recording session for token {}", token);

        try {
            this.sessionMappingStorage.removeBySessionById(session.getId());
        } catch (final Exception e) {
            // ignore if the session is already marked as invalid. Nothing we can do!
        }
        sessionMappingStorage.addSessionById(token, session);
    }

    /**
     * Uncompress a logout message (base64 + deflate).
     *
     * @param originalMessage the original logout message.
     * @return the uncompressed logout message.
     */
    private String uncompressLogoutMessage(final String originalMessage) {
        final var binaryMessage = Base64.getDecoder().decode(originalMessage);

        Inflater decompresser = null;
        try {
            // decompress the bytes
            decompresser = new Inflater();
            decompresser.setInput(binaryMessage);
            final var result = new byte[binaryMessage.length * DECOMPRESSION_FACTOR];

            final var resultLength = decompresser.inflate(result);

            // decode the bytes into a String
            return new String(result, 0, resultLength, "UTF-8");
        } catch (final Exception e) {
            logger.error("Unable to decompress logout message", e);
            throw new RuntimeException(e);
        } finally {
            if (decompresser != null) {
                decompresser.end();
            }
        }
    }

    /**
     * Destroys the current HTTP session for the given CAS logout request.
     *
     * @param request HTTP request containing a CAS logout message.
     */
    private void destroySession(final HttpServletRequest request) {
        var logoutMessage = WebUtils.safeGetParameter(request, this.logoutParameterName, this.safeParameters);
        if (CommonUtils.isBlank(logoutMessage)) {
            logger.error("Could not locate logout message of the request from {}", this.logoutParameterName);
            return;
        }

        if (!logoutMessage.contains("SessionIndex")) {
            logoutMessage = uncompressLogoutMessage(logoutMessage);
        }

        logger.trace("Logout request:\n{}", logoutMessage);
        final var token = XmlUtils.getTextForElement(logoutMessage, "SessionIndex");
        if (CommonUtils.isNotBlank(token)) {
            final var session = this.sessionMappingStorage.removeSessionByMappingId(token);

            if (session != null) {
                final var sessionID = session.getId();
                logger.debug("Invalidating session [{}] for token [{}]", sessionID, token);

                try {
                    session.invalidate();
                } catch (final IllegalStateException e) {
                    logger.debug("Error invalidating session.", e);
                }
                this.logoutStrategy.logout(request);
            }
        }
    }

    private static boolean isMultipartRequest(final ServletRequest request) {
        return request.getContentType() != null && request.getContentType().toLowerCase().startsWith("multipart");
    }
}
