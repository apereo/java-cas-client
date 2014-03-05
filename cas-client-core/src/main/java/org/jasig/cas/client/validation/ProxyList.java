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
package org.jasig.cas.client.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jasig.cas.client.util.CommonUtils;

/**
 * Holding class for the proxy list to make Spring configuration easier.
 *
 * @author Scott Battaglia
 * @author John Gasper
 * @version $Revision$ $Date$
 * @since 3.1.3
 */
public final class ProxyList {

    private final List<String[]> proxyChains;

    public ProxyList(final List<String[]> proxyChains) {
        CommonUtils.assertNotNull(proxyChains, "List of proxy chains cannot be null.");
        this.proxyChains = proxyChains;
    }

    public ProxyList() {
        this(new ArrayList<String[]>());
    }

    public boolean contains(String[] proxiedList) {
        for (final String[] list : this.proxyChains) {
            if (Arrays.equals(proxiedList, list)) {
                return true;
            } else {
                //strings might be regex, so check for each string
                if (list.length == proxiedList.length) {
                    boolean passed = false;
                    
                    for (int i=0; i<list.length; i++) {
                        String pattern = list[i];
                        if ((pattern.startsWith("^") && proxiedList[i].matches(pattern))
                            || pattern.equals(proxiedList[i])) {
                            passed = true;
                        } else {
                            break;
                        }
                    }
                    if (passed == true) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public String toString() {
        return this.proxyChains.toString();
    }
}
