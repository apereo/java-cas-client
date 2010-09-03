/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.tomcat;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.authenticator.AuthenticatorBase;
import org.apache.catalina.authenticator.Constants;
import org.apache.catalina.connector.Request;
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

    protected void startInternal() throws LifecycleException {
        super.startInternal();
        try {
            CommonUtils.assertNotNull(this.casServerUrlPrefix, "casServerUrlPrefix cannot be null.");
            CommonUtils.assertNotNull(this.casServerLoginUrl, "casServerLoginUrl cannot be null.");
            CommonUtils.assertTrue(this.serverName != null || this.serviceUrl != null, "either serverName or serviceUrl must be set.");

        } catch (final Exception e) {
            throw new LifecycleException(e);
        }
    }

    public final void setCasServerUrlPrefix(final String casServerUrlPrefix) {
        this.casServerUrlPrefix = casServerUrlPrefix;
    }

    public final void setCasServerLoginUrl(final String casServerLoginUrl) {
        this.casServerLoginUrl = casServerLoginUrl;
    }

    public final boolean isEncode() {
        return this.encode;
    }

    public final void setEncode(final boolean encode) {
        this.encode = encode;
    }

    protected final boolean isRenew() {
        return this.renew;
    }

    public void setRenew(final boolean renew) {
        this.renew = renew;
    }


    public final void setServerName(final String serverName) {
        this.serverName = serverName;
    }

    public final void setServiceUrl(final String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public final String getInfo() {
        return INFO;
    }

    public final boolean authenticate(final Request request, final HttpServletResponse response, final LoginConfig loginConfig) throws IOException {
        final Principal principal = request.getUserPrincipal();
        final String ssoId = (String) request.getNote(Constants.REQ_SSOID_NOTE);

        if (principal != null && ssoId != null) {
            associate(ssoId, request.getSessionInternal(true));
            return true;
        }

        if (ssoId != null && reauthenticateFromSSO(ssoId, request)) {
            return true;
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
            final Principal p = context.getRealm().authenticate(newAssertion.getPrincipal().getName(), null);

            if (p != null) {
                register(request, response, p, Constants.SINGLE_SIGN_ON_COOKIE, p.getName(), null);
                return true;
            }
        } catch (final TicketValidationException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
            return false;
        }

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }
}
