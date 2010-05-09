package org.jasig.cas.client.validation;

import org.jasig.cas.client.PublicTestHttpServer;
import org.jasig.cas.client.util.CommonUtils;

import java.io.UnsupportedEncodingException;
import java.util.Date;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1.3
 */
public final class Saml11TicketValidatorTests extends AbstractTicketValidatorTests {

    private Saml11TicketValidator validator;

    protected void setUp() throws Exception {
        this.validator = new Saml11TicketValidator(CONST_CAS_SERVER_URL);
        this.validator.setTolerance(1000L);
    }

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

        PublicTestHttpServer.instance().content = RESPONSE
                .getBytes(PublicTestHttpServer.instance().encoding);
        try {
            this.validator.validate("test", "test");
            fail("ValidationException expected due to 'no' response");
        } catch (final TicketValidationException e) {
            // expected
        }
    }
    
    public void testValidationSuccessWithNoAttributes() throws UnsupportedEncodingException {
        final Date now = new Date();
        final Date before = new Date(now.getTime() - 5000);
        final Date after = new Date(now.getTime() + 200000000);
        final String RESPONSE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"><SOAP-ENV:Header/><SOAP-ENV:Body><Response xmlns=\"urn:oasis:names:tc:SAML:1.0:protocol\" xmlns:saml=\"urn:oasis:names:tc:SAML:1.0:assertion\" xmlns:samlp=\"urn:oasis:names:tc:SAML:1.0:protocol\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" IssueInstant=\"" + CommonUtils.formatForUtcTime(now) + "\" MajorVersion=\"1\" MinorVersion=\"1\" Recipient=\"test\" ResponseID=\"_e1e2124c08ab456eab0bbab3e1c0c433\"><Status><StatusCode Value=\"samlp:Success\"></StatusCode></Status><Assertion xmlns=\"urn:oasis:names:tc:SAML:1.0:assertion\" AssertionID=\"_d2fd0d6e4da6a6d7d2ba5274ab570d5c\" IssueInstant=\"" + CommonUtils.formatForUtcTime(now) + "\" Issuer=\"testIssuer\" MajorVersion=\"1\" MinorVersion=\"1\"><Conditions NotBefore=\"" + CommonUtils.formatForUtcTime(before) + "\" NotOnOrAfter=\"" + CommonUtils.formatForUtcTime(after) + "\"><AudienceRestrictionCondition><Audience>test</Audience></AudienceRestrictionCondition></Conditions><AuthenticationStatement AuthenticationInstant=\"2008-06-19T14:34:44.426Z\" AuthenticationMethod=\"urn:ietf:rfc:2246\"><Subject><NameIdentifier>testPrincipal</NameIdentifier><SubjectConfirmation><ConfirmationMethod>urn:oasis:names:tc:SAML:1.0:cm:artifact</ConfirmationMethod></SubjectConfirmation></Subject></AuthenticationStatement></Assertion></Response></SOAP-ENV:Body></SOAP-ENV:Envelope>";
        PublicTestHttpServer.instance().content = RESPONSE
        .getBytes(PublicTestHttpServer.instance().encoding);
		try {
		    final Assertion a = this.validator.validate("test", "test");
		    assertEquals("testPrincipal", a.getPrincipal().getName());
		} catch (final TicketValidationException e) {
		    fail(e.toString());
		}
    }
}
