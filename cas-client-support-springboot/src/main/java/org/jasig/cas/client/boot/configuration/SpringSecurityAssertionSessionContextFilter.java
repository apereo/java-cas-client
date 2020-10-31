/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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
package org.jasig.cas.client.boot.configuration;

import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.util.AbstractConfigurationFilter;
import org.jasig.cas.client.validation.Assertion;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Populates the Spring Security context with
 * the CAS authentication fetched from assertion
 * and processing roles, attributes, etc.
 *
 * @author Misagh Moayyed
 * @since 3.6.2
 */
public class SpringSecurityAssertionSessionContextFilter extends AbstractConfigurationFilter {
    private final AuthenticationUserDetailsService<CasAssertionAuthenticationToken> userDetailsService;

    private final String[] attributes;

    public SpringSecurityAssertionSessionContextFilter(
        final AuthenticationUserDetailsService<CasAssertionAuthenticationToken> userDetailsService,
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

            final CasAssertionAuthenticationToken token = new CasAssertionAuthenticationToken(assertion, "");
            final UserDetails userDetails = userDetailsService.loadUserDetails(token);
            final CasAuthenticationToken authentication = new CasAuthenticationToken("CasAuthenticationToken",
                userDetails,
                userDetails,
                userDetails.getAuthorities(),
                userDetails,
                assertion);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }
}
