package org.jasig.cas.client.validation;

/**
 * Exception denotes that an invalid proxy chain was sent from the CAS server to the local application. 
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class InvalidProxyChainTicketValidationException extends TicketValidationException {

    /**
     * Constructs an exception with the supplied message.
     * @param string the supplied message.
     */

    public InvalidProxyChainTicketValidationException(final String string) {
        super(string);
    }

    /**
     * Constructs an exception with the supplied message and chained throwable.
     * @param string the message.
     * @param throwable the root exception.
     */
    public InvalidProxyChainTicketValidationException(final String string, final Throwable throwable) {
        super(string, throwable);
    }

    /**
     * Constructs an exception with the chained throwable.
     * @param throwable the root exception.
     */
    public InvalidProxyChainTicketValidationException(final Throwable throwable) {
        super(throwable);
    }
}
