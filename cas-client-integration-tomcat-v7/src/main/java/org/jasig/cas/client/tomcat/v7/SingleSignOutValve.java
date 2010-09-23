/*
 * Copyright 2010 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.tomcat.v7;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.Session;
import org.apache.catalina.SessionEvent;
import org.apache.catalina.SessionListener;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jasig.cas.client.session.SessionMappingStorage;
import org.jasig.cas.client.session.SingleSignOutHandler;

/**
 * Handles logout request messages sent from the CAS server by ending the current
 * HTTP session.
 *
 * @author Marvin S. Addison
 * @version $Revision$
 *
 */
public class SingleSignOutValve extends ValveBase implements SessionListener {
    /** Logger instance */
    private final Log log = LogFactory.getLog(getClass());
    
    private final SingleSignOutHandler handler = new SingleSignOutHandler();

    public void setArtifactParameterName(final String name) {
        handler.setArtifactParameterName(name);
    }
    
    public void setLogoutParameterName(final String name) {
        handler.setLogoutParameterName(name);
    }

    public void setSessionMappingStorage(final SessionMappingStorage storage) {
        handler.setSessionMappingStorage(storage);
    }


    /** {@inheritDoc} */
    public void invoke(final Request request, final Response response) throws IOException, ServletException {
        if (this.handler.isTokenRequest(request)) {
            this.handler.recordSession(request);
            request.getSessionInternal(true).addSessionListener(this);
        }
        else if (this.handler.isLogoutRequest(request)) {
            this.handler.destroySession(request);
            // Do not proceed up valve chain
            return;
        } else {
            this.log.debug("Ignoring URI " + request.getRequestURI());
        }
        getNext().invoke(request, response);
    }
    

    /** {@inheritDoc} */
    public void sessionEvent(final SessionEvent event) {
        if (Session.SESSION_DESTROYED_EVENT.equals(event.getType())) {
            this.log.debug("Cleaning up SessionMappingStorage on destroySession event");
	        this.handler.getSessionMappingStorage().removeBySessionById(event.getSession().getId());
        }
    }

    /** {@inheritDoc} */
    protected void startInternal() throws LifecycleException {
        super.startInternal();
        this.log.info("Starting...");
        handler.init();
        this.log.info("Startup completed.");
    }
}
