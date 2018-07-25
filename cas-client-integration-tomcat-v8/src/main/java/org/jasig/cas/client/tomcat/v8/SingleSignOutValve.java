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
package org.jasig.cas.client.tomcat.v8;

import java.io.IOException;
import javax.servlet.ServletException;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Session;
import org.apache.catalina.SessionEvent;
import org.apache.catalina.SessionListener;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.jasig.cas.client.session.SessionMappingStorage;
import org.jasig.cas.client.session.SingleSignOutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles logout request messages sent from the CAS server by ending the current
 * HTTP session.
 *
 * @author Marvin S. Addison
 * @version $Revision$ $Date$
 * @since 3.1.12
 *
 */
public class SingleSignOutValve extends ValveBase implements SessionListener {

    /** Logger instance */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final SingleSignOutHandler handler = new SingleSignOutHandler();

    public void setArtifactParameterName(final String name) {
        this.handler.setArtifactParameterName(name);
    }

    public void setLogoutParameterName(final String name) {
        this.handler.setLogoutParameterName(name);
    }
    
    public void setRelayStateParameterName(final String name) {
        this.handler.setRelayStateParameterName(name);
    }

    public void setCasServerUrlPrefix(final String casServerUrlPrefix) {
        this.handler.setCasServerUrlPrefix(casServerUrlPrefix);
    }

    public void setLogoutCallbackPath(String logoutCallbackPath) {
        this.handler.setLogoutCallbackPath(logoutCallbackPath);
    }

    public void setSessionMappingStorage(final SessionMappingStorage storage) {
        this.handler.setSessionMappingStorage(storage);
    }

    /** {@inheritDoc} */
    public void invoke(final Request request, final Response response) throws IOException, ServletException {
        if (this.handler.process(request, response)) {
            getNext().invoke(request, response);
        }
    }

    /** {@inheritDoc} */
    public void sessionEvent(final SessionEvent event) {
        if (Session.SESSION_DESTROYED_EVENT.equals(event.getType())) {
            logger.debug("Cleaning up SessionMappingStorage on destroySession event");
            this.handler.getSessionMappingStorage().removeBySessionById(event.getSession().getId());
        }
    }

    /** {@inheritDoc} */
    protected void startInternal() throws LifecycleException {
        super.startInternal();
        logger.info("Starting...");
        this.handler.init();
        logger.info("Startup completed.");
    }
}
