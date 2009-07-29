/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.validation;

import org.jasig.cas.client.util.CommonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Arrays;

/**
 * Holding class for the proxy list to make Spring configuration easier.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1.3
 */
public final class ProxyList {

    private final List proxyChains;

    public ProxyList(final List proxyChains) {
        CommonUtils.assertNotNull(proxyChains, "List of proxy chains cannot be null.");

        // Assert that all entries in the list are String[]
        for (final Iterator iter = proxyChains.iterator(); iter.hasNext();) {
            CommonUtils.assertTrue(iter.next() instanceof String[], "Proxy chains must contain String[] items exclusively.");
        }

        this.proxyChains = proxyChains;
    }

    public ProxyList() {
        this(new ArrayList());
    }

    public boolean contains(String[] proxiedList) {
        for (Iterator iter = this.proxyChains.iterator(); iter.hasNext();) {
            if (Arrays.equals(proxiedList, (String[]) iter.next())) {
                return true;
            }
        }

        return false;
    }
    
    public String toString() {
    	return this.proxyChains.toString();
    }
}
