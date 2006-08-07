/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.client.authorization;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class AuthorizationExceptionTests extends TestCase {

    private static final String CONST_MESSAGE = "t";

    private static final Exception CONST_THROWABLE = new RuntimeException();

    public void testMessageThrowable() {
        final AuthorizationException e = new AuthorizationException(
            CONST_MESSAGE, CONST_THROWABLE);

        assertEquals(CONST_MESSAGE, e.getMessage());
        assertEquals(CONST_THROWABLE, e.getCause());
    }

    public void testMessage() {
        final AuthorizationException e = new AuthorizationException(
            CONST_MESSAGE);

        assertEquals(CONST_MESSAGE, e.getMessage());
    }

    public void testThrowable() {
        final AuthorizationException e = new AuthorizationException(
            CONST_THROWABLE);

        assertEquals(CONST_THROWABLE, e.getCause());
    }
}
