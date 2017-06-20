package org.jasig.cas.client.validation.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.validation.TicketValidationException;

import java.io.IOException;

/**
 * This is {@link JsonValidationResponseParser}.
 *
 * @author Misagh Moayyed
 */
final class JsonValidationResponseParser {
    private final ObjectMapper objectMapper;

    public JsonValidationResponseParser() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
    }

    public TicketValidationJsonResponse parse(final String response) throws TicketValidationException, IOException {
        if (CommonUtils.isBlank(response)) {
            throw new TicketValidationException("Invalid JSON response; The response is empty");
        }

        final TicketValidationJsonResponse json = this.objectMapper.readValue(response, TicketValidationJsonResponse.class);

        final TicketValidationJsonResponse.CasServiceResponseAuthentication serviceResponse = json.getServiceResponse();
        if (serviceResponse.getAuthenticationFailure() != null
                && serviceResponse.getAuthenticationSuccess() != null) {
            throw new TicketValidationException("Invalid JSON response; It indicates both a success "
                    + "and a failure event, which is indicative of a server error. The actual response is " + response);
        }

        if (serviceResponse.getAuthenticationFailure() != null) {
            final String error = json.getServiceResponse().getAuthenticationFailure().getCode()
                    + " - " + serviceResponse.getAuthenticationFailure().getDescription();
            throw new TicketValidationException(error);
        }

        final String principal = json.getServiceResponse().getAuthenticationSuccess().getUser();
        if (CommonUtils.isEmpty(principal)) {
            throw new TicketValidationException("No principal was found in the response from the CAS server.");
        }
        return json;
    }
}
