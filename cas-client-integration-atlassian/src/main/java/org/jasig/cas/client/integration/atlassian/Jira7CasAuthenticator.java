/*
 * Licensed to Jasig under one or more contributor license agreements. See the
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. Jasig licenses this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.jasig.cas.client.integration.atlassian;

import java.io.IOException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.security.login.JiraSeraphAuthenticator;
import com.atlassian.jira.web.ServletContextProvider;
import com.atlassian.seraph.auth.AuthenticatorException;
import com.atlassian.seraph.auth.LoginReason;
import com.atlassian.seraph.config.SecurityConfig;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;

/**
 * Extension of JiraSeraphAuthenticator to allow people to configure JIRA 7 and
 * above to authenticate via Jasig CAS
 *
 * @author Scott Battaglia
 * @author Martin Stiborsky
 * @author Jozef Kotlar
 * @author Jason Hitt
 * @version $Revision$ $Date$
 * @since 3.4.2
 */
public final class Jira7CasAuthenticator extends JiraSeraphAuthenticator {
    private static final long serialVersionUID = -8262305967991752189L;

    private static final Logger LOGGER = LoggerFactory.getLogger(Jira7CasAuthenticator.class);

    /**
     * We're just going to be wrapping the functionality of the servlet filter
     */
    private Cas20ProxyReceivingTicketValidationFilter validationFilter;

    @Override
    public void init(final Map<String, String> params, final SecurityConfig config) {
        super.init(params, config);

        try {
            validationFilter = new Cas20ProxyReceivingTicketValidationFilter();
            validationFilter.init(new WrappedFilterConfig(params));
            validationFilter.setRedirectAfterValidation(false);
        } catch (ServletException e) {
            LOGGER.error("Failed to initialize internal validation filter!", e);
            validationFilter = null;
        }
    }

    public Principal getUser(final HttpServletRequest request, final HttpServletResponse response) {
        // First, check to see if this session has already been authenticated
        // during a previous request.
        final HttpSession session = request.getSession(false);

        if (session != null) {
            Principal existingUser = getUserFromSessionOrAssertion(request, response);
            if (existingUser != null)
                return existingUser;
        }
        if (response != null) {
            try {
                validationFilter.doFilter(request, response, new FilterChain() {
                    @Override
                    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
                    }
                });
                return getUserFromSessionOrAssertion(request, response);
            } catch (Exception e) {
                LOGGER.debug("Call to internal validation filter failed", e);
            }
        }
        return null;
    }

    public boolean logout(final HttpServletRequest request, final HttpServletResponse response) throws AuthenticatorException {
        final HttpSession session = request.getSession();
        final Principal p = (Principal) session.getAttribute(LOGGED_IN_KEY);

        if (p != null) {
            LOGGER.debug("Logging out [{}] from CAS.", p.getName());
        }

        session.setAttribute(AbstractCasFilter.CONST_CAS_ASSERTION, null);
        return super.logout(request, response);
    }

    private Principal getUserFromSessionOrAssertion(final HttpServletRequest request, final HttpServletResponse response) {
        // First, check to see if this session has already been authenticated
        // during a previous request.
        final HttpSession session = request.getSession(false);
        if (session != null) {
            Principal existingUser = getUserFromSession(request);
            if (existingUser != null) {
                LOGGER.debug("Session found; user already logged in.");
                return existingUser;
            }

            final Assertion assertion = (Assertion) session.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION);
            if (assertion != null) {
                final String username = assertion.getPrincipal().getName();
                final Principal user = getUser(username);

                if (user != null) {
                    putPrincipalInSessionContext(request, user);
                    getElevatedSecurityGuard().onSuccessfulLoginAttempt(request, username);
                    LoginReason.OK.stampRequestResponse(request, response);
                    LOGGER.debug("Logging in [{}] from CAS.", username);
                } else {
                    LOGGER.debug("Failed logging [{}] from CAS.", username);
                    getElevatedSecurityGuard().onFailedLoginAttempt(request, username);
                }
                return user;
            }
        }
        return null;
    }

    private class WrappedFilterConfig implements FilterConfig {
        private final Map<String, String> params;

        public WrappedFilterConfig(Map<String, String> params) {
            this.params = ImmutableMap.copyOf(params);
        }

        @Override
        public String getFilterName() {
            return null;
        }

        @Override
        public ServletContext getServletContext() {
            return ServletContextProvider.getServletContext();
        }

        @Override
        public String getInitParameter(String name) {
            return params.get(name);
        }

        @Override
        public Enumeration<String> getInitParameterNames() {
            return Iterators.asEnumeration(params.keySet().iterator());
        }
    }
}
