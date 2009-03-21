/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.proxy;

/**
 * Interface for the storage and retrieval of ProxyGrantingTicketIds by mapping
 * them to a specific ProxyGrantingTicketIou.
 *
 * @author Scott Battaglia
 * @version $Revision: 11729 $ $Date: 2007-09-26 14:22:30 -0400 (Tue, 26 Sep 2007) $
 * @since 3.0
 */
public interface ProxyGrantingTicketStorage {

    /**
     * Method to save the ProxyGrantingTicket to the backing storage facility.
     *
     * @param proxyGrantingTicketIou used as the key
     * @param proxyGrantingTicket    used as the value
     */
    public void save(String proxyGrantingTicketIou, String proxyGrantingTicket);

    /**
     * Method to retrieve a ProxyGrantingTicket based on the
     * ProxyGrantingTicketIou. Note that implementations are not guaranteed to
     * return the same result if retrieve is called twice with the same
     * proxyGrantingTicketIou.
     *
     * @param proxyGrantingTicketIou used as the key
     * @return the ProxyGrantingTicket Id or null if it can't be found
     */
    public String retrieve(String proxyGrantingTicketIou);
    
    /**
     * Called on a regular basis by an external timer,
     * giving implementations a chance to remove stale data.
     */
    public void cleanUp();
}
