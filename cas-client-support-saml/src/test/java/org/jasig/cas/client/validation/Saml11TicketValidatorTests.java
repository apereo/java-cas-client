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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Date;
import org.jasig.cas.client.PublicTestHttpServer;
import org.jasig.cas.client.util.SamlUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1.3
 */
@Ignore
public final class Saml11TicketValidatorTests extends AbstractTicketValidatorTests {

    private Saml11TicketValidator validator;

    private static final PublicTestHttpServer server = PublicTestHttpServer.instance(9051);

    @Before
    public void setUp() throws Exception {
        this.validator = new Saml11TicketValidator(AbstractTicketValidatorTests.CONST_CAS_SERVER_URL_PREFIX + "9051");
        this.validator.setTolerance(1000L);
    }

    /*@AfterClass
    public static void cleanUp() throws Exception {
        server.shutdown();
    }*/

    @Test
    public void testCompatibilityValidationFailedResponse() throws UnsupportedEncodingException {
        final String RESPONSE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><SOAP-ENV:Envelope\n"
                + " xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"><SOAP-ENV:Header/><SOAP-ENV:Body><Response\n"
                + " xmlns=\"urn:oasis:names:tc:SAML:1.0:protocol\"\n"
                + " xmlns:saml=\"urn:oasis:names:tc:SAML:1.0:assertion\"\n"
                + " xmlns:samlp=\"urn:oasis:names:tc:SAML:1.0:protocol\"\n"
                + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n"
                + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                + " IssueInstant=\"2008-06-03T04:44:57.143Z\" MajorVersion=\"1\" MinorVersion=\"1\"\n"
                + " Recipient=\"http://localhost:8084/WebApplication1/\"\n"
                + " ResponseID=\"_3b62bece2e8da1c10279db04882012ac\"><Status><StatusCode\n"
                + " Value=\"samlp:Responder\"></StatusCode><StatusMessage>Success</StatusMessage></Status></Response></SOAP-ENV:Body></SOAP-ENV:Envelope>";

        server.content = RESPONSE.getBytes(server.encoding);
        try {
            this.validator.validate("test", "test");
            fail("ValidationException expected due to 'no' response");
        } catch (final TicketValidationException e) {
            // expected
        }
    }

    @Test
    public void testCompatibilityValidationSuccessWithNoAttributes() throws UnsupportedEncodingException {
        final Interval range = currentTimeRangeInterval();
        final Date now = new Date();
        final String RESPONSE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"><SOAP-ENV:Header/><SOAP-ENV:Body><Response xmlns=\"urn:oasis:names:tc:SAML:1.0:protocol\" xmlns:saml=\"urn:oasis:names:tc:SAML:1.0:assertion\" xmlns:samlp=\"urn:oasis:names:tc:SAML:1.0:protocol\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" IssueInstant=\""
                + SamlUtils.formatForUtcTime(now)
                + "\" MajorVersion=\"1\" MinorVersion=\"1\" Recipient=\"test\" ResponseID=\"_e1e2124c08ab456eab0bbab3e1c0c433\"><Status><StatusCode Value=\"samlp:Success\"></StatusCode></Status><Assertion xmlns=\"urn:oasis:names:tc:SAML:1.0:assertion\" AssertionID=\"_d2fd0d6e4da6a6d7d2ba5274ab570d5c\" IssueInstant=\""
                + SamlUtils.formatForUtcTime(now)
                + "\" Issuer=\"testIssuer\" MajorVersion=\"1\" MinorVersion=\"1\"><Conditions NotBefore=\""
                + SamlUtils.formatForUtcTime(range.getStart().toDate())
                + "\" NotOnOrAfter=\""
                + SamlUtils.formatForUtcTime(range.getEnd().toDate())
                + "\"><AudienceRestrictionCondition><Audience>test</Audience></AudienceRestrictionCondition></Conditions><AuthenticationStatement AuthenticationInstant=\"2008-06-19T14:34:44.426Z\" AuthenticationMethod=\"urn:ietf:rfc:2246\"><Subject><NameIdentifier>testPrincipal</NameIdentifier><SubjectConfirmation><ConfirmationMethod>urn:oasis:names:tc:SAML:1.0:cm:artifact</ConfirmationMethod></SubjectConfirmation></Subject></AuthenticationStatement></Assertion></Response></SOAP-ENV:Body></SOAP-ENV:Envelope>";
        server.content = RESPONSE.getBytes(server.encoding);
        try {
            final Assertion a = this.validator.validate("test", "test");
            assertEquals("testPrincipal", a.getPrincipal().getName());
        } catch (final TicketValidationException e) {
            fail(e.toString());
        }
    }

    @Test
    public void openSaml2GeneratedResponse() throws UnsupportedEncodingException {
        final Interval range = currentTimeRangeInterval();
        final Date now = new Date();

        final String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><soap11:Envelope xmlns:soap11=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap11:Body>"
                + "<saml1p:Response xmlns:saml1p=\"urn:oasis:names:tc:SAML:1.0:protocol\" InResponseTo=\"_fd1632b5dfa921623e7ca6f9ab727161\" IssueInstant=\""
                + SamlUtils.formatForUtcTime(now)
                + "\" MajorVersion=\"1\" MinorVersion=\"1\" Recipient=\"https://example.com/test-client/secure/?TARGET=https%3A%2F%2Fexample.com%2Ftest-client%2Fsecure%2F\" ResponseID=\"_436dbb2cca5166af29250f431a07888f\">"
                + "<saml1p:Status><saml1p:StatusCode Value=\"saml1p:Success\"/></saml1p:Status>"
                + "<saml1:Assertion xmlns:saml1=\"urn:oasis:names:tc:SAML:1.0:assertion\" IssueInstant=\""
                + SamlUtils.formatForUtcTime(now)
                + "\" Issuer=\"localhost\" MajorVersion=\"1\" MinorVersion=\"1\">"
                + "<saml1:Conditions NotBefore=\""
                + SamlUtils.formatForUtcTime(range.getStart().toDate())
                + "\" NotOnOrAfter=\""
                + SamlUtils.formatForUtcTime(range.getEnd().toDate())
                + "\">"
                + "<saml1:AudienceRestrictionCondition><saml1:Audience>https://example.com/test-client/secure/</saml1:Audience>"
                + "</saml1:AudienceRestrictionCondition></saml1:Conditions>"
                + "<saml1:AuthenticationStatement AuthenticationInstant=\""
                + SamlUtils.formatForUtcTime(now)
                + "\" AuthenticationMethod=\"urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport\">"
                + "<saml1:Subject><saml1:NameIdentifier>testPrincipal</saml1:NameIdentifier>"
                + "<saml1:SubjectConfirmation><saml1:ConfirmationMethod>urn:oasis:names:tc:SAML:1.0:cm:artifact</saml1:ConfirmationMethod></saml1:SubjectConfirmation>"
                + "</saml1:Subject></saml1:AuthenticationStatement>"
                + "<saml1:AttributeStatement><saml1:Subject><saml1:NameIdentifier>testPrincipal</saml1:NameIdentifier>"
                + "<saml1:SubjectConfirmation><saml1:ConfirmationMethod>urn:oasis:names:tc:SAML:1.0:cm:artifact</saml1:ConfirmationMethod></saml1:SubjectConfirmation></saml1:Subject>"
                + "<saml1:Attribute AttributeName=\"uid\" AttributeNamespace=\"http://www.ja-sig.org/products/cas/\"><saml1:AttributeValue xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"xs:string\">12345</saml1:AttributeValue>"
                + "</saml1:Attribute><saml1:Attribute AttributeName=\"accountState\" AttributeNamespace=\"http://www.ja-sig.org/products/cas/\">"
                + "<saml1:AttributeValue xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"xs:string\">ACTIVE</saml1:AttributeValue>"
                + "</saml1:Attribute><saml1:Attribute AttributeName=\"eduPersonAffiliation\" AttributeNamespace=\"http://www.ja-sig.org/products/cas/\">"
                + "<saml1:AttributeValue xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"xs:string\">employee</saml1:AttributeValue>"
                + "<saml1:AttributeValue xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"xs:string\">staff</saml1:AttributeValue>"
                + "<saml1:AttributeValue xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"xs:string\">student</saml1:AttributeValue></saml1:Attribute></saml1:AttributeStatement></saml1:Assertion></saml1p:Response></soap11:Body></soap11:Envelope>";

        server.content = response.getBytes(server.encoding);
        try {
            final Assertion a = this.validator.validate("test", "test");
            assertEquals(
                    "urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport",
                    a.getAttributes().get(Saml11TicketValidator.AUTH_METHOD_ATTRIBUTE));
            assertEquals("testPrincipal", a.getPrincipal().getName());
            assertEquals("12345", a.getPrincipal().getAttributes().get("uid"));
            assertEquals("ACTIVE", a.getPrincipal().getAttributes().get("accountState"));
            assertEquals(3, ((Collection) a.getPrincipal().getAttributes().get("eduPersonAffiliation")).size());
        } catch (final TicketValidationException e) {
            fail(e.toString());
        }
    }

    private Interval currentTimeRangeInterval() {
        return new Interval(new DateTime(DateTimeZone.UTC).minus(5000), new DateTime(DateTimeZone.UTC).plus(200000000));
    }
}