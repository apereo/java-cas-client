package org.jasig.cas.client.validation;

/**
 * Contract for a validator that will confirm the validity of a supplied ticket.
 * <p>
 * Validator makes no statement about how to validate the ticket or the format of the ticket (other than that it must be a String).
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public interface TicketValidator {

    /**
     * Attempts to validatea  ticket for the provided service.
     *
     * @param ticket the ticket to attempt to validate.
     * @param service the service this ticket is valid for.
     * @return an assertion from the ticket.
     * @throws TicketValidationException if the ticket cannot be validated.
     *
     */
    Assertion validate(String ticket, String service) throws TicketValidationException;
}
