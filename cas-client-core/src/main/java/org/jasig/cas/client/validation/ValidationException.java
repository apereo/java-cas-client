/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.validation;

/**
 * Implementation of Exception to be thrown when there is an error validating
 * the Ticket returned from the CAS server.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class ValidationException extends Exception {

    /**
     * Unique Id for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    public ValidationException() {
        super();
    }

    /**
     * Constructor that accepts a message and a chained exception.
     *
     * @param message the error message.
     * @param cause   the exception we are chaining with.
     */
    public ValidationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor that accepts a message.
     *
     * @param message the error message.
     */
    public ValidationException(final String message) {
        super(message);
    }

    /**
     * Constructor that accepts a chained exception.
     *
     * @param cause the exception we are chaining with.
     */
    public ValidationException(final Throwable cause) {
        super(cause);
    }
}
