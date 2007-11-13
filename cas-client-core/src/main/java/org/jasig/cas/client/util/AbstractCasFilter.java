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

    /** Defines the parameter to look for for the artifact. */
    private String artifactParameterName = "ticket";

    /** Defines the parameter to look for for the service. */
    private String serviceParameterName = "service";

    /**
     * The name of the server.  Should be in the following format: {protocol}:{hostName}:{port}.
     * Standard ports can be excluded. */
    private String serverName;

    /** The exact url of the service. */
    private String service;

    public final void init(final FilterConfig filterConfig) throws ServletException {
        setServerName(getPropertyFromInitParams(filterConfig, "serverName", null));
        setService(getPropertyFromInitParams(filterConfig, "service", null));
        setArtifactParameterName(getPropertyFromInitParams(filterConfig, "artifactParameterName", "ticket"));
        setServiceParameterName(getPropertyFromInitParams(filterConfig, "serviceParameterName", "service"));

        initInternal(filterConfig);
        init();
    }

    /** Controls the ordering of filter initialiation and checking by defining a method that runs before the init. */
    protected void initInternal(final FilterConfig filterConfig) throws ServletException {
        // template method
    }

    /**
     * Initialization method.  Called by Filter's init method or by Spring.
     */
    public void init() {
        CommonUtils.assertNotNull(this.artifactParameterName, "artifactParameterName cannot be null.");
        CommonUtils.assertNotNull(this.serviceParameterName, "serviceParameterName cannot be null.");
        CommonUtils.assertTrue(CommonUtils.isNotEmpty(this.serverName) || CommonUtils.isNotEmpty(this.service), "serverName or service must be set.");
    }

    public final void destroy() {
        // nothing to do
    }

    protected final String constructServiceUrl(final HttpServletRequest request, final HttpServletResponse response) {
        return CommonUtils.constructServiceUrl(request, response, this.service, this.serverName, this.artifactParameterName);
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
