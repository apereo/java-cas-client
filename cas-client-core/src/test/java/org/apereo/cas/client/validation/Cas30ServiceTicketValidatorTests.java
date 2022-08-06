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
package org.apereo.cas.client.validation;

import org.apereo.cas.client.PublicTestHttpServer;
import org.apereo.cas.client.proxy.ProxyGrantingTicketStorage;
import org.apereo.cas.client.proxy.ProxyGrantingTicketStorageImpl;
import org.apereo.cas.client.proxy.ProxyRetriever;

import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test cases for the {@link Cas20ServiceTicketValidator}.
 *
 * @author Scott Battaglia
 * @since 3.0
 */
public final class Cas30ServiceTicketValidatorTests extends AbstractTicketValidatorTests {

    private static final PublicTestHttpServer server = PublicTestHttpServer.instance(8088);

    private Cas30ServiceTicketValidator ticketValidator;

    private ProxyGrantingTicketStorage proxyGrantingTicketStorage;

    public Cas30ServiceTicketValidatorTests() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        this.proxyGrantingTicketStorage = getProxyGrantingTicketStorage();
        this.ticketValidator = new Cas30ServiceTicketValidator(CONST_CAS_SERVER_URL_PREFIX + "8088");
        this.ticketValidator.setProxyCallbackUrl("test");
        this.ticketValidator.setProxyGrantingTicketStorage(getProxyGrantingTicketStorage());
        this.ticketValidator.setProxyRetriever(getProxyRetriever());
        this.ticketValidator.setRenew(true);
    }

    @Test
    public void testNoResponse() throws UnsupportedEncodingException {
        final String RESPONSE =
            "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'><cas:authenticationFailure code=\"INVALID_TICKET\">Ticket ST-1856339-aA5Yuvrxzpv8Tau1cYQ7 not recognized</cas:authenticationFailure></cas:serviceResponse>";
        server.content = RESPONSE.getBytes(server.encoding);
        try {
            this.ticketValidator.validate("test", "test");
            fail("ValidationException expected due to 'no' response");
        } catch (final TicketValidationException e) {
            // expected
        }
    }

    @Test
    public void testYesResponseButNoPgt() throws TicketValidationException, UnsupportedEncodingException {
        final String USERNAME = "username";
        final String RESPONSE = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'><cas:authenticationSuccess><cas:user>"
                                + USERNAME + "</cas:user></cas:authenticationSuccess></cas:serviceResponse>";
        server.content = RESPONSE.getBytes(server.encoding);

        final Assertion assertion = this.ticketValidator.validate("test", "test");
        assertEquals(USERNAME, assertion.getPrincipal().getName());

    }

    @Test
    public void testYesResponseWithPgt() throws TicketValidationException, UnsupportedEncodingException {
        final String USERNAME = "username";
        final String PGTIOU = "testPgtIou";
        final String PGT = "test";
        final String RESPONSE = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'><cas:authenticationSuccess><cas:user>"
                                + USERNAME
                                + "</cas:user><cas:proxyGrantingTicket>"
                                + PGTIOU
                                + "</cas:proxyGrantingTicket></cas:authenticationSuccess></cas:serviceResponse>";

        server.content = RESPONSE.getBytes(server.encoding);
        this.proxyGrantingTicketStorage.save(PGTIOU, PGT);

        final Assertion assertion = this.ticketValidator.validate("test", "test");
        assertEquals(USERNAME, assertion.getPrincipal().getName());
        //        assertEquals(PGT, assertion.getProxyGrantingTicketId());
    }

    @Test
    public void testGetAttributes() throws TicketValidationException, UnsupportedEncodingException {
        final String USERNAME = "username";
        final String PGTIOU = "testPgtIou";
        final String RESPONSE = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'><cas:authenticationSuccess><cas:user>"
                                + USERNAME
                                + "</cas:user><cas:proxyGrantingTicket>"
                                + PGTIOU
                                + "</cas:proxyGrantingTicket><cas:attributes><cas:password>test</cas:password><cas:eduPersonId>id</cas:eduPersonId><cas:longAttribute>test1\n\ntest</cas:longAttribute><cas:multivaluedAttribute>value1</cas:multivaluedAttribute><cas:multivaluedAttribute>value2</cas:multivaluedAttribute></cas:attributes></cas:authenticationSuccess></cas:serviceResponse>";

        server.content = RESPONSE.getBytes(server.encoding);
        final Assertion assertion = this.ticketValidator.validate("test", "test");
        assertEquals(USERNAME, assertion.getPrincipal().getName());
        assertEquals("test", assertion.getPrincipal().getAttributes().get("password"));
        assertEquals("id", assertion.getPrincipal().getAttributes().get("eduPersonId"));
        assertEquals("test1\n\ntest", assertion.getPrincipal().getAttributes().get("longAttribute"));
        try {
            final List<?> multivalued = (List<?>) assertion.getPrincipal().getAttributes().get("multivaluedAttribute");
            assertArrayEquals(new String[]{"value1", "value2"}, multivalued.toArray());
        } catch (final Exception e) {
            fail("'multivaluedAttribute' attribute expected as List<Object> object.");
        }
        //assertEquals(PGT, assertion.getProxyGrantingTicketId());
    }

    @Test
    public void testGetInlinedAttributes() throws TicketValidationException, UnsupportedEncodingException {
        final String USERNAME = "username";
        final String PGTIOU = "testPgtIou";
        final String RESPONSE = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'><cas:authenticationSuccess><cas:user>"
                                + USERNAME
                                + "</cas:user><cas:proxyGrantingTicket>"
                                + PGTIOU
                                + "</cas:proxyGrantingTicket><cas:attributes><cas:attribute name=\"password\" value=\"test\"/><cas:attribute name=\"eduPersonId\" value=\"id\"/><cas:attribute name=\"longAttribute\" value=\"test1&#10;&#10;test\"/><cas:attribute name=\"multivaluedAttribute\" value=\"value1\"/><cas:attribute name=\"multivaluedAttribute\" value=\"value2\"/></cas:attributes></cas:authenticationSuccess></cas:serviceResponse>";

        server.content = RESPONSE.getBytes(server.encoding);
        final Assertion assertion = this.ticketValidator.validate("test", "test");
        assertEquals(USERNAME, assertion.getPrincipal().getName());
        assertEquals("test", assertion.getPrincipal().getAttributes().get("password"));
        assertEquals("id", assertion.getPrincipal().getAttributes().get("eduPersonId"));
        assertEquals("test1\n\ntest", assertion.getPrincipal().getAttributes().get("longAttribute"));
        try {
            final List<?> multivalued = (List<?>) assertion.getPrincipal().getAttributes().get("multivaluedAttribute");
            assertArrayEquals(new String[]{"value1", "value2"}, multivalued.toArray());
        } catch (final Exception e) {
            fail("'multivaluedAttribute' attribute expected as List<Object> object.");
        }
        //assertEquals(PGT, assertion.getProxyGrantingTicketId());
    }

    @Test
    public void testInvalidResponse() throws Exception {
        final String RESPONSE = "<root />";
        server.content = RESPONSE.getBytes(server.encoding);
        try {
            this.ticketValidator.validate("test", "test");
            fail("ValidationException expected due to invalid response.");
        } catch (final TicketValidationException e) {
            // expected
        }
    }

    private ProxyGrantingTicketStorage getProxyGrantingTicketStorage() {
        return new ProxyGrantingTicketStorageImpl();
    }

    private ProxyRetriever getProxyRetriever() {
        return new ProxyRetriever() {

            /** Unique Id for serialization. */
            private static final long serialVersionUID = 1L;

            @Override
            public String getProxyTicketIdFor(final String proxyGrantingTicketId, final String targetService) {
                return "test";
            }
        };
    }
}
