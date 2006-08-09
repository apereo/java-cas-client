/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.client.validation;


import org.apache.commons.httpclient.HttpClient;
import org.jasig.cas.authentication.principal.SimpleService;
import org.jasig.cas.client.PublicTestHttpServer;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorage;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorageImpl;

import java.io.UnsupportedEncodingException;

/**
 * Test cases for the {@link Cas20ServiceTicketValidator}.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class Cas20ServiceTicketValidatorTests extends
        AbstractTicketValidatorTests {

    private Cas20ServiceTicketValidator ticketValidator;

    private ProxyGrantingTicketStorage proxyGrantingTicketStorage;

    public Cas20ServiceTicketValidatorTests() {
        super();
    }

    protected void setUp() throws Exception {
        this.proxyGrantingTicketStorage = getProxyGrantingTicketStorage();
        this.ticketValidator = new Cas20ServiceTicketValidator(CONST_CAS_SERVER_URL, true, new HttpClient(), this.proxyGrantingTicketStorage);
    }

    private ProxyGrantingTicketStorage getProxyGrantingTicketStorage() {
        ProxyGrantingTicketStorageImpl proxyGrantingTicketStorageImpl = new ProxyGrantingTicketStorageImpl();

        return proxyGrantingTicketStorageImpl;
    }

    public void testNoResponse() throws UnsupportedEncodingException {
        final String RESPONSE = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'><cas:authenticationFailure code=\"INVALID_TICKET\">Ticket ST-1856339-aA5Yuvrxzpv8Tau1cYQ7 not recognized</cas:authenticationFailure></cas:serviceResponse>";
        PublicTestHttpServer.instance().content = RESPONSE
                .getBytes(PublicTestHttpServer.instance().encoding);
        try {
            this.ticketValidator.validate("test", new SimpleService("test"));
            fail("ValidationException expected due to 'no' response");
        } catch (final ValidationException e) {
            // expected
        }
    }

    public void testYesResponseButNoPgt() throws ValidationException,
            UnsupportedEncodingException {
        final String USERNAME = "username";
        final String RESPONSE = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'><cas:authenticationSuccess><cas:user>"
                + USERNAME
                + "</cas:user></cas:authenticationSuccess></cas:serviceResponse>";
        PublicTestHttpServer.instance().content = RESPONSE
                .getBytes(PublicTestHttpServer.instance().encoding);

        final Assertion assertion = this.ticketValidator.validate("test",
                new SimpleService("test"));
        assertEquals(USERNAME, assertion.getPrincipal().getId());
    }

    public void testYesResponseWithPgt() throws ValidationException,
            UnsupportedEncodingException {
        final String USERNAME = "username";
        final String PGTIOU = "testPgtIou";
        final String PGT = "test";
        final String RESPONSE = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'><cas:authenticationSuccess><cas:user>"
                + USERNAME
                + "</cas:user><cas:proxyGrantingTicket>"
                + PGTIOU
                + "</cas:proxyGrantingTicket></cas:authenticationSuccess></cas:serviceResponse>";

        PublicTestHttpServer.instance().content = RESPONSE
                .getBytes(PublicTestHttpServer.instance().encoding);

        this.proxyGrantingTicketStorage.save(PGTIOU, PGT);

        final Assertion assertion = this.ticketValidator.validate("test",
                new SimpleService("test"));
        assertEquals(USERNAME, assertion.getPrincipal().getId());
        assertEquals(PGT, assertion.getProxyGrantingTicketId());
    }

    public void testInvalidResponse() throws Exception {
        final String RESPONSE = "<root />";
        PublicTestHttpServer.instance().content = RESPONSE
                .getBytes(PublicTestHttpServer.instance().encoding);
        try {
            this.ticketValidator.validate("test", new SimpleService("test"));
            fail("ValidationException expected due to invalid response.");
        } catch (final ValidationException e) {
            // expected
        }
    }
}
