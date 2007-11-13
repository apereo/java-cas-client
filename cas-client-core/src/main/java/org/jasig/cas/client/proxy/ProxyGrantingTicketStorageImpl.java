/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
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
 * <p/>
 * A cleanup thread is run periodically to clean out the HashMap.
 *
 * @author Scott Battaglia
 * @version $Revision: 11729 $ $Date: 2006-09-26 14:22:30 -0400 (Tue, 26 Sep 2006) $
 * @since 3.0
 */
public final class ProxyGrantingTicketStorageImpl implements
        ProxyGrantingTicketStorage {

    /**
     * Default timeout in milliseconds.
     */
    private static final long DEFAULT_TIMEOUT = 60000;

    /**
     * Map that stores the PGTIOU to PGT mappings.
     */
    private final Map cache = new HashMap();

    /**
     * Constructor set the timeout to the default value.
     */
    public ProxyGrantingTicketStorageImpl() {
        this(DEFAULT_TIMEOUT);
    }

    /**
     * Sets the amount of time to hold on to a ProxyGrantingTicket if its never
     * been retrieved.
     *
     * @param timeout the time to hold on to the ProxyGrantingTicket
     */
    public ProxyGrantingTicketStorageImpl(final long timeout) {
        final Thread thread = new ProxyGrantingTicketCleanupThread(
                timeout, this.cache);
        thread.setDaemon(true);
        thread.start();
    }

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

    private final class ProxyGrantingTicketHolder {

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

    private final class ProxyGrantingTicketCleanupThread extends Thread {

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
