/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
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
	 * Unique Id for Serialization
	 */
	private static final long serialVersionUID = -7036248720402711806L;

	/**
     * Constructs an exception with the supplied message.
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
