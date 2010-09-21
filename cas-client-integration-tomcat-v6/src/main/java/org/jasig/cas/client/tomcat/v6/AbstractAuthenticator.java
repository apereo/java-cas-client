/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.tomcat.v6;

import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Realm;
import org.apache.catalina.authenticator.AuthenticatorBase;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.client.tomcat.AuthenticatorDelegate;
import org.jasig.cas.client.tomcat.CasRealm;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.validation.TicketValidator;

import java.io.IOException;
import java.security.Principal;

/**
 * Base authenticator for all authentication protocols supported by CAS.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1.12
 */
public abstract class AbstractAuthenticator extends AuthenticatorBase implements LifecycleListener {

    protected final Log log = LogFactory.getLog(getClass());
    
    private final AuthenticatorDelegate delegate = new AuthenticatorDelegate();

    private String casServerUrlPrefix;

    private String encoding;

    private boolean encode;

    private boolean renew;

    protected abstract String getAuthenticationMethod();

    protected abstract String getArtifactParameterName();

    protected abstract String getServiceParameterName();

    protected abstract TicketValidator getTicketValidator();


    public void start() throws LifecycleException {
        super.start();
        this.log.debug("Starting...");
        final Realm realm = this.context.getRealm();
        if (!(realm instanceof CasRealm)) {
            throw new LifecycleException("Expected CasRealm but got " + realm.getInfo());
        }
        try {
            CommonUtils.assertNotNull(this.casServerUrlPrefix, "casServerUrlPrefix cannot be null.");
            CommonUtils.assertNotNull(this.delegate.getCasServerLoginUrl(), "casServerLoginUrl cannot be null.");
            CommonUtils.assertTrue(
                    this.delegate.getServerName() != null || this.delegate.getServiceUrl() != null,
                    "either serverName or serviceUrl must be set.");
            this.delegate.setRealm((CasRealm) realm);   
        } catch (final Exception e) {
            throw new LifecycleException(e);
        }
        // Complete delegate initialization after the component is started.
        // See #lifecycleEvent() method.
        addLifecycleListener(this);
    }

    protected final String getCasServerUrlPrefix() {
        return this.casServerUrlPrefix;
    }

    public final void setCasServerUrlPrefix(final String casServerUrlPrefix) {
        this.casServerUrlPrefix = casServerUrlPrefix;
    }

    public final void setCasServerLoginUrl(final String casServerLoginUrl) {
        this.delegate.setCasServerLoginUrl(casServerLoginUrl);
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
        this.delegate.setServerName(serverName);
    }

    public final void setServiceUrl(final String serviceUrl) {
        this.delegate.setServiceUrl(serviceUrl);
    }

    protected final String getEncoding() {
        return this.encoding;
    }

    public final void setEncoding(final String encoding) {
        this.encoding = encoding;
    }

    /** {@inheritDoc} */
    public final boolean authenticate(final Request request, final Response response, final LoginConfig loginConfig) throws IOException {
        Principal principal = request.getUserPrincipal();
        boolean result = false;
        if (principal == null) {
            // Authentication sets the response headers for status and redirect if needed
	        principal = this.delegate.authenticate(request.getRequest(), response);
            if (principal != null) {
                request.setAuthType(getAuthenticationMethod());
                request.setUserPrincipal(principal);
                result = true;
            }
        } else {
            result = true;
        }
        return result;
    }

    /** {@inheritDoc} */
    public void lifecycleEvent(final LifecycleEvent event) {
        if (AFTER_START_EVENT.equals(event.getType())) {
	        this.log.debug("Processing lifecycle event " + AFTER_START_EVENT);
            this.delegate.setTicketValidator(getTicketValidator());
            this.delegate.setArtifactParameterName(getArtifactParameterName());
            this.delegate.setServiceParameterName(getServiceParameterName());
        }
    }

    /** {@inheritDoc} */
    public String getInfo() {
        return getClass().getName() + "/1.0";
    }
}
