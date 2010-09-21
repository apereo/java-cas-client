/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.tomcat.v6;

import org.apache.catalina.LifecycleException;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.jasig.cas.client.validation.TicketValidator;

/**
 *  Authenticator that handles the CAS 2.0 protocol.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1.12
 */
public final class Cas20CasAuthenticator extends AbstractCasAuthenticator {
    public static final String AUTH_METHOD = "CAS20";
    
    private Cas20ServiceTicketValidator ticketValidator;

    protected TicketValidator getTicketValidator() {
        return this.ticketValidator;
    }
    
    protected String getAuthenticationMethod() {
        return AUTH_METHOD;
    }

    public void start() throws LifecycleException {
        super.start();
        this.ticketValidator = new Cas20ServiceTicketValidator(getCasServerUrlPrefix());
        if (getEncoding() != null) {
            this.ticketValidator.setEncoding(getEncoding());
        }
        this.ticketValidator.setProxyCallbackUrl(getProxyCallbackUrl());
        this.ticketValidator.setProxyGrantingTicketStorage(ProxyCallbackValve.getProxyGrantingTicketStorage());
        this.ticketValidator.setRenew(isRenew());
        lifecycle.fireLifecycleEvent(AFTER_START_EVENT, null);
        this.log.debug("Startup completed.");
    }
}
