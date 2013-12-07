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

<<<<<<< HEAD
=======
import org.jasig.cas.client.util.AbstractConfigurationFilter;
import org.jasig.cas.client.util.ReflectUtils;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.Closeable;
>>>>>>> v3.2.1-feature
import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import org.jasig.cas.client.util.AbstractConfigurationFilter;

/**
 * Implements the Single Sign Out protocol.  It handles registering the session and destroying the session.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class SingleSignOutFilter extends AbstractConfigurationFilter {

    private static final SingleSignOutHandler handler = new SingleSignOutHandler();

    public void init(final FilterConfig filterConfig) throws ServletException {
        if (!isIgnoreInitConfiguration()) {
            handler.setArtifactParameterName(getPropertyFromInitParams(filterConfig, "artifactParameterName", "ticket"));
<<<<<<< HEAD
            handler.setLogoutParameterName(getPropertyFromInitParams(filterConfig, "logoutParameterName",
                    "logoutRequest"));
            handler.setArtifactParameterOverPost(parseBoolean(getPropertyFromInitParams(filterConfig,
                    "artifactParameterOverPost", "false")));
            handler.setEagerlyCreateSessions(parseBoolean(getPropertyFromInitParams(filterConfig,
                    "eagerlyCreateSessions", "true")));
=======
            handler.setLogoutParameterName(getPropertyFromInitParams(filterConfig, "logoutParameterName", "logoutRequest"));
            String sessionMappingStorageClassName = getPropertyFromInitParams(filterConfig, "sessionMappingStorageClass", null);
            String ehcacheConfigFile = getPropertyFromInitParams(filterConfig, "ehcacheConfigFile", null);
            if (sessionMappingStorageClassName != null) {
                if (ehcacheConfigFile != null){
                    handler.setSessionMappingStorage(ReflectUtils.<SessionMappingStorage>newInstance(sessionMappingStorageClassName, ehcacheConfigFile));
                }else{
                    handler.setSessionMappingStorage(ReflectUtils.<SessionMappingStorage>newInstance(sessionMappingStorageClassName));
                }

            }
>>>>>>> v3.2.1-feature
        }
        handler.init();
    }

    public void setArtifactParameterName(final String name) {
        handler.setArtifactParameterName(name);
    }

    public void setLogoutParameterName(final String name) {
        handler.setLogoutParameterName(name);
    }

    public void setSessionMappingStorage(final SessionMappingStorage storage) {
        handler.setSessionMappingStorage(storage);
    }

<<<<<<< HEAD
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
            final FilterChain filterChain) throws IOException, ServletException {
=======
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {
>>>>>>> v3.2.1-feature
        final HttpServletRequest request = (HttpServletRequest) servletRequest;

        if (handler.isTokenRequest(request)) {
            handler.recordSession(request);
        } else if (handler.isLogoutRequest(request)) {
            handler.destroySession(request);
            // Do not continue up filter chain
            return;
        } else {
            logger.trace("Ignoring URI {}", request.getRequestURI());
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    public void destroy() {
        log.info("The container is shutting down ...");
        SessionMappingStorage sessionMappingStorage = handler.getSessionMappingStorage();
        if (sessionMappingStorage instanceof Closeable) {
            try {
                ((Closeable) sessionMappingStorage).close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    protected static SingleSignOutHandler getSingleSignOutHandler() {
        return handler;
    }
}
