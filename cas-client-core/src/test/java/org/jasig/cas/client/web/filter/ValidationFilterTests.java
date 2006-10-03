/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.web.filter;

import junit.framework.TestCase;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.AssertionImpl;
import org.jasig.cas.client.validation.TicketValidator;
import org.jasig.cas.client.validation.ValidationException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * Tests for the TicketValidationFilter.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class ValidationFilterTests extends TestCase {

    private TicketValidationFilter filter;

    protected void setUp() throws Exception {
        this.filter = new TicketValidationFilter("localhost:8443", null, new TicketValidator() {

            public Assertion validate(final String ticketId,
                                      final Service service) throws ValidationException {
                if (ticketId.equals("true")) {
                    return new AssertionImpl(new SimplePrincipal("test"));
                }
                throw new ValidationException("error validating ticket.");
            }
        });
    }

    protected void tearDown() throws Exception {
        this.filter.destroy();
    }

    public void testNoTicket() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockHttpSession session = new MockHttpSession();
        request.setSession(session);
        final FilterChain filterChain = new FilterChain() {

            public void doFilter(final ServletRequest arg0,
                                 final ServletResponse arg1) throws IOException,
                    ServletException {
                // nothing to do
            }
        };

        this.filter.doFilter(request, response, filterChain);

        assertNull(session.getAttribute(AbstractCasFilter.CONST_ASSERTION));
    }

    public void testValidationSuccess() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockHttpSession session = new MockHttpSession();
        request.setSession(session);
        request.setParameter(AbstractCasFilter.PARAM_TICKET, "true");
        final FilterChain filterChain = new FilterChain() {

            public void doFilter(final ServletRequest arg0,
                                 final ServletResponse arg1) throws IOException,
                    ServletException {
                // nothing to do
            }
        };

        this.filter.doFilter(request, response, filterChain);

        assertNotNull(session.getAttribute(AbstractCasFilter.CONST_ASSERTION));
        assertNull(response.getRedirectedUrl());
    }

    public void testValidationFailure() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockHttpSession session = new MockHttpSession();
        request.setSession(session);
        request.setParameter(AbstractCasFilter.PARAM_TICKET, "false");
        final FilterChain filterChain = new FilterChain() {

            public void doFilter(final ServletRequest arg0,
                                 final ServletResponse arg1) throws IOException,
                    ServletException {
                // nothing to do
            }
        };

        try {
            this.filter.doFilter(request, response, filterChain);
            fail("Exception expected.");
        } catch (final ServletException e) {
            assertTrue(e.getRootCause().getClass().isAssignableFrom(
                    ValidationException.class));
            // expected
        }
    }
}
