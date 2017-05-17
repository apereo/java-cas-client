package org.jasig.cas.client.validation;

import java.util.List;
import java.util.Map;

/**
 * This is {@link Cas30JsonServiceTicketValidator}.
 *
 * @author Misagh Moayyed
 */
public class Cas30JsonServiceTicketValidator extends Cas30ProxyTicketValidator {
    public Cas30JsonServiceTicketValidator(final String casServerUrlPrefix) {
        super(casServerUrlPrefix);
        getCustomParameters().put("format", "JSON");
    }

    @Override
    protected List<String> parseProxiesFromResponse(final String response) {
        return super.parseProxiesFromResponse(response);
    }

    @Override
    protected String parseProxyGrantingTicketFromResponse(final String response) {
        return super.parseProxyGrantingTicketFromResponse(response);
    }

    @Override
    protected String parsePrincipalFromResponse(final String response) {
        return super.parsePrincipalFromResponse(response);
    }

    @Override
    protected String parseAuthenticationFailureFromResponse(final String response) {
        return super.parseAuthenticationFailureFromResponse(response);
    }

    @Override
    protected Map<String, Object> extractCustomAttributes(final String xml) {
        return super.extractCustomAttributes(xml);
    }
}
