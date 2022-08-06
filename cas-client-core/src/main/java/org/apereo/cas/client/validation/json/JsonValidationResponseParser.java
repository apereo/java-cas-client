/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apereo.cas.client.validation.json;

import org.apereo.cas.client.util.CommonUtils;
import org.apereo.cas.client.validation.TicketValidationException;

import com.fasterxml.jackson.databind.ObjectMapper;

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
