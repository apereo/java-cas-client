/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apereo.cas.client.validation.json;

import org.apereo.cas.client.validation.Assertion;
import org.apereo.cas.client.validation.Cas30ProxyTicketValidator;
import org.apereo.cas.client.validation.TicketValidationException;

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
