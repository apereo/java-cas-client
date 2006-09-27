/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.integration.uportal;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.client.proxy.ProxyRetriever;
import org.jasig.cas.client.validation.TicketValidator;

/**
 * Extension of AbstractCasSecurityContext that retrieves the Service from the
 * ServiceHolder ThreadLocal object. This allows for a more flexible service to
 * be provided for ticket validation. This is needed as the normal
 * ISecurityContext has no mechanism for service urls based on requests.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class ThreadLocalAwareCasSecurityContext extends
        CasSecurityContext {

    /**
     * Unique Id for Serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiate a new CasSecurityContext, setting the required fields.
     *
     * @param ticketValidator the Ticket Validator.
     * @param service         the Service instance representing this uPortal instance.
     * @param proxyRetriever  the object used to retrieve proxies.
     */
    public ThreadLocalAwareCasSecurityContext(
            final TicketValidator ticketValidator, final Service service,
            final ProxyRetriever proxyRetriever) {
        super(ticketValidator, service, proxyRetriever);
    }

    protected Service getService() {
        return ServiceHolder.getService();
    }
}
