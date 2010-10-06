/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.proxy;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import net.sf.ehcache.CacheManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1.9
 */
public final class EhcacheBackedProxyGrantingTicketStorageImpl implements ProxyGrantingTicketStorage {

    public static final String EHCACHE_CACHE_NAME = "org.jasig.cas.client.proxy.EhcacheBackedProxyGrantingTicketStorageImpl.cache";

    private static final Log log = LogFactory.getLog(EhcacheBackedProxyGrantingTicketStorageImpl.class);

    final Cache cache;

    public EhcacheBackedProxyGrantingTicketStorageImpl() {
        this(CacheManager.getInstance().getCache(EHCACHE_CACHE_NAME));
        log.info("Created cache with name: " + this.cache.getName());
    }

    public EhcacheBackedProxyGrantingTicketStorageImpl(final Cache cache) {
        this.cache = cache;

    }

    public void save(final String proxyGrantingTicketIou, final String proxyGrantingTicket) {
        final Element element = new Element(proxyGrantingTicketIou, proxyGrantingTicket);
        this.cache.put(element);
    }

    public String retrieve(final String proxyGrantingTicketIou) {
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
