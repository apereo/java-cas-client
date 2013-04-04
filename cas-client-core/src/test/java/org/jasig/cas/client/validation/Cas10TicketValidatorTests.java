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
import org.jasig.cas.client.PublicTestHttpServer;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.*;

/**
 * Test cases for the {@link Cas10TicketValidator}.
 *
 * @author Scott Battaglia
 * @version $Revision: 11731 $ $Date: 2007-09-27 11:27:21 -0400 (Wed, 27 Sep 2007) $
 * @since 3.0
 */
public final class Cas10TicketValidatorTests extends AbstractTicketValidatorTests {

    private static final PublicTestHttpServer server = PublicTestHttpServer.instance(8090);

    private Cas10TicketValidator ticketValidator;

    public Cas10TicketValidatorTests() {
        super();
    }

    /*@AfterClass
    public static void classCleanUp() {
        server.shutdown();
    } */

    @Before
    public void setUp() throws Exception {
        this.ticketValidator = new Cas10TicketValidator(CONST_CAS_SERVER_URL_PREFIX + "8090");
    }

    @Test
    public void testNoResponse() throws Exception {
        server.content = "no\n\n".getBytes(server.encoding);
        try {
            this.ticketValidator.validate("testTicket",
                    "myService");
            fail("ValidationException expected.");
        } catch (final TicketValidationException e) {
            // expected
        }
    }

    @Test
    public void testYesResponse() throws TicketValidationException,
            UnsupportedEncodingException {
        server.content = "yes\nusername\n\n".getBytes(server.encoding);
        final Assertion assertion = this.ticketValidator.validate("testTicket",
                "myService");
        assertEquals(CONST_USERNAME, assertion.getPrincipal().getName());
    }

    @Test
    public void testBadResponse() throws UnsupportedEncodingException {
        server.content = "falalala\n\n"
                .getBytes(server.encoding);
        try {
            this.ticketValidator.validate("testTicket",
                    "myService");
            fail("ValidationException expected.");
        } catch (final TicketValidationException e) {
            // expected
        }
    }
}
