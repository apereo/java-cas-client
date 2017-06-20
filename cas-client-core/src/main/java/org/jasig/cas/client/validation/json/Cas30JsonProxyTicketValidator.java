package org.jasig.cas.client.validation.json;

import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.Cas30ProxyTicketValidator;
import org.jasig.cas.client.validation.TicketValidationException;

import java.util.Collections;
import java.util.List;

/**
 * This is {@link Cas30JsonProxyTicketValidator} that attempts to parse the CAS validation response
 * as JSON. Very similar to {@link Cas30JsonServiceTicketValidator}, it also honors proxies as the name suggests.
 *
 * @author Misagh Moayyed
 */
public class Cas30JsonProxyTicketValidator extends Cas30ProxyTicketValidator {
    public Cas30JsonProxyTicketValidator(final String casServerUrlPrefix) {
        super(casServerUrlPrefix);
        setCustomParameters(Collections.singletonMap("format", "JSON"));
    }

    @Override
    protected Assertion parseResponseFromServer(final String response) throws TicketValidationException {
        try {
            final TicketValidationJsonResponse json = new JsonValidationResponseParser().parse(response);
            return json.getAssertion(getProxyGrantingTicketStorage(), getProxyRetriever());
        } catch (final Exception e) {
            logger.warn("Unable parse the JSON response");
            return super.parseResponseFromServer(response);
        }
    }

    @Override
    protected List<String> parseProxiesFromResponse(final String response) {
        try {
            final TicketValidationJsonResponse json = new JsonValidationResponseParser().parse(response);
            return json.getServiceResponse().getAuthenticationSuccess().getProxies();
        } catch (final Exception e) {
            logger.warn("Unable to locate proxies from the JSON response", e);
            return super.parseProxiesFromResponse(response);
        }
    }
}
