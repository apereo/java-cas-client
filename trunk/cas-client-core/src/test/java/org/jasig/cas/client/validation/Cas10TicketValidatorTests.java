/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.validation;


import org.jasig.cas.client.PublicTestHttpServer;

import java.io.UnsupportedEncodingException;

/**
 * Test cases for the {@link Cas10TicketValidator}.
 *
 * @author Scott Battaglia
 * @version $Revision: 11731 $ $Date: 2007-09-27 11:27:21 -0400 (Wed, 27 Sep 2007) $
 * @since 3.0
 */
public final class Cas10TicketValidatorTests extends AbstractTicketValidatorTests {

    private Cas10TicketValidator ticketValidator;

    public Cas10TicketValidatorTests() {
        super();
    }

    protected void setUp() throws Exception {
        this.ticketValidator = new Cas10TicketValidator(CONST_CAS_SERVER_URL);
    }

    public void testNoResponse() throws Exception {
        PublicTestHttpServer.instance().content = "no\n\n"
                .getBytes(PublicTestHttpServer.instance().encoding);
        try {
            this.ticketValidator.validate("testTicket",
                    "myService");
            fail("ValidationException expected.");
        } catch (final TicketValidationException e) {
            // expected
        }
    }

    public void testYesResponse() throws TicketValidationException,
            UnsupportedEncodingException {
        PublicTestHttpServer.instance().content = "yes\nusername\n\n"
                .getBytes(PublicTestHttpServer.instance().encoding);
        final Assertion assertion = this.ticketValidator.validate("testTicket",
                "myService");
        assertEquals(CONST_USERNAME, assertion.getPrincipal().getName());
    }

    public void testBadResponse() throws UnsupportedEncodingException {
        PublicTestHttpServer.instance().content = "falalala\n\n"
                .getBytes(PublicTestHttpServer.instance().encoding);
        try {
            this.ticketValidator.validate("testTicket",
                    "myService");
            fail("ValidationException expected.");
        } catch (final TicketValidationException e) {
            // expected
        }
    }
}
