/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.tomcat.v6;

import org.apache.catalina.LifecycleException;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.validation.Cas20ProxyTicketValidator;
import org.jasig.cas.client.validation.TicketValidator;

/**
 * Authenticator that handles the CAS 2.0 protocol with proxying support.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1.12
 */
public final class Cas20ProxyCasAuthenticator extends AbstractCasAuthenticator {
    public static final String AUTH_METHOD = "CAS20-PROXY";

    private Cas20ProxyTicketValidator ticketValidator;
    
    private boolean acceptAnyProxy;

    private String allowedProxyChains;

    public void setAcceptAnyProxy(final boolean acceptAnyProxy) {
        this.acceptAnyProxy = acceptAnyProxy;
    }

    public void setAllowedProxyChains(final String allowedProxyChains) {
        this.allowedProxyChains = allowedProxyChains;
    }

    protected TicketValidator getTicketValidator() {
        return this.ticketValidator;
    }
    
    protected String getAuthenticationMethod() {
        return AUTH_METHOD;
    }

    public void start() throws LifecycleException {
        super.start();
        this.ticketValidator = new Cas20ProxyTicketValidator(getCasServerUrlPrefix());
        this.ticketValidator.setRenew(isRenew());
        this.ticketValidator.setProxyCallbackUrl(getProxyCallbackUrl());
        this.ticketValidator.setProxyGrantingTicketStorage(ProxyCallbackValve.getProxyGrantingTicketStorage());
        this.ticketValidator.setAcceptAnyProxy(this.acceptAnyProxy);
        this.ticketValidator.setAllowedProxyChains(CommonUtils.createProxyList(this.allowedProxyChains));
        if (getEncoding() != null) {
            this.ticketValidator.setEncoding(getEncoding());
        }
        lifecycle.fireLifecycleEvent(AFTER_START_EVENT, null);
        this.log.debug("Startup completed.");
    }
}
