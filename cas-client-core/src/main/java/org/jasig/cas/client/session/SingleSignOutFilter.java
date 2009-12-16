/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.client.util.AbstractConfigurationFilter;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.util.XmlUtils;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Implements the Single Sign Out protocol.  It handles registering the session and destroying the session.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class SingleSignOutFilter extends AbstractConfigurationFilter {

    /**
     * The name of the artifact parameter.  This is used to capture the session identifier.
     */
    private String artifactParameterName = "ticket";

    private static SessionMappingStorage SESSION_MAPPING_STORAGE = new HashMapBackedSessionMappingStorage();
    private static Log log = LogFactory.getLog(SingleSignOutFilter.class);

    public void init(final FilterConfig filterConfig) throws ServletException {
        if (!isIgnoreInitConfiguration()) {
            setArtifactParameterName(getPropertyFromInitParams(filterConfig, "artifactParameterName", "ticket"));
        }
        init();
    }

    public void init() {
        CommonUtils.assertNotNull(this.artifactParameterName, "artifactParameterName cannot be null.");
        CommonUtils.assertNotNull(SESSION_MAPPING_STORAGE, "sessionMappingStorage cannote be null.");
    }

    public void setArtifactParameterName(final String artifactParameterName) {
        this.artifactParameterName = artifactParameterName;
    }

    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) servletRequest;

        if ("POST".equals(request.getMethod())) {
            final String logoutRequest = CommonUtils.safeGetParameter(request, "logoutRequest");

            if (CommonUtils.isNotBlank(logoutRequest)) {

                if (log.isTraceEnabled()) {
                    log.trace ("Logout request=[" + logoutRequest + "]");
                }
                
                final String sessionIdentifier = XmlUtils.getTextForElement(logoutRequest, "SessionIndex");

                if (CommonUtils.isNotBlank(sessionIdentifier)) {
                	final HttpSession session = SESSION_MAPPING_STORAGE.removeSessionByMappingId(sessionIdentifier);

                	if (session != null) {
                        String sessionID = session.getId();

                        if (log.isDebugEnabled()) {
                            log.debug ("Invalidating session [" + sessionID + "] for ST [" + sessionIdentifier + "]");
                        }
                        
                        try {
                        	session.invalidate();
                        } catch (final IllegalStateException e) {
                        	log.debug(e,e);
                        }
                	}
                  return;
                }
            }
        } else {
        	final String artifact = CommonUtils.safeGetParameter(request, this.artifactParameterName);

            if (CommonUtils.isNotBlank(artifact)) {
                final HttpSession session = request.getSession(true);

                if (log.isDebugEnabled()) {
                    log.debug("Storing session identifier for " + session.getId());
                }

                try {
                    SESSION_MAPPING_STORAGE.removeBySessionById(session.getId());
                } catch (final Exception e) {
                    // ignore if the session is already marked as invalid.  Nothing we can do!
                }
                SESSION_MAPPING_STORAGE.addSessionById(artifact, session);
            } else {
                log.debug("No Artifact Provided; no action taking place.");
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    public void setSessionMappingStorage(final SessionMappingStorage storage) {
    	SESSION_MAPPING_STORAGE = storage;
    }

    public static SessionMappingStorage getSessionMappingStorage() {
    	return SESSION_MAPPING_STORAGE;
    }

    public void destroy() {
        // nothing to do
    }
}
