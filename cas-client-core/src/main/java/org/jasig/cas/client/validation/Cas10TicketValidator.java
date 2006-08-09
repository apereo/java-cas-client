/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.client.validation;

import org.apache.commons.httpclient.HttpClient;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SimplePrincipal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

/**
 * Implementation of TicketValidator that follows the CAS 1.0 protocol.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class Cas10TicketValidator extends AbstractUrlBasedTicketValidator {

    public Cas10TicketValidator(final String casServerUrl, final boolean renew, final HttpClient httpClient) {
        super(casServerUrl, renew, httpClient);
    }

    protected String constructURL(final String ticketId, final Service service) {
        return getCasServerUrl() + "validate?ticket=" + ticketId
                + (isRenew() ? "&renew=true" : "") + "&service="
                + getEncodedService(service);
    }

    protected final Assertion parseResponse(final String response)
            throws ValidationException {
        if (response == null || "no\n\n".equals(response)
                || !response.startsWith("yes")) {
            throw new ValidationException(
                    "'No' response returned from server for validation request.");
        }

        try {
            final BufferedReader reader = new BufferedReader(new StringReader(
                    response));
            reader.readLine();

            final Principal principal = new SimplePrincipal(reader.readLine());
            return new AssertionImpl(principal);
        } catch (final IOException e) {
            throw new ValidationException("Unable to parse response.", e);
        }
    }
}
