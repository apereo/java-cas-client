/*
 * Copyright 2010 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.tomcat.v7;

import org.apache.catalina.LifecycleException;
import org.jasig.cas.client.validation.Saml11TicketValidator;
import org.jasig.cas.client.validation.TicketValidator;

/**
 * CAS authenticator that uses the SAML 1.1 protocol.
 *
 * @author Marvin S. Addison
 * @version $Revision$
 *
 */
public class Saml11Authenticator extends AbstractAuthenticator {
    public static final String AUTH_METHOD = "SAML11";

    private Saml11TicketValidator ticketValidator;
    
    /** SAML protocol clock drift tolerance in ms */
    private int tolerance = -1;


    /**
     * @param ms SAML clock drift tolerance in milliseconds.
     */
    public void setTolerance(final int ms) {
        this.tolerance = ms;
    }

    protected void startInternal() throws LifecycleException {
        super.startInternal();
        this.ticketValidator = new Saml11TicketValidator(getCasServerUrlPrefix());
        if (this.tolerance > -1) {
	        this.ticketValidator.setTolerance(this.tolerance);
        }
        if (getEncoding() != null) {
            this.ticketValidator.setEncoding(getEncoding());
        }
        this.ticketValidator.setRenew(isRenew());
    }

    protected TicketValidator getTicketValidator() {
        return this.ticketValidator;
    }
    
    protected String getAuthenticationMethod() {
        return AUTH_METHOD;
    }
    
    /** {@inheritDoc} */
    protected String getArtifactParameterName() {
        return "SAMLart";
    }

    /** {@inheritDoc} */
    protected String getServiceParameterName() {
        return "TARGET";
    }

}
