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
package org.apereo.cas.client.boot.configuration;

import org.apereo.cas.client.util.AbstractCasFilter;
import org.apereo.cas.client.util.AbstractConfigurationFilter;
import org.apereo.cas.client.validation.Assertion;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.io.Serial;
import java.util.ArrayList;

/**
 * Populates the Spring Security context with
 * the CAS authentication fetched from assertion
 * and processing roles, attributes, etc.
 *
 * @author Misagh Moayyed
 * @since 3.6.2
 */
public class SpringSecurityAssertionSessionContextFilter extends AbstractConfigurationFilter {
    private final AuthenticationUserDetailsService userDetailsService;

    private final String[] attributes;

    public SpringSecurityAssertionSessionContextFilter(
        final AuthenticationUserDetailsService userDetailsService,
        final String... attributes) {
        this.userDetailsService = userDetailsService;
        this.attributes = attributes;
    }

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
                         final FilterChain filterChain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;
        final HttpSession session = request.getSession();
        if (session != null && session.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION) != null) {
            final Assertion assertion = (Assertion) session.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION);

            final CasAuthenticationToken authentication = new CasAuthenticationToken(assertion);
            final UserDetails userDetails = userDetailsService.loadUserDetails(authentication);
            authentication.getAuthorities().addAll(userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }

    private static class CasAuthenticationToken extends AbstractAuthenticationToken {
        @Serial
        private static final long serialVersionUID = -270498735829222143L;

        private final Assertion assertion;

        public CasAuthenticationToken(final Assertion assertion) {
            super(new ArrayList<>());
            this.assertion = assertion;
        }

        @Override
        public Object getCredentials() {
            return null;
        }

        @Override
        public Object getPrincipal() {
            return assertion.getPrincipal().getName();
        }
    }
}
