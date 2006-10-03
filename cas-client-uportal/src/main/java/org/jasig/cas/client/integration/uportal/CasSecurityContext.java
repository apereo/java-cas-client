/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.integration.uportal;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.TicketValidator;
import org.jasig.cas.client.validation.ValidationException;
import org.jasig.portal.security.PortalSecurityException;
import org.jasig.portal.security.provider.ChainingSecurityContext;

/**
 * Implementation of ICasSecurityContext that knows how to handle CAS ticket
 * validation, as well as the retrieval of Proxy Tickets.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class CasSecurityContext extends ChainingSecurityContext implements
        ICasSecurityContext {

    /**
     * Unique Id for Serialization
     */
    private static final long serialVersionUID = 1L;

    /**
     * Instance of TicketValidator to validate tickets.
     */
    private final TicketValidator ticketValidator;

    /**
     * Instance of Service representing uPortal instance.
     */
    private final Service service;

    /**
     * Assertion about the person this security context is for.
     */
    private Assertion assertion;

    /**
     * Instantiate a new CasSecurityContext, setting the required fields.
     *
     * @param ticketValidator the TicketValidator to validate tickets.
     * @param service         the Service representing the portal.
     */
    public CasSecurityContext(final TicketValidator ticketValidator, final Service service) {
        CommonUtils.assertNotNull(ticketValidator, "ticketValidator cannot be null.");
        CommonUtils.assertNotNull(service, "service cannot be null.");

        log.trace("Initalizing CasSecurityContext");
        this.ticketValidator = ticketValidator;
        this.service = service;
    }

    public final String getProxyTicket(final Service targetService) {
        return this.assertion.getProxyTicketFor(targetService);
    }

    public final int getAuthType() {
        return ICasSecurityContext.CAS_AUTHTYPE;
    }

    public final synchronized void authenticate() throws PortalSecurityException {
        this.isauth = false;
        final String serviceTicket = new String(
                this.myOpaqueCredentials.credentialstring);
        final Service service = getService();

        if (log.isDebugEnabled()) {
            log.debug("Attempting to validate ticket [" + serviceTicket
                    + "] for service [" + service.toString());
        }

        try {
            this.assertion = this.ticketValidator.validate(serviceTicket,
                    service);
            this.myAdditionalDescriptor = null;
            this.myPrincipal.setUID(this.assertion.getPrincipal().getId());
            this.isauth = true;
            super.authenticate();
        } catch (final ValidationException e) {
            log.warn(e, e);
            throw new PortalSecurityException(e.getMessage(), e);
        }
    }

    protected Service getService() {
        return this.service;
    }
}
