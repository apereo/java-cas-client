/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.client.proxy;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.distribution.RemoteCacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1.9
 */
public final class EhcacheBackedProxyGrantingTicketStorageImpl extends AbstractEncryptedProxyGrantingTicketStorageImpl {

    public static final String EHCACHE_CACHE_NAME = "org.jasig.cas.client.proxy.EhcacheBackedProxyGrantingTicketStorageImpl.cache";

    private static final Logger logger = LoggerFactory.getLogger(EhcacheBackedProxyGrantingTicketStorageImpl.class);

    final Cache cache;

    public EhcacheBackedProxyGrantingTicketStorageImpl() {
        this(CacheManager.getInstance().getCache(EHCACHE_CACHE_NAME));
        logger.info("Created cache with name: {}", this.cache.getName());
    }

    public EhcacheBackedProxyGrantingTicketStorageImpl(final Cache cache) {
        super();
        this.cache = cache;
    }

    public void saveInternal(final String proxyGrantingTicketIou, final String proxyGrantingTicket) {
        final Element element = new Element(proxyGrantingTicketIou, proxyGrantingTicket);
        try {
            this.cache.put(element);
        } catch (final RemoteCacheException e) {
            logger.warn("Exception accessing one of the remote servers: {}", e.getMessage(), e);
        }
    }

    public String retrieveInternal(final String proxyGrantingTicketIou) {
        final Element element = this.cache.get(proxyGrantingTicketIou);

        if (element == null) {
            return null;
        }

        return (String) element.getValue();
    }

    public void cleanUp() {
        // nothing to do
    }
}
