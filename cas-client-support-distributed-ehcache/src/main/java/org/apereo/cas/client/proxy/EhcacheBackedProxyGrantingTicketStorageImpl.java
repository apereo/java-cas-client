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

import org.ehcache.Cache;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1.9
 */
public final class EhcacheBackedProxyGrantingTicketStorageImpl extends AbstractEncryptedProxyGrantingTicketStorageImpl {

    public static final String EHCACHE_CACHE_NAME = "org.apereocas.client.proxy.EhcacheBackedProxyGrantingTicketStorageImpl.cache";

    private static final Logger logger = LoggerFactory.getLogger(EhcacheBackedProxyGrantingTicketStorageImpl.class);

    private final Cache cache;

    public EhcacheBackedProxyGrantingTicketStorageImpl() {
        final var cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
        cacheManager.init();
        cache = cacheManager.createCache(EHCACHE_CACHE_NAME,
            CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, String.class,
                ResourcePoolsBuilder.heap(1000)));
        logger.info("Created cache with name: {}", EHCACHE_CACHE_NAME);
    }

    @Override
    public void saveInternal(final String proxyGrantingTicketIou, final String proxyGrantingTicket) {
        try {
            this.cache.put(proxyGrantingTicketIou, proxyGrantingTicket);
        } catch (final Exception e) {
            logger.warn("Exception accessing one of the remote servers: {}", e.getMessage(), e);
        }
    }

    @Override
    public String retrieveInternal(final String proxyGrantingTicketIou) {
        return proxyGrantingTicketIou == null ? null : (String) this.cache.get(proxyGrantingTicketIou);
    }

    @Override
    public void cleanUp() {
    }
}
