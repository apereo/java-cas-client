/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.client.validation;


import org.apache.commons.httpclient.HttpClient;
import org.jasig.cas.authentication.principal.SimpleService;
import org.jasig.cas.client.PublicTestHttpServer;

import java.io.UnsupportedEncodingException;

/**
 * Test cases for the {@link Cas10TicketValidator}.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class Cas10TicketValidatorTests extends AbstractTicketValidatorTests {

    private Cas10TicketValidator ticketValidator;

    public Cas10TicketValidatorTests() {
        super();
    }

    protected void setUp() throws Exception {
        this.ticketValidator = new Cas10TicketValidator(CONST_CAS_SERVER_URL, true, new HttpClient());
    }

    public void testNoResponse() throws Exception {
        PublicTestHttpServer.instance().content = "no\n\n"
                .getBytes(PublicTestHttpServer.instance().encoding);
        try {
            this.ticketValidator.validate("testTicket", new SimpleService(
                    "myService"));
            fail("ValidationException expected.");
        } catch (final ValidationException e) {
            // expected
        }
    }

    public void testYesResponse() throws ValidationException,
            UnsupportedEncodingException {
        PublicTestHttpServer.instance().content = "yes\nusername\n\n"
                .getBytes(PublicTestHttpServer.instance().encoding);
        final Assertion assertion = this.ticketValidator.validate("testTicket",
                new SimpleService("myService"));
        assertEquals(CONST_USERNAME, assertion.getPrincipal().getId());
    }

    public void testBadResponse() throws UnsupportedEncodingException {
        PublicTestHttpServer.instance().content = "falalala\n\n"
                .getBytes(PublicTestHttpServer.instance().encoding);
        try {
            this.ticketValidator.validate("testTicket", new SimpleService(
                    "myService"));
            fail("ValidationException expected.");
        } catch (final ValidationException e) {
            // expected
        }
    }
}
