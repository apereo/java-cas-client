package org.jasig.cas.client.validation;

/**
 * Generic exception to be thrown when ticket validation fails.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public class TicketValidationException extends Exception {

    /**
     * Constructs an exception with the supplied messsage.
     *
     * @param string the message
     */
    public TicketValidationException(final String string) {
        super(string);
    }

    /**
     * Constructs an exception with the supplied message and chained throwable.
     *
     * @param string the message
     * @param throwable the original exception
     */
    public TicketValidationException(final String string, final Throwable throwable) {
        super(string, throwable);
    }

    /**
     * Constructs an exception with the chained throwable.
     * @param throwable the original exception.                                    
     */
    public TicketValidationException(final Throwable throwable) {
        super(throwable);
    }
}
