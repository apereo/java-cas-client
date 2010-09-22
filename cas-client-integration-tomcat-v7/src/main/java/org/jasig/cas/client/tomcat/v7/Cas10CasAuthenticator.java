/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.tomcat.v7;

import org.apache.catalina.LifecycleException;
import org.jasig.cas.client.validation.Cas10TicketValidator;
import org.jasig.cas.client.validation.TicketValidator;

/**
 * Authenticator that handles CAS 1.0 protocol.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1.12
 */
public final class Cas10CasAuthenticator extends AbstractCasAuthenticator {
    public static final String AUTH_METHOD = "CAS10";
    
    private static final String NAME = Cas10CasAuthenticator.class.getName();

    private Cas10TicketValidator ticketValidator;

    protected TicketValidator getTicketValidator() {
        return this.ticketValidator;
    }
    
    protected String getAuthenticationMethod() {
        return AUTH_METHOD;
    }

    protected String getName() {
        return NAME;
    }

    protected void startInternal() throws LifecycleException {
        super.startInternal();
        this.ticketValidator = new Cas10TicketValidator(getCasServerUrlPrefix());
    }
}
