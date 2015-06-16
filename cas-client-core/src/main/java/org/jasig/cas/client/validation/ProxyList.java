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
import java.util.List;
import org.jasig.cas.client.authentication.ExactUrlPatternMatcherStrategy;
import org.jasig.cas.client.authentication.RegexUrlPatternMatcherStrategy;
import org.jasig.cas.client.authentication.UrlPatternMatcherStrategy;
import org.jasig.cas.client.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holding class for the proxy list to make Spring configuration easier.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1.3
 */
public final class ProxyList {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final List<List<UrlPatternMatcherStrategy>> proxyChains;

    public ProxyList(final List<String[]> proxyChains) {
        CommonUtils.assertNotNull(proxyChains, "List of proxy chains cannot be null.");

        this.proxyChains = new ArrayList<List<UrlPatternMatcherStrategy>>();

        for (final String[] list : proxyChains) {
            final List<UrlPatternMatcherStrategy> chain = new ArrayList<UrlPatternMatcherStrategy>();

            for (final String item : list) {
                if (item.startsWith("^")) {
                    chain.add(new RegexUrlPatternMatcherStrategy(item));
                } else {
                    chain.add(new ExactUrlPatternMatcherStrategy(item));
                }
            }

            this.proxyChains.add(chain);
        }
    }

    public ProxyList() {
        this(new ArrayList<String[]>());
    }

    public boolean contains(final String[] proxiedList) {
        StringBuilder loggingOutput;

        for (final List<UrlPatternMatcherStrategy> proxyChain : this.proxyChains) {
            loggingOutput = new StringBuilder();

            if (proxyChain.size() == proxiedList.length) {
                for (int linkIndex = 0; linkIndex < proxyChain.size(); linkIndex++) {
                    final String linkToTest = proxiedList[linkIndex];
                    loggingOutput.append(linkToTest);

                    if (proxyChain.get(linkIndex).matches(linkToTest)) {
                        //If we are at the last link, we found a good proxyChain.
                        if (linkIndex == proxyChain.size() - 1) {
                            logger.info("Proxy chain matched: {}", loggingOutput.toString());
                            return true;
                        }

                    } else {
                        logger.warn("Proxy chain did not match at {}. Skipping to next allowedProxyChain", loggingOutput.toString());
                        break;
                    }
                    loggingOutput.append("->");
                }
            }
        }

        logger.warn("No proxy chain matched the allowedProxyChains list.");
        return false;
    }

    public String toString() {
        return this.proxyChains.toString();
    }
}
