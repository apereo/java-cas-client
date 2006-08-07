package org.jasig.cas.client.integration.uportal;

import junit.framework.TestCase;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.authentication.principal.SimpleService;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.AssertionImpl;
import org.jasig.cas.client.validation.TicketValidator;
import org.jasig.cas.client.validation.ValidationException;
import org.jasig.cas.client.proxy.ProxyRetriever;
import org.jasig.portal.security.PortalSecurityException;

import java.util.HashMap;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class CasSecurityContextTests extends TestCase {

    private CasSecurityContext context;


    protected void setUp() throws Exception {
        this.context = new CasSecurityContext(new TicketValidator() {

            public Assertion validate(String ticketId, Service service) throws ValidationException {
                return new AssertionImpl(new SimplePrincipal("test"), new HashMap(), null);
            }
        }, new SimpleService("test"), null);
        this.context.getOpaqueCredentialsInstance().setCredentials("ticket");
    }

    public void testAuthenticate() throws Exception {
        this.context.authenticate();

        assertEquals("test", this.context.getPrincipal().getUID());
        assertTrue(this.context.isAuthenticated());
        assertNull(this.context.getProxyTicket(new SimpleService("test")));
    }

    public void testAuthenticateWithProxy() throws Exception {
        this.context = new CasSecurityContext(new TicketValidator() {

            public Assertion validate(String ticketId, Service service) throws ValidationException {
                return new AssertionImpl(new SimplePrincipal("test"), new HashMap(), "test");
            }
        }, new SimpleService("test"), new ProxyRetriever() {

            public String getProxyTicketIdFor(String proxyGrantingTicketId, Service targetService) {
                return "test";
            }
        });
        this.context.getOpaqueCredentialsInstance().setCredentials("ticket");
        this.context.authenticate();
        assertEquals("test", this.context.getProxyTicket(new SimpleService("test")));

    }

    public void testAuthenticateFail() {
        this.context = new CasSecurityContext(new TicketValidator() {

            public Assertion validate(String ticketId, Service service) throws ValidationException {
                throw new ValidationException("test");
            }
        }, new SimpleService("test"), null);
        this.context.getOpaqueCredentialsInstance().setCredentials("ticket");

        try {
            this.context.authenticate();
            fail("Exception expected.");
        } catch (PortalSecurityException e) {
            assertTrue(e.getCause() instanceof ValidationException);
        }
    }

    public void testGetAuthType() {
        assertEquals(ICasSecurityContext.CAS_AUTHTYPE, this.context.getAuthType());
    }
}
