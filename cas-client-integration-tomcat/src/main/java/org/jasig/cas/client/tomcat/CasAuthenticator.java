package org.jasig.cas.client.tomcat;

import org.apache.catalina.authenticator.AuthenticatorBase;
import org.apache.catalina.connector.Request;
import org.apache.catalina.deploy.LoginConfig;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.validation.Assertion;

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

    private String serverName;

    private String serviceUrl;

    private String casServerLoginUrl;

    private boolean encode;

    private boolean renew;

    protected abstract String getArtifactParameterName();

    protected abstract String getServiceParameterName();

    public String getInfo() {
        return INFO;
    }

    public boolean authenticate(final Request request, final HttpServletResponse httpServletResponse, final LoginConfig loginConfig) throws IOException {
        final Assertion assertion = (Assertion) request.getSession(true).getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION);

        if (assertion != null) {
            return true;
        }

        final String ticket = CommonUtils.safeGetParameter(request, getArtifactParameterName());

        if (CommonUtils.isBlank(ticket)) {
            final String serviceUrl = CommonUtils.constructServiceUrl(request, httpServletResponse, this.serviceUrl, this.serverName, getArtifactParameterName(), this.encode);
            final String urlToRedirectTo = CommonUtils.constructRedirectUrl(this.casServerLoginUrl, getServiceParameterName(), serviceUrl, this.renew, false);

            httpServletResponse.sendRedirect(urlToRedirectTo);
            return false;
        }

        final Principal principal = this.context.getRealm().authenticate(null, ticket);

        

        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    
}
