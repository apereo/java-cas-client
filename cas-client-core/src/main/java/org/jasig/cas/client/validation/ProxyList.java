/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
