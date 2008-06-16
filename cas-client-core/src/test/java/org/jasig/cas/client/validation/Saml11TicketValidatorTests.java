package org.jasig.cas.client.validation;

import org.jasig.cas.client.PublicTestHttpServer;

import java.io.UnsupportedEncodingException;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1.3
 */
public final class Saml11TicketValidatorTests extends AbstractTicketValidatorTests {

    private Saml11TicketValidator validator;

    protected void setUp() throws Exception {
        this.validator = new Saml11TicketValidator(CONST_CAS_SERVER_URL);
        this.validator.setTolerance(Long.MAX_VALUE);
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
}
