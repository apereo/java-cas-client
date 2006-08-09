/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.client.validation;

/**
 * Specific instance of a ValidationException that is thrown when the proxy
 * chain does not match what is returned.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class InvalidProxyChainValidationException extends ValidationException {

    /**
     * Unique id for serialization.
     */
    private static final long serialVersionUID = 1L;

    public InvalidProxyChainValidationException() {
        super();
    }

    public InvalidProxyChainValidationException(String message) {
        super(message);
    }
}
