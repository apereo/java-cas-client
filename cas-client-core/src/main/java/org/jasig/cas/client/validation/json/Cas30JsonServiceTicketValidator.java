package org.jasig.cas.client.validation.json;

import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.Cas30ServiceTicketValidator;
import org.jasig.cas.client.validation.TicketValidationException;

import java.util.Collections;
import java.util.Map;

/**
 * This is {@link Cas30JsonServiceTicketValidator} that attempts to parse the CAS validation response
 * as JSON. If the response is not formatted as JSON, it shall fallback to the XML default syntax.
 * The JSON response provides advantages in terms of naming and parsing CAS attributes that have special
 * names that otherwise may not be encoded as XML, such as the invalid {@code <cas:special:attribute>value</cas:special:attribute>}
 *
 * @author Misagh Moayyed
 */
public class Cas30JsonServiceTicketValidator extends Cas30ServiceTicketValidator {

    public Cas30JsonServiceTicketValidator(final String casServerUrlPrefix) {
        super(casServerUrlPrefix);
        setCustomParameters(Collections.singletonMap("format", "JSON"));
    }

    @Override
    protected Assertion parseResponseFromServer(final String response) throws TicketValidationException {
        try {
            final TicketValidationJsonResponse json = new JsonValidationResponseParser().parse(response);
            return json.getAssertion(getProxyGrantingTicketStorage(), getProxyRetriever());
        } catch (final Exception e) {
            logger.warn("Unable parse the JSON response", e);
            return super.parseResponseFromServer(response);
        }
    }

    @Override
    protected Map<String, Object> extractCustomAttributes(final String xml) {
        return Collections.emptyMap();
    }
}
