/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.cas.client.validation;

import org.jasig.cas.client.PublicTestHttpServer;
import org.jasig.cas.client.util.CommonUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1.3
 */
public final class Saml11TicketValidatorTests extends AbstractTicketValidatorTests {

    private Saml11TicketValidator validator;

    @Before
    public void setUp() throws Exception {
        this.validator = new Saml11TicketValidator(CONST_CAS_SERVER_URL);
        this.validator.setTolerance(1000L);
    }

    @AfterClass
    public static void classCleanUp() {
        PublicTestHttpServer.instance().shutdown();
    }

    @Test
    public void testValidationFailedResponse() throws UnsupportedEncodingException {
        final String RESPONSE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><SOAP-ENV:Envelope\n" +
                " xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"><SOAP-ENV:Header/><SOAP-ENV:Body><Response\n" +
                " xmlns=\"urn:oasis:names:tc:SAML:1.0:protocol\"\n" +
                " xmlns:saml=\"urn:oasis:names:tc:SAML:1.0:assertion\"\n" +
                " xmlns:samlp=\"urn:oasis:names:tc:SAML:1.0:protocol\"\n" +
                " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                " IssueInstant=\"2008-06-03T04:44:57.143Z\" MajorVersion=\"1\" MinorVersion=\"1\"\n" +
                " Recipient=\"http://localhost:8084/WebApplication1/\"\n" +
                " ResponseID=\"_3b62bece2e8da1c10279db04882012ac\"><Status><StatusCode\n" +
                " Value=\"samlp:Responder\"></StatusCode><StatusMessage>Success</StatusMessage></Status></Response></SOAP-ENV:Body></SOAP-ENV:Envelope>";

        PublicTestHttpServer.instance().content = RESPONSE.getBytes(PublicTestHttpServer.instance().encoding);
        try {
            this.validator.validate("test", "test");
            fail("ValidationException expected due to 'no' response");
        } catch (final TicketValidationException e) {
            // expected
        }
    }

    @Test
    public void testValidationSuccessWithNoAttributes() throws UnsupportedEncodingException {
        final Date now = new Date();
        final Date before = new Date(now.getTime() - 5000);
        final Date after = new Date(now.getTime() + 200000000);
        final String RESPONSE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"><SOAP-ENV:Header/><SOAP-ENV:Body><Response xmlns=\"urn:oasis:names:tc:SAML:1.0:protocol\" xmlns:saml=\"urn:oasis:names:tc:SAML:1.0:assertion\" xmlns:samlp=\"urn:oasis:names:tc:SAML:1.0:protocol\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" IssueInstant=\"" + CommonUtils.formatForUtcTime(now) + "\" MajorVersion=\"1\" MinorVersion=\"1\" Recipient=\"test\" ResponseID=\"_e1e2124c08ab456eab0bbab3e1c0c433\"><Status><StatusCode Value=\"samlp:Success\"></StatusCode></Status><Assertion xmlns=\"urn:oasis:names:tc:SAML:1.0:assertion\" AssertionID=\"_d2fd0d6e4da6a6d7d2ba5274ab570d5c\" IssueInstant=\"" + CommonUtils.formatForUtcTime(now) + "\" Issuer=\"testIssuer\" MajorVersion=\"1\" MinorVersion=\"1\"><Conditions NotBefore=\"" + CommonUtils.formatForUtcTime(before) + "\" NotOnOrAfter=\"" + CommonUtils.formatForUtcTime(after) + "\"><AudienceRestrictionCondition><Audience>test</Audience></AudienceRestrictionCondition></Conditions><AuthenticationStatement AuthenticationInstant=\"2008-06-19T14:34:44.426Z\" AuthenticationMethod=\"urn:ietf:rfc:2246\"><Subject><NameIdentifier>testPrincipal</NameIdentifier><SubjectConfirmation><ConfirmationMethod>urn:oasis:names:tc:SAML:1.0:cm:artifact</ConfirmationMethod></SubjectConfirmation></Subject></AuthenticationStatement></Assertion></Response></SOAP-ENV:Body></SOAP-ENV:Envelope>";
        PublicTestHttpServer.instance().content = RESPONSE.getBytes(PublicTestHttpServer.instance().encoding);
		try {
		    final Assertion a = this.validator.validate("test", "test");
		    assertEquals("testPrincipal", a.getPrincipal().getName());
		} catch (final TicketValidationException e) {
		    fail(e.toString());
		}
    }
}
