package org.jasig.cas.client.tomcat;

import org.apache.catalina.authenticator.AuthenticatorBase;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.TicketValidationException;
import org.jasig.cas.client.validation.TicketValidator;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1.12
 */
public abstract class AbstractAuthenticator extends AuthenticatorBase {

    private static final String INFO = "org.jasig.cas.client.tomcat.AbstractAuthenticator/1.0";

    private static final Log log = LogFactory.getLog(AbstractAuthenticator.class);

    private String casServerLoginUrl;

    private String casServerUrlPrefix;

    private boolean encode;

    private boolean renew;

    protected abstract String getArtifactParameterName();

    protected abstract String getServiceParameterName();

    protected abstract TicketValidator getTicketValidator();

    private String serverName;

    private String serviceUrl;

    protected final String getCasServerUrlPrefix() {
        return this.casServerUrlPrefix;
    }

    public String getInfo() {
        return INFO;
    }

    public final boolean authenticate(final Request request, final HttpServletResponse response, final LoginConfig loginConfig) throws IOException {
        final Assertion assertion = (Assertion) request.getSession(true).getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION);

        if (assertion != null) {
            return isKnownUser(assertion, request, response);
        }

        final String token = request.getParameter(getArtifactParameterName());
        final String service = CommonUtils.constructServiceUrl(request, response, this.serviceUrl, this.serverName, getArtifactParameterName(), true);

        if (CommonUtils.isBlank(token)) {
            final String redirectUrl = CommonUtils.constructRedirectUrl(this.casServerLoginUrl, getServiceParameterName(), service, false, false);
            response.sendRedirect(redirectUrl);
            return false;
        }

        try {
            final Assertion newAssertion = getTicketValidator().validate(token, service);
            request.getSession(true).setAttribute(AbstractCasFilter.CONST_CAS_ASSERTION, newAssertion);
            return isKnownUser(newAssertion, request, response);
        } catch (final TicketValidationException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
            return false;
        }
    }

    protected boolean isKnownUser(final Assertion assertion, final Request request, final HttpServletResponse response) throws IOException {
        final String userName = assertion.getPrincipal().getName();
        final Principal principal = request.getUserPrincipal();

         if (principal == null) {
           // principal not already known; look it up via the configured realm
           principal = realm.getPrincipal(userName);
           if (principal != null) {
             // register it so that Tomcat can reuse it without another realm lookup
             register(request, response, principal, "CAS", userName, null);
           }
         }

         if (principal == null) {
           log.warn("unknown CAS user " + userName + " for "
               + request.getRequestURI());
           response.sendError(Response.SC_UNAUTHORIZED);
           return false;
         }
         request.setUserPrincipal(principal);
         return true;
    }
}
