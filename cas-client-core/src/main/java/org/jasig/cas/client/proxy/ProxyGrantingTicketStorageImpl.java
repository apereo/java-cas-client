/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.client.proxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link ProxyGrantingTicketStorage} that is backed by a
 * HashMap that keeps a ProxyGrantingTicket for a specified amount of time.
 * <p>
 * A cleanup thread is run periodically to clean out the HashMap.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class ProxyGrantingTicketStorageImpl implements
    ProxyGrantingTicketStorage {

    /**
     * Default timeout in milliseconds.
     */
    private static final long DEFAULT_TIMEOUT = 60000;

    private Map cache = new HashMap();

    private long timeout = DEFAULT_TIMEOUT;

    /**
     * NOTE: you can only retrieve a ProxyGrantingTicket once with this method.
     * Its removed after retrieval.
     */
    public String retrieve(final String proxyGrantingTicketIou) {
        final ProxyGrantingTicketHolder holder = (ProxyGrantingTicketHolder) this.cache
            .get(proxyGrantingTicketIou);

        if (holder == null) {
            return null;
        }

        this.cache.remove(holder);

        return holder.getProxyGrantingTicket();
    }

    public void save(final String proxyGrantingTicketIou,
        final String proxyGrantingTicket) {
        final ProxyGrantingTicketHolder holder = new ProxyGrantingTicketHolder(
            proxyGrantingTicket);

        this.cache.put(proxyGrantingTicketIou, holder);
    }

    public void init() throws Exception {
        final Thread thread = new ProxyGrantingTicketCleanupThread(
            this.timeout, this.cache);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Sets the amount of time to hold on to a ProxyGrantingTicket if its never
     * been retrieved.
     * 
     * @param timeout the time to hold on to the ProxyGrantingTicket
     */
    public void setTimeout(final long timeout) {
        this.timeout = timeout;
    }

    private class ProxyGrantingTicketHolder {

        private final String proxyGrantingTicket;

        private final long timeInserted;

        protected ProxyGrantingTicketHolder(final String proxyGrantingTicket) {
            this.proxyGrantingTicket = proxyGrantingTicket;
            this.timeInserted = System.currentTimeMillis();
        }

        public String getProxyGrantingTicket() {
            return this.proxyGrantingTicket;
        }

        final boolean isExpired(final long timeout) {
            return System.currentTimeMillis() - this.timeInserted > timeout;
        }
    }

    private class ProxyGrantingTicketCleanupThread extends Thread {

        private final long timeout;

        private final Map cache;

        public ProxyGrantingTicketCleanupThread(final long timeout,
            final Map cache) {
            this.timeout = timeout;
            this.cache = cache;
        }

        public void run() {

            while (true) {
                try {
                    Thread.sleep(this.timeout);
                } catch (final InterruptedException e) {
                    // nothing to do
                }

                final List itemsToRemove = new ArrayList();

                synchronized (this.cache) {
                    for (final Iterator iter = this.cache.keySet().iterator(); iter
                        .hasNext();) {
                        final Object key = iter.next();
                        final ProxyGrantingTicketHolder holder = (ProxyGrantingTicketHolder) this.cache
                            .get(key);

                        if (holder.isExpired(this.timeout)) {
                            itemsToRemove.add(key);
                        }
                    }

                    for (final Iterator iter = itemsToRemove.iterator(); iter
                        .hasNext();) {
                        this.cache.remove(iter.next());
                    }
                }
            }
        }
    }
}
