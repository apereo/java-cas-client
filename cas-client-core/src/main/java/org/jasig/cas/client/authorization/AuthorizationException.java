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

    public AuthorizationException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public AuthorizationException(String arg0) {
        super(arg0);
    }

    public AuthorizationException(Throwable arg0) {
        super(arg0);
    }
}
