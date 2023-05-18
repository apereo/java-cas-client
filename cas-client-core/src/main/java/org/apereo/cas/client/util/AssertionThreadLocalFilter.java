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
package org.apereo.cas.client.util;

import org.apereo.cas.client.validation.Assertion;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

/**
 * Places the assertion in a ThreadLocal such that other resources can access it that do not have access to the web tier session.
 *
 * @author Scott Battaglia
 * @since 3.0
 */
public final class AssertionThreadLocalFilter implements Filter {

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        // nothing to do here
    }

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
                         final FilterChain filterChain) throws IOException, ServletException {
        final var request = (HttpServletRequest) servletRequest;
        final var session = request.getSession(false);
        final var assertion = (Assertion) (session == null ? request
            .getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION) : session
            .getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION));

        try {
            AssertionHolder.setAssertion(assertion);
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            AssertionHolder.clear();
        }
    }

    @Override
    public void destroy() {
        // nothing to do
    }
}
