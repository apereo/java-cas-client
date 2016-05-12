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
package org.jasig.cas.client.http.servlet;

import org.jasig.cas.client.http.ClientSession;
import org.jasig.cas.client.http.HttpRequest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * An {@link HttpRequest} that delegates to a {@link HttpServletRequest}.
 *
 * @author Carl Harris
 */
public class DelegatingHttpRequest implements HttpRequest {

    private final LogoutStrategy logoutStrategy = isServlet30() ?
            Servlet30LogoutStrategy.INSTANCE : Servlet25LogoutStrategy.INSTANCE;

    private final HttpServletRequest delegate;

    public DelegatingHttpRequest(final HttpServletRequest delegate) {
        if (delegate == null) {
            throw new IllegalArgumentException("delegate is required");
        }
        this.delegate = delegate;
    }

    @Override
    public String getMethod() {
        return delegate.getMethod();
    }

    @Override
    public String getRequestURL() {
        return delegate.getRequestURL().toString();
    }

    @Override
    public String getRequestURI() {
        return delegate.getRequestURI();
    }

    @Override
    public String getQueryString() {
        return delegate.getQueryString();
    }

    @Override
    public String getContentType() {
        return delegate.getContentType();
    }

    @Override
    public String getParameter(final String name) {
        return delegate.getParameter(name);
    }

    @Override
    public String getHeader(final String name) {
        return delegate.getHeader(name);
    }

    @Override
    public ClientSession getSession() {
        final HttpSession sessionDelegate = delegate.getSession(false);
        if (sessionDelegate == null) return null;
        return new DelegatingClientSession(sessionDelegate);
    }

    @Override
    public ClientSession getOrCreateSession() {
        final HttpSession sessionDelegate = delegate.getSession(true);
        return new DelegatingClientSession(sessionDelegate);
    }


    @Override
    public boolean isSecure() {
        return delegate.isSecure();
    }

    @Override
    public int getServerPort() {
        return delegate.getServerPort();
    }

    @Override
    public void logout() {
        logoutStrategy.logout(delegate);
    }

    private static boolean isServlet30() {
        try {
            return HttpServletRequest.class.getMethod("logout") != null;
        } catch (final NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * A strategy for performing logout for an HTTP servlet request.
     */
    private interface LogoutStrategy {

        void logout(HttpServletRequest request);

    }

    private static class Servlet25LogoutStrategy implements LogoutStrategy {

        static final LogoutStrategy INSTANCE = new Servlet25LogoutStrategy();

        private Servlet25LogoutStrategy() {}

        @Override
        public void logout(final HttpServletRequest request) {
            // nothing additional to do here
        }

    }

    private static class Servlet30LogoutStrategy implements LogoutStrategy {

        static final LogoutStrategy INSTANCE = new Servlet30LogoutStrategy();

        private Servlet30LogoutStrategy() {}

        @Override
        public void logout(final HttpServletRequest request) {
            try {
                request.logout();
            } catch (ServletException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

}
