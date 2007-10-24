package org.jasig.cas.client.session;

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
 * Implements the Single Sign Out protocol.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class SingleSignOutFilter extends AbstractConfigurationFilter {

    private String artifactParameterName = "ticket";

    public void init(final FilterConfig filterConfig) throws ServletException {
        setArtifactParameterName(getPropertyFromInitParams(filterConfig, "artifactParameterName", "ticket"));
    }

    public void setArtifactParameterName(final String artifactParameterName) {
        this.artifactParameterName = artifactParameterName;
    }

    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) servletRequest;

        if ("POST".equals(request.getMethod())) {
            final String logoutRequest = request.getParameter("logoutRequest");

            if (CommonUtils.isNotBlank(logoutRequest)) {
                final String sessionIdentifier = XmlUtils.getTextForElement(logoutRequest, "SessionIndex");

                if (CommonUtils.isNotBlank(sessionIdentifier)) {
                    SingleSignOutHttpSessionListener.removeSession(sessionIdentifier);
                    return;
                }
            }
        } else {
            final String artifact = request.getParameter(this.artifactParameterName);
            final HttpSession session = request.getSession();
            if (CommonUtils.isNotBlank(artifact)) {
                SingleSignOutHttpSessionListener.addSession(artifact, session);
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    public void destroy() {
        // nothing to do
    }
}
