package org.jasig.cas.client.validation.json;

import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.AssertionImpl;
import org.jasig.cas.client.validation.Cas30ServiceTicketValidator;
import org.jasig.cas.client.validation.TicketValidationException;

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
        getCustomParameters().put("format", "JSON");
    }

    @Override
    protected Assertion parseResponseFromServer(final String response) throws TicketValidationException {
        final TicketValidationJsonResponse json = new JsonValidationResponseParser().parse(response);
        final String proxyGrantingTicketIou = json.getAuthenticationSuccess().getProxyGrantingTicket();
        final String proxyGrantingTicket;
        if (CommonUtils.isBlank(proxyGrantingTicketIou) || getProxyGrantingTicketStorage() == null) {
            proxyGrantingTicket = null;
        } else {
            proxyGrantingTicket = getProxyGrantingTicketStorage().retrieve(proxyGrantingTicketIou);
        }

        final Assertion assertion;
        final Map<String, Object> attributes = json.getAuthenticationSuccess().getAttributes();
        final String principal = json.getAuthenticationSuccess().getUser();
        if (CommonUtils.isNotBlank(proxyGrantingTicket)) {
            final AttributePrincipal attributePrincipal = new AttributePrincipalImpl(principal, attributes,
                    proxyGrantingTicket, getProxyRetriever());
            assertion = new AssertionImpl(attributePrincipal);
        } else {
            assertion = new AssertionImpl(new AttributePrincipalImpl(principal, attributes));
        }
        return assertion;
    }
}
