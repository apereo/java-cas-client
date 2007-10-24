/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.validation;

import org.jasig.cas.client.PublicTestHttpServer;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorage;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorageImpl;
import org.jasig.cas.client.proxy.ProxyRetriever;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Test cases for the {@link Cas20ProxyTicketValidator}.
 *
 * @author Scott Battaglia
 * @version $Revision: 11737 $ $Date: 2006-10-03 09:14:02 -0400 (Tue, 03 Oct 2006) $
 * @since 3.0
 */
public final class Cas20ProxyTicketValidatorTests extends
        AbstractTicketValidatorTests {

    private Cas20ProxyTicketValidator ticketValidator;

    public Cas20ProxyTicketValidatorTests() {
        super();
    }

    protected void setUp() throws Exception {
        final ProxyGrantingTicketStorage proxyGrantingTicketStorage = getProxyGrantingTicketStorage();
        final List list = new ArrayList();
        list.add(new String[] {"proxy1", "proxy2", "proxy3"});

        this.ticketValidator = new Cas20ProxyTicketValidator(CONST_CAS_SERVER_URL);
        this.ticketValidator.setRenew(true);
        this.ticketValidator.setProxyCallbackUrl("test");
        this.ticketValidator.setProxyGrantingTicketStorage(getProxyGrantingTicketStorage());
        this.ticketValidator.setProxyRetriever(getProxyRetriever());
        this.ticketValidator.setAllowedProxyChains(list);
    }

    private ProxyGrantingTicketStorage getProxyGrantingTicketStorage() {
        final ProxyGrantingTicketStorageImpl proxyGrantingTicketStorageImpl = new ProxyGrantingTicketStorageImpl();

        return proxyGrantingTicketStorageImpl;
    }

    private ProxyRetriever getProxyRetriever() {
        final ProxyRetriever proxyRetriever = new ProxyRetriever() {

            public String getProxyTicketIdFor(String proxyGrantingTicketId, String targetService) {
                return "test";
            }
        };

        return proxyRetriever;
    }

    public void testProxyChainWithValidProxy() throws TicketValidationException,
            UnsupportedEncodingException {
        final String USERNAME = "username";
        final String RESPONSE = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'><cas:authenticationSuccess><cas:user>username</cas:user><cas:proxyGrantingTicket>PGTIOU-84678-8a9d...</cas:proxyGrantingTicket><cas:proxies><cas:proxy>proxy1</cas:proxy><cas:proxy>proxy2</cas:proxy><cas:proxy>proxy3</cas:proxy></cas:proxies></cas:authenticationSuccess></cas:serviceResponse>";
        PublicTestHttpServer.instance().content = RESPONSE
                .getBytes(PublicTestHttpServer.instance().encoding);

        final Assertion assertion = this.ticketValidator.validate("test",
                "test");
        assertEquals(USERNAME, assertion.getPrincipal().getName());
    }

    public void testProxyChainWithInvalidProxy() throws TicketValidationException,
            UnsupportedEncodingException {
        final String RESPONSE = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'><cas:authenticationSuccess><cas:user>username</cas:user><cas:proxyGrantingTicket>PGTIOU-84678-8a9d...</cas:proxyGrantingTicket><cas:proxies><cas:proxy>proxy7</cas:proxy><cas:proxy>proxy2</cas:proxy><cas:proxy>proxy3</cas:proxy></cas:proxies></cas:authenticationSuccess></cas:serviceResponse>";
        PublicTestHttpServer.instance().content = RESPONSE
                .getBytes(PublicTestHttpServer.instance().encoding);

        try {
            this.ticketValidator.validate("test", "test");
            fail("Invalid proxy chain");
        } catch (InvalidProxyChainTicketValidationException e) {
            // expected
        }
    }
}
