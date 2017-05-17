package org.jasig.cas.client.validation.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.validation.TicketValidationException;

import java.util.List;
import java.util.Map;

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
    
    public TicketValidationJsonResponse parse(final String response) throws TicketValidationException {
        try {
            final TicketValidationJsonResponse json = this.objectMapper.readValue(response, TicketValidationJsonResponse.class);

            if (json == null || json.getAuthenticationFailure() != null && json.getAuthenticationSuccess() != null) {
                throw new TicketValidationException("Invalid JSON response; either the response is empty or it indicates both a success "
                        + "and a failure event, which is indicative of a server error. The actual response is " + response);
            }

            if (json.getAuthenticationFailure() != null) {
                final String error = json.getAuthenticationFailure().getDescription()
                        + " - " + json.getAuthenticationFailure().getDescription();
                throw new TicketValidationException(error);
            }

            final String principal = json.getAuthenticationSuccess().getUser();
            if (CommonUtils.isEmpty(principal)) {
                throw new TicketValidationException("No principal was found in the response from the CAS server.");
            }
            return json;
        } catch (final Exception e) {
            throw new RuntimeException("Unable to parse JSON validation response", e);
        }
    }
}
