/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
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
package org.jasig.cas.client.validation;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.junit.Test;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;

/**
 * Unit test for {@link Cas10TicketValidationFilter}.
 *
 * @author Marvin S. Addison
 */
public class Cas10TicketValidationFilterTests {
    @Test
    public void testThrowsRenewInitParam() throws Exception {
        final Cas10TicketValidationFilter f = new Cas10TicketValidationFilter();
        final MockFilterConfig config = new MockFilterConfig();
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
        final Cas10TicketValidationFilter f = new Cas10TicketValidationFilter();
        final MockServletContext context = new MockServletContext();
        context.addInitParameter("casServerUrlPrefix", "https://cas.example.com");
        context.addInitParameter("renew", "true");
        final TicketValidator validator = f.getTicketValidator(new MockFilterConfig(context));
        assertTrue(validator instanceof Cas10TicketValidator);
        assertTrue(((Cas10TicketValidator) validator).isRenew());
    }
}
