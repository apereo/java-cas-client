/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.validation;

/**
 * Static holder that places Assertion in a threadlocal.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class AssertionHolder {

    /**
     * ThreadLocal to hold the Assertion for Threads to access.
     */
    private static final ThreadLocal threadLocal = new ThreadLocal();


    /**
     * Retrieve the assertion from the ThreadLocal.
     */
    public static Assertion getAssertion() {
        return (Assertion) threadLocal.get();
    }

    /**
     * Add the Assertion to the ThreadLocal.
     */
    public static void setAssertion(final Assertion assertion) {
        threadLocal.set(assertion);
    }

    /**
     * Clear the ThreadLocal.
     */
    public static void clear() {
        threadLocal.set(null);
    }
}
