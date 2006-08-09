/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.client.proxy;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public abstract class AbstractProxyGrantingTicketStorageTests extends TestCase {

    protected ProxyGrantingTicketStorageImpl proxyGrantingTicketStorageImpl;

    public final void testNullValue() {
        assertNull(this.proxyGrantingTicketStorageImpl
                .retrieve("this should not exist"));
    }

    public final void testValueExists() {
        final String CONST_KEY = "testKey";
        final String CONST_VALUE = "testValue";

        this.proxyGrantingTicketStorageImpl.save(CONST_KEY, CONST_VALUE);

        assertEquals(CONST_VALUE, this.proxyGrantingTicketStorageImpl
                .retrieve(CONST_KEY));
    }
}
