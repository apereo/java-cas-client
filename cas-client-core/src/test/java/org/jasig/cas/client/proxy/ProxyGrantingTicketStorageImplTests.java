/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.client.proxy;

/**
 * Test cases for the ProxyGrantingTicketStorageImplTests.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class ProxyGrantingTicketStorageImplTests extends
    AbstractProxyGrantingTicketStorageTests {

    protected void setUp() throws Exception {
        this.proxyGrantingTicketStorageImpl = new ProxyGrantingTicketStorageImpl();
        this.proxyGrantingTicketStorageImpl.setTimeout(1000);
        this.proxyGrantingTicketStorageImpl.init();
    }
}
