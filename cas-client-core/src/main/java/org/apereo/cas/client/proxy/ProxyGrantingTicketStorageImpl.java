/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apereo.cas.client.proxy;

import org.apereo.cas.client.util.CommonUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Implementation of {@link ProxyGrantingTicketStorage} that is backed by a
 * HashMap that keeps a ProxyGrantingTicket for a specified amount of time.
 * <p>
 * {@link ProxyGrantingTicketStorage#cleanUp()} must be called on a regular basis to
 * keep the HashMap from growing indefinitely.
 *
 * @author Scott Battaglia
 * @author Brad Cupit (brad [at] lsu {dot} edu)
 * @since 3.0
 */
public final class ProxyGrantingTicketStorageImpl implements ProxyGrantingTicketStorage {

    /**
     * Default timeout in milliseconds.
     */
    private static final long DEFAULT_TIMEOUT = 60000;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Map that stores the PGTIOU to PGT mappings.
     */
    private final ConcurrentMap<String, ProxyGrantingTicketHolder> cache = new ConcurrentHashMap<>();

    /**
     * time, in milliseconds, before a {@link ProxyGrantingTicketHolder}
     * is considered expired and ready for removal.
     *
     * @see ProxyGrantingTicketStorageImpl#DEFAULT_TIMEOUT
     */
    private final long timeout;

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
        this.timeout = timeout;
    }

    @Override
    public void save(final String proxyGrantingTicketIou, final String proxyGrantingTicket) {
        final var holder = new ProxyGrantingTicketHolder(proxyGrantingTicket);

        logger.debug("Saving ProxyGrantingTicketIOU and ProxyGrantingTicket combo: [{}, {}]", proxyGrantingTicketIou,
            proxyGrantingTicket);
        this.cache.put(proxyGrantingTicketIou, holder);
    }

    /**
     * NOTE: you can only retrieve a ProxyGrantingTicket once with this method.
     * It's removed after retrieval.
     */
    @Override
    public String retrieve(final String proxyGrantingTicketIou) {
        if (CommonUtils.isBlank(proxyGrantingTicketIou)) {
            return null;
        }

        final var holder = this.cache.get(proxyGrantingTicketIou);

        if (holder == null) {
            logger.info("No Proxy Ticket found for [{}].", proxyGrantingTicketIou);
            return null;
        }

        this.cache.remove(proxyGrantingTicketIou);

        logger.debug("Returned ProxyGrantingTicket of [{}]", holder.getProxyGrantingTicket());
        return holder.getProxyGrantingTicket();
    }

    /**
     * Cleans up old, expired proxy tickets. This method must be
     * called regularly via an external thread or timer.
     */
    @Override
    public void cleanUp() {
        for (final var holder : this.cache.entrySet()) {
            if (holder.getValue().isExpired(this.timeout)) {
                this.cache.remove(holder.getKey());
            }
        }
    }

    private static final class ProxyGrantingTicketHolder {

        private final String proxyGrantingTicket;

        private final long timeInserted;

        private ProxyGrantingTicketHolder(final String proxyGrantingTicket) {
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
}
