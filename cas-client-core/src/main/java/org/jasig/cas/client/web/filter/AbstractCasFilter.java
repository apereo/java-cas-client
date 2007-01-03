/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
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
 * <p/>
 * You must specify the serverName (format: hostname:port) or the serviceUrl. If you specify both, the
 * serviceUrl is used over the serverName.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public abstract class AbstractCasFilter implements Filter {

    /**
     * Constant representing where we store the <code>Assertion</code> in the
     * session.
     */
    public static final String CONST_ASSERTION = "_cas_assertion_";

    /**
     * Constant representing where we flag a gatewayed request in the session.
     */
    public static final String CONST_GATEWAY = "_cas_gateway_";

    /**
     * Constant representing where we flag a principal.
     */
    public static final String CONST_PRINCIPAL = "_cas_principal_";

    /**
     * Instance of Commons Logging.
     */
    protected final Log log = LogFactory.getLog(this.getClass());

    /**
     * The name of the server in the following format: <hostname>:<port> where
     * port is optional if its a standard port.
     */
    private final String service;

    /**
     * The exact service url to match to.
     */
    private final boolean isServerName;

    /**
     * Whether to store the entry in session or not. Defaults to true.
     */
    private boolean useSession = true;

    private String artifactParameterName = "ticket";

    protected AbstractCasFilter(final String service, final boolean isServerName) {
        CommonUtils.assertNotNull(service, "service must be set");

        this.service = service;
        this.isServerName = isServerName;

        log.info("Service set to: " + this.service + "; Is Server Name?  set to: " + this.isServerName + "Use Session set to: " + this.useSession);
    }

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
     * @param request  the HttpServletRequest
     * @param response the HttpServletResponse
     * @return the service url to use.
     */
    protected final String constructServiceUrl(final HttpServletRequest request,
                                               final HttpServletResponse response) {
        if (!isServerName) {
            return response.encodeURL(this.service);
        }

        final StringBuffer buffer = new StringBuffer();

        synchronized (buffer) {
            buffer.append(request.isSecure() ? "https://" : "http://");
            buffer.append(this.service);
            buffer.append(request.getRequestURI());

            if (CommonUtils.isNotBlank(request.getQueryString())) {
                final int location = request.getQueryString().indexOf(
                        this.artifactParameterName + "=");

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
                            .indexOf("&" + this.artifactParameterName + "=");

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

    public final void setUseSession(final boolean useSession) {
        this.useSession = useSession;
    }

    /**
     * Defaults to "ticket" based on the CAS 2 Specification.  Other examples include SAML artifacts which are defined as
     * "SAMLart"
     * 
     * @param artifactName
     */
    public final void setArtifactParameterName(final String artifactName) {
        this.artifactParameterName = artifactName;
    }

    protected final String getArtifactParameterName() {
        return this.artifactParameterName;
    }
}
