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
package org.apereo.cas.client.validation;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockServletContext;

import jakarta.servlet.FilterConfig;

/**
 * Unit test for {@link Saml11TicketValidationFilter}.
 *
 * @author Marvin S. Addison
 */
public class Saml11TicketValidationFilterTests {
    @Test
    public void testRenewInitParamThrows() throws Exception {
        final var f = new Saml11TicketValidationFilter();
        final var config = new MockFilterConfig();
        config.addInitParameter("casServerUrlPrefix", "https://cas.example.com");
        config.addInitParameter("renew", "true");
        try {
            f.init(config);
            fail("Should have thrown IllegalArgumentException.");
        } catch (final IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Renew MUST"));
        }
    }

    @Test
    public void testAllowsRenewContextParam() throws Exception {
        final var f = new Saml11TicketValidationFilter();
        final var context = new MockServletContext();
        context.addInitParameter("casServerUrlPrefix", "https://cas.example.com");
        context.addInitParameter("renew", "true");
        context.addInitParameter("service", "http://www.jasig.org");
        final FilterConfig config = new MockFilterConfig(context);
        f.init(config);
        final var validator = f.getTicketValidator(config);
        assertTrue(validator instanceof Saml11TicketValidator);
        assertTrue(((Saml11TicketValidator) validator).isRenew());
    }
}
