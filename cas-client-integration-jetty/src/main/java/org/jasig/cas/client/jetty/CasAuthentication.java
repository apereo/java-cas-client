package org.jasig.cas.client.jetty;

import org.eclipse.jetty.security.UserAuthentication;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.validation.Assertion;

/**
 * CAS-specific user authentication.
 *
 * @author Marvin S. Addison
 */
public class CasAuthentication extends UserAuthentication {

    /** CAS authenticator that produced this authentication. */
    private final CasAuthenticator authenticator;

    /** CAS ticket that was successfully validated to permit authentication. */
    private final String ticket;


    /**
     * Creates a new instance.
     *
     * @param authenticator The authenticator that produced this authentication.
     * @param ticket The CAS ticket that was successfully validated to permit authentication.
     * @param assertion The CAS assertion produced from successful ticket validation.
     */
    public CasAuthentication(final CasAuthenticator authenticator, final String ticket, final Assertion assertion) {
        super(authenticator.getAuthMethod(), new CasUserIdentity(assertion, authenticator.getRoleAttribute()));
        CommonUtils.assertNotNull(ticket, "Ticket cannot be null");
        CommonUtils.assertNotNull(authenticator, "CasAuthenticator cannot be null");
        this.authenticator = authenticator;
        this.ticket = ticket;
    }

    /** @return The CAS ticket that was successfully validated to permit authentication. */
    public String getTicket() {
        return ticket;
    }

    @Override
    public void logout() {
        super.logout();
        this.authenticator.clearCachedAuthentication(ticket);
    }
}
