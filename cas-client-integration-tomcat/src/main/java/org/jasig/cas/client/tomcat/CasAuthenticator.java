package org.jasig.cas.client.tomcat;

import org.apache.catalina.authenticator.AuthenticatorBase;
import org.apache.catalina.connector.Request;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.TicketValidationException;
import org.jasig.cas.client.validation.TicketValidator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1.12
 */
public abstract class CasAuthenticator extends AuthenticatorBase {

    private static final String INFO = "org.jasig.cas.client.tomcat.CasAuthenticator/1.0";

    private static final Log log = LogFactory.getLog(CasAuthenticator.class);

    private String serverName;

    private String serviceUrl;

    private String casServerLoginUrl;

    private boolean encode;

    private boolean renew;

    protected abstract String getArtifactParameterName();

    protected abstract String getServiceParameterName();

    private TicketValidator ticketValidator;

    public String getInfo() {
        return INFO;
    }

    public boolean authenticate(final Request request, final HttpServletResponse httpServletResponse, final LoginConfig loginConfig) throws IOException {
        final Assertion assertion = (Assertion) request.getSession(true).getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION);

        if (assertion != null) {
            return isKnownUser(assertion);
        }


        

        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    protected boolean isKnownUser(final Assertion assertion) {
        return true;
    }

    protected boolean isAuthenticatedRequest(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        final String ticket = CommonUtils.safeGetParameter(request, getArtifactParameterName());
        final String serviceUrl = CommonUtils.constructServiceUrl(request, response, this.serviceUrl, this.serverName, getArtifactParameterName(), this.encode);

        if (CommonUtils.isBlank(ticket)) {
            final String urlToRedirectTo = CommonUtils.constructRedirectUrl(this.casServerLoginUrl, getServiceParameterName(), serviceUrl, this.renew, false);
            response.sendRedirect(urlToRedirectTo);
            return false;
        }

        try {
            final Assertion assertion = this.ticketValidator.validate(ticket, serviceUrl);
            request.getSession(true).setAttribute(AbstractCasFilter.CONST_CAS_ASSERTION, assertion);
            return isKnownUser(assertion);
        } catch (final TicketValidationException e) {
            return false;
        }

    }

    
}
