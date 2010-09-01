package org.jasig.cas.client.tomcat;

import org.apache.catalina.LifecycleException;
import org.jasig.cas.client.validation.Cas10TicketValidator;
import org.jasig.cas.client.validation.TicketValidator;

/**
 * Authenticator that handles CAS 1.0 responses.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1.12
 */
public class Cas10CasAuthenticator extends AbstractCasAuthenticator {

    private TicketValidator ticketValidator;

    protected TicketValidator getTicketValidator() {
        return this.ticketValidator;
    }

    protected void startInternal() throws LifecycleException {
        super.startInternal();

        this.ticketValidator = new Cas10TicketValidator(getCasServerUrlPrefix());
    }
}
