/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.client.web.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.client.util.CommonUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Abstract class that contains common functionality amongst CAS filters.
 * <p>
 * You must specify the serverName or the serviceUrl. If you specify both, the
 * serviceUrl is used over the serverName.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public abstract class AbstractCasFilter implements Filter {

    /** Constant string representing the ticket parameter. */
    public static final String PARAM_TICKET = "ticket";

    /**
     * Constant representing where we store the <code>Assertion</code> in the
     * session.
     */
    public static final String CONST_ASSERTION = "_cas_assertion_";

    /** Constant representing where we flag a gatewayed request in the session. */
    public static final String CONST_GATEWAY = "_cas_gateway_";

    /** Constant representing where we flag a principal. */
    public static final String CONST_PRINCIPAL = "_cas_principal_";

    /** Instance of Commons Logging. */
    protected final Log log = LogFactory.getLog(this.getClass());

    /**
     * The name of the server in the following format: <hostname>:<port> where
     * port is optional if its a standard port.
     */
    private String serverName;

    /** The exact service url to match to. */
    private String serviceUrl;

    /** Whether to store the entry in session or not. Defaults to true. */
    private boolean useSession = true;

    public final void destroy() {
        // nothing to do
    }

    public final void doFilter(final ServletRequest servletRequest,
        final ServletResponse servletResponse, final FilterChain filterChain)
        throws IOException, ServletException {
        doFilterInternal((HttpServletRequest) servletRequest,
            (HttpServletResponse) servletResponse, filterChain);
    }

    protected abstract void doFilterInternal(HttpServletRequest request,
        HttpServletResponse response, FilterChain filterChain)
        throws IOException, ServletException;

    public void init(final FilterConfig filterConfig) throws ServletException {
        // nothing to do here
    }

    /**
     * Constructs a service url from the HttpServletRequest or from the given
     * serviceUrl. Prefers the serviceUrl provided if both a serviceUrl and a
     * serviceName.
     * 
     * @param request the HttpServletRequest
     * @param response the HttpServletResponse
     * @return the service url to use.
     */
    protected final String constructServiceUrl(final HttpServletRequest request,
        final HttpServletResponse response) {
        if (CommonUtils.isNotBlank(this.serviceUrl)) {
            return response.encodeURL(this.serviceUrl);
        }

        final StringBuffer buffer = new StringBuffer();

        synchronized (buffer) {
            buffer.append(request.isSecure() ? "https://" : "http://");
            buffer.append(this.serverName);
            buffer.append(request.getRequestURI());

            if (CommonUtils.isNotBlank(request.getQueryString())) {
                final int location = request.getQueryString().indexOf(
                    PARAM_TICKET + "=");

                if (location == 0) {
                    final String returnValue = response.encodeURL(buffer
                        .toString());
                    if (log.isDebugEnabled()) {
                        log.debug("serviceUrl generated: " + returnValue);
                    }
                    return returnValue;
                }

                buffer.append("?");

                if (location == -1) {
                    buffer.append(request.getQueryString());
                } else if (location > 0) {
                    final int actualLocation = request.getQueryString()
                        .indexOf("&" + PARAM_TICKET + "=");

                    if (actualLocation == -1) {
                        buffer.append(request.getQueryString());
                    } else if (actualLocation > 0) {
                        buffer.append(request.getQueryString().substring(0,
                            actualLocation));
                    }
                }
            }
        }

        final String returnValue = response.encodeURL(buffer.toString());
        if (log.isDebugEnabled()) {
            log.debug("serviceUrl generated: " + returnValue);
        }
        return returnValue;
    }

    protected final boolean isUseSession() {
        return this.useSession;
    }

    protected final void setUseSession(final boolean useSession) {
        this.useSession = useSession;
    }

    /**
     * Sets the serverName which should be of the format <hostname>:<port>
     * where port is optional if its a standard port. Either the serverName or
     * the serviceUrl must be set.
     * 
     * @param serverName the <hostname>:<port> combination.
     */
    public final void setServerName(final String serverName) {
        this.serverName = serverName;
    }

    public final void setServiceUrl(final String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public final void init() {
        CommonUtils.assertTrue(CommonUtils.isNotBlank(this.serverName)
            || CommonUtils.isNotBlank(this.serviceUrl),
            "either serverName or serviceUrl must be set");
        afterPropertiesSetInternal();
    }

    /**
     * Template method for additional checks at initialization.
     * 
     * @throws Exception any exceptions thrown upon initialization.
     */
    protected void afterPropertiesSetInternal() {
        // template method
    }
}
