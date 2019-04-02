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

import static org.junit.Assert.*;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.List;
import org.jasig.cas.client.PublicTestHttpServer;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorage;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorageImpl;
import org.jasig.cas.client.proxy.ProxyRetriever;
import org.junit.Before;
import org.junit.Test;

/**
 * Test cases for the {@link Cas20ServiceTicketValidator}.
 *
 * @author Scott Battaglia
 * @since 3.0
 */
public final class Cas20ServiceTicketValidatorTests extends AbstractTicketValidatorTests {

    private static final PublicTestHttpServer server = PublicTestHttpServer.instance(8088);
    private static final String USERNAME = "username";
    private static final String PGTIOU = "PGTIOU-1-test";
    private static final String PGT = "PGT-1-ixcY6jtRXZ4OrJ39SadtLEcTLsGNhE8-NYtvDTK3kk5iAEdatRcnGrGjLckOwK8xU6ocastest";
    private static final String ENCRYPTED_PGT = "H3wqFQLBlvhbrPVo4yrwIF9p8yJhCfzHnLHgTWTYVw42sLDJj7c3PBFHKgZfaY9l57qDbKA0fZY979GGFFgnSz1VOOlTgVRi/nmbpwlScRLHP8qUf2JGUyhu0+nTRp6TcQiEqpf5iquXNyQ9UXPyWPdTM/YtgtYtcIOzMovjN5c=";

    private Cas20ServiceTicketValidator ticketValidator;

    private ProxyGrantingTicketStorage proxyGrantingTicketStorage;

    private Field proxyGrantingTicketField;

    public Cas20ServiceTicketValidatorTests() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        this.proxyGrantingTicketStorage = new ProxyGrantingTicketStorageImpl();
        this.proxyGrantingTicketStorage.save(PGTIOU, PGT);
        this.ticketValidator = new Cas20ServiceTicketValidator(CONST_CAS_SERVER_URL_PREFIX + "8088");
        this.ticketValidator.setProxyCallbackUrl("test");
        this.ticketValidator.setProxyGrantingTicketStorage(this.proxyGrantingTicketStorage);
        this.ticketValidator.setProxyRetriever(getProxyRetriever());
        this.ticketValidator.setPrivateKey(Cas20ProxyReceivingTicketValidationFilter.buildPrivateKey("src/test/resources/private.pem", "RSA"));
        this.ticketValidator.setRenew(true);
        proxyGrantingTicketField = AttributePrincipalImpl.class.getDeclaredField("proxyGrantingTicket");
        proxyGrantingTicketField.setAccessible(true);
    }

    private ProxyRetriever getProxyRetriever() {
        return new ProxyRetriever() {

            /** Unique Id for serialization. */
            private static final long serialVersionUID = 1L;

            public String getProxyTicketIdFor(String proxyGrantingTicketId, String targetService) {
                return "test";
            }
        };
    }

    @Test
    public void testNoResponse() throws UnsupportedEncodingException {
        final String RESPONSE = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'><cas:authenticationFailure code=\"INVALID_TICKET\">Ticket ST-1856339-aA5Yuvrxzpv8Tau1cYQ7 not recognized</cas:authenticationFailure></cas:serviceResponse>";
        server.content = RESPONSE.getBytes(server.encoding);
        try {
            this.ticketValidator.validate("test", "test");
            fail("ValidationException expected due to 'no' response");
        } catch (final TicketValidationException e) {
            // expected
        }
    }

    @Test
    public void testYesResponseButNoPgtiou() throws TicketValidationException, UnsupportedEncodingException {
        final String RESPONSE = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'><cas:authenticationSuccess><cas:user>"
                + USERNAME + "</cas:user></cas:authenticationSuccess></cas:serviceResponse>";
        server.content = RESPONSE.getBytes(server.encoding);

        final Assertion assertion = this.ticketValidator.validate("test", "test");
        assertEquals(USERNAME, assertion.getPrincipal().getName());

    }

    @Test
    public void testYesResponseWithPgtiou() throws TicketValidationException, UnsupportedEncodingException, IllegalAccessException {
        final String RESPONSE = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'><cas:authenticationSuccess><cas:user>"
                + USERNAME
                + "</cas:user><cas:proxyGrantingTicket>"
                + PGTIOU
                + "</cas:proxyGrantingTicket></cas:authenticationSuccess></cas:serviceResponse>";

        server.content = RESPONSE.getBytes(server.encoding);

        final Assertion assertion = this.ticketValidator.validate("test", "test");
        final AttributePrincipalImpl principal = (AttributePrincipalImpl) assertion.getPrincipal();
        assertEquals(USERNAME, principal.getName());
        assertEquals(PGT, proxyGrantingTicketField.get(principal));
    }

    @Test
    public void testGetAttributes() throws TicketValidationException, UnsupportedEncodingException, IllegalAccessException {
        final String RESPONSE = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'><cas:authenticationSuccess><cas:user>"
                + USERNAME
                + "</cas:user><cas:proxyGrantingTicket>"
                + PGTIOU
                + "</cas:proxyGrantingTicket><cas:attributes><cas:password>test</cas:password><cas:eduPersonId>id</cas:eduPersonId><cas:longAttribute>test1\n\ntest</cas:longAttribute><cas:multivaluedAttribute>value1</cas:multivaluedAttribute><cas:multivaluedAttribute>value2</cas:multivaluedAttribute></cas:attributes></cas:authenticationSuccess></cas:serviceResponse>";

        server.content = RESPONSE.getBytes(server.encoding);
        final Assertion assertion = this.ticketValidator.validate("test", "test");
        final AttributePrincipalImpl principal = (AttributePrincipalImpl) assertion.getPrincipal();
        assertEquals(USERNAME, principal.getName());
        assertEquals("test", principal.getAttributes().get("password"));
        assertEquals("id", principal.getAttributes().get("eduPersonId"));
        assertEquals("test1\n\ntest", principal.getAttributes().get("longAttribute"));
        try {
            List<?> multivalued = (List<?>) principal.getAttributes().get("multivaluedAttribute");
            assertArrayEquals(new String[] { "value1", "value2" }, multivalued.toArray());
        } catch (Exception e) {
            fail("'multivaluedAttribute' attribute expected as List<Object> object.");
        }
        assertEquals(PGT, proxyGrantingTicketField.get(principal));
    }

    @Test
    public void testYesResponseWithEncryptedPgt() throws TicketValidationException, UnsupportedEncodingException, IllegalAccessException {
        final String RESPONSE = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'><cas:authenticationSuccess><cas:user>"
                + USERNAME
                + "</cas:user><cas:attributes><cas:proxyGrantingTicket>"
                + ENCRYPTED_PGT
                + "</cas:proxyGrantingTicket></cas:attributes></cas:authenticationSuccess></cas:serviceResponse>";

        server.content = RESPONSE.getBytes(server.encoding);

        final Assertion assertion = this.ticketValidator.validate("test", "test");
        final AttributePrincipalImpl principal = (AttributePrincipalImpl) assertion.getPrincipal();
        assertEquals(USERNAME, principal.getName());
        assertEquals(PGT, proxyGrantingTicketField.get(principal));
    }

    @Test
    public void testYesResponseWithPgtiouAndEncryptedPgt() throws TicketValidationException, UnsupportedEncodingException, IllegalAccessException {
        final String RESPONSE = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'><cas:authenticationSuccess><cas:user>"
                + USERNAME
                + "</cas:user><cas:proxyGrantingTicket>"
                + PGTIOU
                + "</cas:proxyGrantingTicket><cas:attributes><cas:proxyGrantingTicket>"
                + ENCRYPTED_PGT
                + "</cas:proxyGrantingTicket></cas:attributes></cas:authenticationSuccess></cas:serviceResponse>";

        server.content = RESPONSE.getBytes(server.encoding);

        final Assertion assertion = this.ticketValidator.validate("test", "test");
        final AttributePrincipalImpl principal = (AttributePrincipalImpl) assertion.getPrincipal();
        assertEquals(USERNAME, principal.getName());
        assertEquals(PGT, proxyGrantingTicketField.get(principal));
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
}
