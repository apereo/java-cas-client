package org.jasig.cas.client.validation.json;

import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.TicketValidationException;

/**
 * This is {@link Cas30JsonProxyTicketValidator} that attempts to parse the CAS validation response
 * as JSON. Very similar to {@link Cas30JsonServiceTicketValidator}, it also honors proxies as the name suggests.
 *
 * @author Misagh Moayyed
 */
public class Cas30JsonProxyTicketValidator extends Cas30JsonServiceTicketValidator {
    public Cas30JsonProxyTicketValidator(final String casServerUrlPrefix) {
        super(casServerUrlPrefix);
        getCustomParameters().put("format", "JSON");
    }

    @Override
    protected Assertion parseResponseFromServer(final String response) throws TicketValidationException {
        return super.parseResponseFromServer(response);
    }
}
