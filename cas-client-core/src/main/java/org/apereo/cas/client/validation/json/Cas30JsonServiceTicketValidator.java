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

import org.apereo.cas.client.validation.Assertion;
import org.apereo.cas.client.validation.Cas30ServiceTicketValidator;
import org.apereo.cas.client.validation.TicketValidationException;

import tools.jackson.core.JacksonException;

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
            final var json = new JsonValidationResponseParser().parse(response);
            return json.getAssertion(getProxyGrantingTicketStorage(), getProxyRetriever());
        } catch (final JacksonException e) {
            logger.warn("Unable parse the JSON response. Falling back to XML", e);
            return super.parseResponseFromServer(response);
        }
    }

    @Override
    protected Map<String, Object> extractCustomAttributes(final String xml) {
        return Collections.emptyMap();
    }
}
