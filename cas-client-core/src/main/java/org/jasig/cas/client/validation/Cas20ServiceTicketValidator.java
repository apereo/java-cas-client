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

    public Cas20ServiceTicketValidator(final String casServerUrl, final boolean renew, final HttpClient httpClient, final ProxyGrantingTicketStorage proxyGrantingTicketStorage) {
        this(casServerUrl, renew, httpClient, null, proxyGrantingTicketStorage);
    }

    public Cas20ServiceTicketValidator(final String casServerUrl, final boolean renew, final HttpClient httpClient, final Service proxyCallbackUrl, final ProxyGrantingTicketStorage proxyGrantingTicketStorage) {
        super(casServerUrl, renew, httpClient);
        CommonUtils.assertNotNull(proxyGrantingTicketStorage,
                "proxyGrantingTicketStorage cannot be null");
        this.proxyCallbackUrl = proxyCallbackUrl;
        this.proxyGrantingTicketStorage = proxyGrantingTicketStorage;
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

    protected final Assertion parseResponse(String response)
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

        if (CommonUtils.isNotBlank(proxyGrantingTicketIou)) {
            return getValidAssertionInternal(response, new AssertionImpl(
                    new SimplePrincipal(principal), null,
                    this.proxyGrantingTicketStorage
                            .retrieve(proxyGrantingTicketIou)));
        }

        return getValidAssertionInternal(response, new AssertionImpl(
                new SimplePrincipal(principal)));
    }

    protected String getValidationUrlName() {
        return "serviceValidate";
    }

    protected Assertion getValidAssertionInternal(final String response,
                                                  final Assertion assertion) throws ValidationException {
        return assertion;
    }
}
