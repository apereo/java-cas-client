package org.jasig.cas.client.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *  Abstract filter that contains code that is common to all CAS filters.
 *  <p>
 * The following filter options can be configured (either at the context-level or filter-level).
 * <ul>
 * <li><code>serverName</code> - the name of the CAS server, in the format: localhost:8080 or localhost:8443 or localhost</li>
 * <li><code>service</code> - the completely qualified service url, i.e. https://localhost/cas-client/app</li>
 * </ul>
 * <p>Please note that one of the two above parameters must be set.</p>
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public abstract class AbstractCasFilter extends AbstractConfigurationFilter {

    /** Represents the constant for where the assertion will be located in memory. */
    public static final String CONST_CAS_ASSERTION = "_const_cas_assertion_";

    /** Instance of commons logging for logging purposes. */
    protected final Log log = LogFactory.getLog(getClass());

    private String artifactParameterName = "ticket";

    private String serviceParameterName = "service";

    /**
     * The name of the server.  Should be in the following format: {protocol}:{hostName}:{port}.
     * Standard ports can be excluded. */
    private String serverName;

    /** The exact url of the service. */
    private String service;

    public void init(final FilterConfig filterConfig) throws ServletException {
        setServerName(getPropertyFromInitParams(filterConfig, "serverName", null));
        setService(getPropertyFromInitParams(filterConfig, "service", null));
        setArtifactParameterName(getPropertyFromInitParams(filterConfig, "artifactParameterName", "ticket"));
        setServiceParameterName(getPropertyFromInitParams(filterConfig, "serviceParameterName", "service"));
    }

    public final void destroy() {
        // nothing to do
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
        if (CommonUtils.isNotBlank(this.service)) {
            return response.encodeURL(this.service);
        }

        final StringBuffer buffer = new StringBuffer();

        synchronized (buffer) {
            if (!this.serverName.startsWith("https://") && !this.serverName.startsWith("http://")) {
                buffer.append(request.isSecure() ? "https://" : "http://");
            }
            
            buffer.append(this.serverName);
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

    public final void setServerName(final String serverName) {
        this.serverName = serverName;
    }

    public final void setService(final String service) {
        this.service = service;
    }

    public final void setArtifactParameterName(final String artifactParameterName) {
        this.artifactParameterName = artifactParameterName;
    }

    public final void setServiceParameterName(final String serviceParameterName) {
        this.serviceParameterName = serviceParameterName;
    }

    public final String getArtifactParameterName() {
        return this.artifactParameterName;
    }

    public final String getServiceParameterName() {
        return this.serviceParameterName;
    }
}
