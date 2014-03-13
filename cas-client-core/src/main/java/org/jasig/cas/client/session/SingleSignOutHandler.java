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

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.util.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs CAS single sign-out operations in an API-agnostic fashion.
 *
 * @author Marvin S. Addison
 * @version $Revision$ $Date$
 * @since 3.1.12
 *
 */
public final class SingleSignOutHandler {

    public final static String DEFAULT_ARTIFACT_PARAMETER_NAME = "ticket";
    public final static String DEFAULT_LOGOUT_PARAMETER_NAME = "logoutRequest";
    public final static String DEFAULT_RELAY_STATE_PARAMETER_NAME = "RelayState";
    
    /** Logger instance */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** Mapping of token IDs and session IDs to HTTP sessions */
    private SessionMappingStorage sessionMappingStorage = new HashMapBackedSessionMappingStorage();

    /** The name of the artifact parameter.  This is used to capture the session identifier. */
    private String artifactParameterName = DEFAULT_ARTIFACT_PARAMETER_NAME;

    /** Parameter name that stores logout request */
    private String logoutParameterName = DEFAULT_LOGOUT_PARAMETER_NAME;

    /** Parameter name that stores the state of the CAS server webflow for the callback */
    private String relayStateParameterName = DEFAULT_RELAY_STATE_PARAMETER_NAME;
    
    private boolean artifactParameterOverPost = false;

    private boolean eagerlyCreateSessions = true;

    private List<String> safeParameters;

    public void setSessionMappingStorage(final SessionMappingStorage storage) {
        this.sessionMappingStorage = storage;
    }

    public void setArtifactParameterOverPost(final boolean artifactParameterOverPost) {
        this.artifactParameterOverPost = artifactParameterOverPost;
    }

    public SessionMappingStorage getSessionMappingStorage() {
        return this.sessionMappingStorage;
    }

    /**
     * @param name Name of the authentication token parameter.
     */
    public void setArtifactParameterName(final String name) {
        this.artifactParameterName = name;
    }

    /**
     * @param name Name of parameter containing CAS logout request message.
     */
    public void setLogoutParameterName(final String name) {
        this.logoutParameterName = name;
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
    public void init() {
        CommonUtils.assertNotNull(this.artifactParameterName, "artifactParameterName cannot be null.");
        CommonUtils.assertNotNull(this.logoutParameterName, "logoutParameterName cannot be null.");
        CommonUtils.assertNotNull(this.sessionMappingStorage, "sessionMappingStorage cannot be null.");
        CommonUtils.assertNotNull(this.relayStateParameterName, "relayStateParameterName cannot be null.");

        if (this.artifactParameterOverPost) {
            this.safeParameters = Arrays.asList(this.logoutParameterName, this.artifactParameterName);
        } else {
            this.safeParameters = Arrays.asList(this.logoutParameterName);
        }
    }

    /**
     * Determines whether the given request contains an authentication token.
     *
     * @param request HTTP reqest.
     *
     * @return True if request contains authentication token, false otherwise.
     */
    public boolean isTokenRequest(final HttpServletRequest request) {
        return CommonUtils.isNotBlank(CommonUtils.safeGetParameter(request, this.artifactParameterName,
                this.safeParameters));
    }

    /**
     * Determines whether the given request is a CAS back channel logout request.
     *
     * @param request HTTP request.
     *
     * @return True if request is logout request, false otherwise.
     */
    public boolean isBackChannelLogoutRequest(final HttpServletRequest request) {
        return "POST".equals(request.getMethod())
                && !isMultipartRequest(request)
                && CommonUtils.isNotBlank(CommonUtils.safeGetParameter(request, this.logoutParameterName,
                        this.safeParameters));
    }

    /**
     * Determines whether the given request is a CAS front channel logout request.
     *
     * @param request HTTP request.
     *
     * @return True if request is logout request, false otherwise.
     */
    public boolean isFrontChannelLogoutRequest(final HttpServletRequest request) {
        return "GET".equals(request.getMethod())
                && CommonUtils.isNotBlank(CommonUtils.safeGetParameter(request, this.logoutParameterName));
    }

    /**
     * Associates a token request with the current HTTP session by recording the mapping
     * in the the configured {@link SessionMappingStorage} container.
     * 
     * @param request HTTP request containing an authentication token.
     */
    public void recordSession(final HttpServletRequest request) {
        final HttpSession session = request.getSession(this.eagerlyCreateSessions);

        if (session == null) {
            logger.debug("No session currently exists (and none created).  Cannot record session information for single sign out.");
            return;
        }

        final String token = CommonUtils.safeGetParameter(request, this.artifactParameterName, this.safeParameters);
        logger.debug("Recording session for token {}", token);

        try {
            this.sessionMappingStorage.removeBySessionById(session.getId());
        } catch (final Exception e) {
            // ignore if the session is already marked as invalid.  Nothing we can do!
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
        // base64 decode
        final byte[] binaryMessage = Base64.decodeBase64(originalMessage);

        try {
            // decompress the bytes
            final Inflater decompresser = new Inflater();
            decompresser.setInput(binaryMessage);
            byte[] result = new byte[binaryMessage.length * 10];
            int resultLength = decompresser.inflate(result);
            decompresser.end();

            // decode the bytes into a String
            return new String(result, 0, resultLength, "UTF-8");
        } catch (DataFormatException e) {
            logger.error("Unable to decompress logout message", e);
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            logger.error("Unable to decompress logout message", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Destroys the current HTTP session for the given CAS logout request.
     *
     * @param request HTTP request containing a CAS logout message.
     */
    public void destroySession(final HttpServletRequest request) {
        String logoutMessage = CommonUtils.safeGetParameter(request, this.logoutParameterName,
                this.safeParameters);
        // front channel request -> the message needs to be base64 decoded + decompressed
        if ("GET".equals(request.getMethod())) {
            logoutMessage = uncompressLogoutMessage(logoutMessage); 
        }
        logger.trace("Logout request:\n{}", logoutMessage);

        final String token = XmlUtils.getTextForElement(logoutMessage, "SessionIndex");
        if (CommonUtils.isNotBlank(token)) {
            final HttpSession session = this.sessionMappingStorage.removeSessionByMappingId(token);

            if (session != null) {
                String sessionID = session.getId();

                logger.debug("Invalidating session [{}] for token [{}]", sessionID, token);

                try {
                    session.invalidate();
                } catch (final IllegalStateException e) {
                    logger.debug("Error invalidating session.", e);
                }
                try {
                    request.logout();
                } catch (final ServletException e) {
                    logger.debug("Error performing request.logout.");
                }
            }
        }
    }

    private boolean isMultipartRequest(final HttpServletRequest request) {
        return request.getContentType() != null && request.getContentType().toLowerCase().startsWith("multipart");
    }
}
