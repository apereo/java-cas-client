/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.validation;

import org.apache.commons.httpclient.HttpClient;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorage;
import org.jasig.cas.client.proxy.ProxyRetriever;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.util.XmlUtils;

/**
 * Implementation of TicketValidator that follows the CAS 2.0 protocol (without
 * proxying).
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class Cas20ServiceTicketValidator extends
        AbstractUrlBasedTicketValidator {

    /**
     * Proxy callback url to send to the CAS server.
     */
    private final Service proxyCallbackUrl;

    /**
     * The storage mechanism for the ProxyGrantingTickets.
     */
    private final ProxyGrantingTicketStorage proxyGrantingTicketStorage;

    /**
     * Injected into Assertions to allow them to retrieve proxy tickets.
     */
    private final ProxyRetriever proxyRetriever;

    public Cas20ServiceTicketValidator(final String casServerUrl, final boolean renew, final HttpClient httpClient) {
        this(casServerUrl, renew, httpClient, null, null, null);
    }

    public Cas20ServiceTicketValidator(final String casServerUrl, final boolean renew, final HttpClient httpClient, final Service proxyCallbackUrl, final ProxyGrantingTicketStorage proxyGrantingTicketStorage, final ProxyRetriever proxyRetriever) {
        super(casServerUrl, renew, httpClient);

        if (proxyCallbackUrl != null) {
            CommonUtils.assertNotNull(proxyGrantingTicketStorage,
                    "proxyGrantingTicketStorage cannot be null");
            CommonUtils.assertNotNull(proxyRetriever, "proxyRetriever cannot be null.");
        }
        this.proxyCallbackUrl = proxyCallbackUrl;
        this.proxyGrantingTicketStorage = proxyGrantingTicketStorage;
        this.proxyRetriever = proxyRetriever;
    }

    protected String constructURL(final String ticketId,
                                  final Service service) {
        return getCasServerUrl()
                + getValidationUrlName()
                + "?ticket="
                + ticketId
                + (isRenew() ? "&renew=true" : "")
                + "&service="
                + getEncodedService(service)
                + (this.proxyCallbackUrl != null ? "&pgtUrl="
                + getEncodedService(this.proxyCallbackUrl) : "");
    }

    protected final Assertion parseResponse(final String response)
            throws ValidationException {

        final String error = XmlUtils.getTextForElement(response,
                "authenticationFailure");

        if (CommonUtils.isNotBlank(error)) {
            log.debug("Validation of ticket failed: " + error);
            throw new ValidationException(error);
        }

        final String principal = XmlUtils.getTextForElement(response, "user");
        final String proxyGrantingTicketIou = XmlUtils.getTextForElement(
                response, "proxyGrantingTicket");

        if (CommonUtils.isEmpty(principal)) {
            throw new ValidationException("No principal found.");
        }

        return getValidAssertionInternal(response, principal, proxyGrantingTicketIou);
    }

    protected String getValidationUrlName() {
        return "serviceValidate";
    }

    protected final Assertion getAssertionBasedOnProxyGrantingTicketIou(final String proxyGrantingTicketIou, final String principal) {
        if (CommonUtils.isNotBlank(proxyGrantingTicketIou)) {
            return new AssertionImpl(
                    new SimplePrincipal(principal), null, this.proxyRetriever, this.proxyGrantingTicketStorage == null ? null : this.proxyGrantingTicketStorage
                    .retrieve(proxyGrantingTicketIou));
        } else {
            return new AssertionImpl(new SimplePrincipal(principal));
        }
    }

    protected Assertion getValidAssertionInternal(final String response, final String principal, final String proxyGrantingTicketIou) throws ValidationException {
        return getAssertionBasedOnProxyGrantingTicketIou(proxyGrantingTicketIou, principal);
    }
}
