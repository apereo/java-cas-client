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
package org.jasig.cas.client.jetty;

import org.eclipse.jetty.security.Authenticator;
import org.eclipse.jetty.security.ServerAuthException;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.jasig.cas.client.Protocol;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.util.ReflectUtils;
import org.jasig.cas.client.validation.AbstractCasProtocolUrlBasedTicketValidator;
import org.jasig.cas.client.validation.AbstractUrlBasedTicketValidator;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.TicketValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Jetty authenticator component for container-managed CAS authentication.
 * <p><em>NOTE:</em> This component does not support CAS gateway mode.</p>
 *
 * @author Marvin S. Addison
 * @since 3.4.2
 */
public class CasAuthenticator extends AbstractLifeCycle implements Authenticator {

    /** Name of authentication method provided by this authenticator. */
    public static final String AUTH_METHOD = "CAS";

    /** Session attribute used to cache CAS authentication data. */
    private static final String CACHED_AUTHN_ATTRIBUTE = "org.jasig.cas.client.jetty.Authentication";

    /** Logger instance. */
    private final Logger logger = LoggerFactory.getLogger(CasAuthenticator.class);

    /** Map of tickets to sessions. */
    private final ConcurrentMap<String, HttpSession> sessionMap = new ConcurrentHashMap<String, HttpSession>();

    /** CAS ticket validator component. */
    private TicketValidator ticketValidator;

    /** Space-delimited ist of server names. */
    private String serverNames;

    /** CAS principal attribute containing role data. */
    private String roleAttribute;

    /** URL to /login URI on CAS server. */
    private String casServerLoginUrl;

    /** Protocol used by ticket validator. */
    private Protocol protocol;

    /** CAS renew parameter. */
    private boolean renew;


    /**
     * Sets the CAS ticket validator component.
     *
     * @param ticketValidator Ticket validator, MUST NOT be null.
     */
    public void setTicketValidator(final TicketValidator ticketValidator) {
        assert ticketValidator != null : "TicketValidator cannot be null";
        if (ticketValidator instanceof AbstractUrlBasedTicketValidator) {
            if (ticketValidator instanceof AbstractCasProtocolUrlBasedTicketValidator) {
                protocol = Protocol.CAS2;
            } else {
                protocol = Protocol.SAML11;
            }
            casServerLoginUrl = ReflectUtils.getField("casServerUrlPrefix", ticketValidator) + "/login";
            renew = (Boolean) ReflectUtils.getField("renew", ticketValidator);
        } else {
            throw new IllegalArgumentException("Unsupported ticket validator " + ticketValidator);
        }
        this.ticketValidator = ticketValidator;
    }

    /**
     * Sets the names of the server host running Jetty.
     *
     * @param nameList Space-delimited list of one or more server names, e.g. "www1.example.com www2.example.com".
     *                 MUST NOT be blank.
     */
    public void setServerNames(final String nameList) {
        CommonUtils.isNotBlank(nameList);
        this.serverNames = nameList;
    }

    /** @return The name of the CAS principal attribute that contains role data. */
    public String getRoleAttribute() {
        return roleAttribute;
    }

    /**
     * Sets the name of the CAS principal attribute that contains role data.
     *
     * @param roleAttribute Role attribute name. MUST NOT be blank.
     */
    public void setRoleAttribute(final String roleAttribute) {
        CommonUtils.isNotBlank(roleAttribute);
        this.roleAttribute = roleAttribute;
    }

    @Override
    public void setConfiguration(final AuthConfiguration configuration) {
        // Nothing to do
        // All configuration must be via CAS-specific setter methods
    }

    @Override
    public String getAuthMethod() {
        return AUTH_METHOD;
    }

    @Override
    public void prepareRequest(final ServletRequest request) {
        // Nothing to do
    }

    @Override
    public Authentication validateRequest(
            final ServletRequest servletRequest, final ServletResponse servletResponse, final boolean mandatory)
            throws ServerAuthException {

        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;

        CasAuthentication authentication = fetchCachedAuthentication(request);
        if (!mandatory) {
            if (authentication != null) {
                return authentication;
            }
            return Authentication.UNAUTHENTICATED;
        }

        String ticket;
        for (final Protocol protocol : Protocol.values()) {
            ticket = request.getParameter(protocol.getArtifactParameterName());
            if (ticket != null) {
                try {
                    logger.debug("Attempting to validate {}", ticket);
                    final Assertion assertion = ticketValidator.validate(ticket, serviceUrl(request, response));
                    logger.debug("Successfully authenticated {}", assertion.getPrincipal());
                    authentication = new CasAuthentication(this, ticket, assertion);
                    cacheAuthentication(request, authentication);
                } catch (Exception e) {
                    throw new ServerAuthException("CAS ticket validation failed", e);
                }
            }
        }

        if (authentication != null) {
            return authentication;
        }
        redirectToCas(request, response);
        return Authentication.SEND_CONTINUE;
    }

    @Override
    public boolean secureResponse(
            final ServletRequest request,
            final ServletResponse response,
            final boolean mandatory,
            final Authentication.User user) throws ServerAuthException {
        return true;
    }

    @Override
    protected void doStart() throws Exception {
        if (ticketValidator == null) {
            throw new RuntimeException("TicketValidator cannot be null");
        }
        if (serverNames == null) {
            throw new RuntimeException("ServerNames cannot be null");
        }
    }

    protected void clearCachedAuthentication(final String ticket) {
        sessionMap.remove(ticket);
    }

    private void cacheAuthentication(final HttpServletRequest request, final CasAuthentication authentication) {
        final HttpSession session = request.getSession(false);
        if (session != null) {
            session.setAttribute(CACHED_AUTHN_ATTRIBUTE, authentication);
            sessionMap.put(authentication.getTicket(), session);
        }
    }

    private CasAuthentication fetchCachedAuthentication(final HttpServletRequest request) {
        final HttpSession session = request.getSession(false);
        if (session != null) {
            return (CasAuthentication) session.getAttribute(CACHED_AUTHN_ATTRIBUTE);
        }
        return null;
    }

    private String serviceUrl(final HttpServletRequest request, final HttpServletResponse response) {
        return CommonUtils.constructServiceUrl(
                request,
                response,
                null,
                serverNames,
                protocol.getServiceParameterName(),
                protocol.getArtifactParameterName(),
                true);
    }

    private void redirectToCas(
            final HttpServletRequest request, final HttpServletResponse response) throws ServerAuthException {
        try {
            final String redirectUrl = CommonUtils.constructRedirectUrl(
                    casServerLoginUrl, protocol.getServiceParameterName(), serviceUrl(request, response), renew, false);
            logger.debug("Redirecting to {}", redirectUrl);
            response.sendRedirect(redirectUrl);
        } catch (IOException e) {
            logger.debug("Redirect to CAS failed with error: {}", e);
            throw new ServerAuthException("Redirect to CAS failed", e);
        }
    }

}
