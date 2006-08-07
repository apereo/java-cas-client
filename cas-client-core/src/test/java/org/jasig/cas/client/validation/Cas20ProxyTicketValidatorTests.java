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
import java.util.ArrayList;
import java.util.List;

/**
 * Test cases for the {@link Cas20ProxyTicketValidator}.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class Cas20ProxyTicketValidatorTests extends
    AbstractTicketValidatorTests {

    private Cas20ProxyTicketValidator ticketValidator;

    public Cas20ProxyTicketValidatorTests() {
        super();
    }

    protected void setUp() throws Exception {
        ProxyGrantingTicketStorage proxyGrantingTicketStorage = getProxyGrantingTicketStorage();
        this.ticketValidator = new Cas20ProxyTicketValidator();
        this.ticketValidator.setCasServerUrl(CONST_CAS_SERVER_URL);
        this.ticketValidator.setRenew(true);
        this.ticketValidator
            .setProxyGrantingTicketStorage(proxyGrantingTicketStorage);
        this.ticketValidator.setHttpClient(new HttpClient());

        final List list = new ArrayList();
        list.add("proxy1 proxy2 proxy3");
        this.ticketValidator.setProxyChains(list);

        this.ticketValidator.init();
    }

    private ProxyGrantingTicketStorage getProxyGrantingTicketStorage()
        throws Exception {
        ProxyGrantingTicketStorageImpl proxyGrantingTicketStorageImpl = new ProxyGrantingTicketStorageImpl();
        proxyGrantingTicketStorageImpl.init();

        return proxyGrantingTicketStorageImpl;
    }

    public void testProxyChainWithValidProxy() throws ValidationException,
        UnsupportedEncodingException {
        final String USERNAME = "username";
        final String RESPONSE = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'><cas:authenticationSuccess><cas:user>username</cas:user><cas:proxyGrantingTicket>PGTIOU-84678-8a9d...</cas:proxyGrantingTicket><cas:proxies><cas:proxy>proxy1</cas:proxy><cas:proxy>proxy2</cas:proxy><cas:proxy>proxy3</cas:proxy></cas:proxies></cas:authenticationSuccess></cas:serviceResponse>";
        PublicTestHttpServer.instance().content = RESPONSE
            .getBytes(PublicTestHttpServer.instance().encoding);

        final Assertion assertion = this.ticketValidator.validate("test",
            new SimpleService("test"));
        assertEquals(USERNAME, assertion.getPrincipal().getId());
    }

    public void testProxyChainWithInvalidProxy() throws ValidationException,
        UnsupportedEncodingException {
        final String RESPONSE = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'><cas:authenticationSuccess><cas:user>username</cas:user><cas:proxyGrantingTicket>PGTIOU-84678-8a9d...</cas:proxyGrantingTicket><cas:proxies><cas:proxy>proxy7</cas:proxy><cas:proxy>proxy2</cas:proxy><cas:proxy>proxy3</cas:proxy></cas:proxies></cas:authenticationSuccess></cas:serviceResponse>";
        PublicTestHttpServer.instance().content = RESPONSE
            .getBytes(PublicTestHttpServer.instance().encoding);

        try {
            this.ticketValidator.validate("test", new SimpleService("test"));
            fail("Invalid proxy chain");
        } catch (InvalidProxyChainValidationException e) {
            // expected
        }
    }
}
