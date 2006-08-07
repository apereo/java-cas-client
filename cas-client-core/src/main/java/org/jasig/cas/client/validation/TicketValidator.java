/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.client.validation;

import org.jasig.cas.authentication.principal.Service;

/**
 * Interface to encapsulate the validation of a ticket. The inteface is
 * specification neutral. Any implementation can be provided, including
 * something that parses CAS1 or CAS2 responses.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public interface TicketValidator {

    /**
     * Method to validate a ticket for a give Service.
     * 
     * @param ticketId the ticket to validate
     * @param service the service to validate the ticket for
     * @return the Assertion about the ticket (never null)
     * @throws ValidationException if there is a problem validating the ticket.
     */
    Assertion validate(String ticketId, Service service)
        throws ValidationException;
}
