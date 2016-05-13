/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.cas.client.tomcat;

import java.io.IOException;
import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.TicketValidationException;
import org.jasig.cas.client.validation.TicketValidator;

/**
 * Version-agnostic authenticator which encapsulates the core CAS workflow of
 * redirecting to CAS for unauthenticated sessions and validating service tickets
 * when found in the request.  Implementations of the Tomcat <code>Authenticator</code>
 * class are expected to be thin wrappers that delegate most if not all authentication
 * logic to this class.
 *
 * @author Marvin S. Addison
 * @version $Revision$
 * @since 3.1.12
 *
 */
public final class AuthenticatorDelegate {

    /** Log instance */
    private final Log log = LogFactory.getLog(getClass());

    private String serviceUrl;
    
    private String serverName;

    private Map<String, String> serverNameRules = new LinkedHashMap<String, String>();

    private String casServerLoginUrl;
    
    private Map<String, String> casServerLoginUrlRules = new LinkedHashMap<String, String>();

    private String artifactParameterName;
    
    private String serviceParameterName;
    
    private TicketValidator ticketValidator;
    
    private CasRealm realm;

    /**
     * Performs CAS authentication on the given request and returns the principal
     * determined by the configured {@link CasRealm} on success.
     *
     * @param request HTTP request.
     * @param response HTTP response.
     *
     * @return The authenticated principal on authentication success, otherwise
     * null.  In the case where authentication explicitly fails, either due to
     * ticket validation failure or realm authentication failure, a 403 status
     * code is set on the response.  In cases where no existing CAS session exists,
     * a 302 redirect is set on the response to redirect to the CAS server for
     * authentication.
     */
    public final Principal authenticate(final HttpServletRequest request, final HttpServletResponse response) {
        Assertion assertion = null;
        HttpSession session = request.getSession();
        if (session != null) {
            assertion = (Assertion) session.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION);
        }
        if (assertion == null) {
            log.debug("CAS assertion not found in session -- authentication required.");
            final String token = request.getParameter(this.artifactParameterName);
            final String requestURL = request.getRequestURL().toString();
            final String serverName = resolveValue(requestURL, this.serverName, this.serverNameRules);
            final String service = CommonUtils.constructServiceUrl(request, response, this.serviceUrl, serverName, this.artifactParameterName, true);
            if (CommonUtils.isBlank(token)) {
                final String casServerLoginUrl = resolveValue(requestURL, this.casServerLoginUrl, this.casServerLoginUrlRules);
                final String redirectUrl = CommonUtils.constructRedirectUrl(casServerLoginUrl, this.serviceParameterName, service, false, false);
                log.debug("Requested " + requestURL);
                log.debug("Redirecting to " + redirectUrl);
                CommonUtils.sendRedirect(response, redirectUrl);
                return null;
            }
            try {
                log.debug("Attempting to validate " + token + " for " + service);
                assertion = this.ticketValidator.validate(token, service);
                log.debug("CAS authentication succeeded.");
                if (session == null) {
                    session = request.getSession(true);
                }
                session.setAttribute(AbstractCasFilter.CONST_CAS_ASSERTION, assertion);
            } catch (final TicketValidationException e) {
                setUnauthorized(response, e.getMessage());
                return null;
            }
        }
        Principal p = realm.authenticate(assertion.getPrincipal());
        if (p == null) {
            log.debug(assertion.getPrincipal().getName() + " failed to authenticate to " + realm);
            setUnauthorized(response, null);
        }
        return p;
    }

    private static String resolveValue(String requestURL, String defaultValue, Map<String, String> rules) {
        String parameter = defaultValue;
        for (Entry<String, String> entry : rules.entrySet()) {
            Matcher matcher = Pattern.compile(entry.getKey()).matcher(requestURL);
            if (matcher.matches()) {
                parameter = matcher.replaceFirst(entry.getValue());
                break;
            }
        }
        return parameter;
    }

    /**
     * @return the serviceUrl
     */
    public String getServiceUrl() {
        return serviceUrl;
    }

    /**
     * @param serviceUrl the serviceUrl to set
     */
    public void setServiceUrl(final String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    /**
     * @return the serverName
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * @param serverName the serverName to set
     */
    public void setServerName(final String serverName) {
        this.serverName = serverName.trim();
        if (this.serverName.contains(" ")) {
            String[] rules = this.serverName.split("\\|\\|");
            for (String rule : rules) {
                String[] mapping = rule.trim().split("\\s+");
                if (mapping.length != 2) {
                    throw new RuntimeException("\"" + rule + "\": serverName mapping format not adequate (<regexp> <serverName>)");
                }
                this.serverNameRules.put(mapping[0].trim(), mapping[1].trim());
            }
        }
    }

    /**
     * @return the casServerLoginUrl
     */
    public String getCasServerLoginUrl() {
        return casServerLoginUrl;
    }

    /**
     * @param casServerLoginUrl the casServerLoginUrl to set
     */
    public void setCasServerLoginUrl(final String casServerLoginUrl) {
        this.casServerLoginUrl = casServerLoginUrl.trim();
        if (this.casServerLoginUrl.contains(" ")) {
            String[] rules = this.casServerLoginUrl.split("\\|\\|");
            for (String rule : rules) {
                String[] mapping = rule.trim().split("\\s+");
                if (mapping.length != 2) {
                    throw new RuntimeException("\"" + rule + "\": casServerLoginUrl mapping format not adequate (<regexp> <casServerLoginUrl>)");
                }
                this.casServerLoginUrlRules.put(mapping[0].trim(), mapping[1].trim());
            }
        }
    }

    /**
     * @param artifactParameterName the artifactParameterName to set
     */
    public void setArtifactParameterName(final String artifactParameterName) {
        this.artifactParameterName = artifactParameterName;
    }

    /**
     * @param serviceParameterName the serviceParameterName to set
     */
    public void setServiceParameterName(final String serviceParameterName) {
        this.serviceParameterName = serviceParameterName;
    }

    /**
     * @param ticketValidator the ticketValidator to set
     */
    public void setTicketValidator(final TicketValidator ticketValidator) {
        this.ticketValidator = ticketValidator;
    }

    /**
     * @param realm the realm to set
     */
    public void setRealm(final CasRealm realm) {
        this.realm = realm;
    }

    private void setUnauthorized(final HttpServletResponse response, final String message) {
        try {
            if (message != null) {
	            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
            } else {
	            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error setting 403 status.", e);
        }
    }
}
