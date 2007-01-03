/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.authorization;

/**
 * Exception to be thrown if the user is not authorized to use the system.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class AuthorizationException extends RuntimeException {

    /**
     * Unique ID for serialization.
     */
    private static final long serialVersionUID = 5912038088650643442L;

    /**
     * Constructs a new exception with the specified detail message and cause.
     * 
     * @param message the detail message. The detail message is saved for later retrieval by the Throwable.getMessage() method.
     * @param cause the cause (which is saved for later retrieval by the Throwable.getCause() method). (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public AuthorizationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified detail message.
     * 
     * @param message the detail message. The detail message is saved for later retrieval by the Throwable.getMessage() method.
     */
    public AuthorizationException(final String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified cause and a detail message of (cause==null ? null : cause.toString()) (which typically contains the class and detail message of cause).
     * 
     * @param cause the cause (which is saved for later retrieval by the Throwable.getCause() method). (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public AuthorizationException(final Throwable cause) {
        super(cause);
    }
}
