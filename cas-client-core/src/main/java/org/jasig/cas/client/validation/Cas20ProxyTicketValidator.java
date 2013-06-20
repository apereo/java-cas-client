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

import java.util.List;
import org.jasig.cas.client.util.XmlUtils;

/**
 * Extension to the traditional Service Ticket validation that will validate service tickets and proxy tickets.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public class Cas20ProxyTicketValidator extends Cas20ServiceTicketValidator {

    private boolean acceptAnyProxy;

    /** This should be a list of an array of Strings */
    private ProxyList allowedProxyChains = new ProxyList();

    /** Allows for an empty chain of proxy callback urls. **/
    private boolean allowEmptyProxyChain = true;

    public Cas20ProxyTicketValidator(final String casServerUrlPrefix) {
        super(casServerUrlPrefix);
    }

    protected final ProxyList getAllowedProxyChains() {
        return this.allowedProxyChains;
    }

    protected String getUrlSuffix() {
        return "proxyValidate";
    }

    protected void customParseResponse(final String response, final Assertion assertion)
            throws TicketValidationException {
        final List<String> proxies = XmlUtils.getTextForElements(response, "proxy");

        // this means there was nothing in the proxy chain, which is okay
        if ((this.allowEmptyProxyChain && proxies.isEmpty()) || this.acceptAnyProxy) {
            return;
        }

        final String[] proxiedList = proxies.toArray(new String[proxies.size()]);
        if (this.allowedProxyChains.contains(proxiedList)) {
            return;
        }

        throw new InvalidProxyChainTicketValidationException("Invalid proxy chain: " + proxies.toString());
    }

    public final void setAcceptAnyProxy(final boolean acceptAnyProxy) {
        this.acceptAnyProxy = acceptAnyProxy;
    }

    public final void setAllowedProxyChains(final ProxyList allowedProxyChains) {
        this.allowedProxyChains = allowedProxyChains;
    }

    protected final boolean isAcceptAnyProxy() {
        return this.acceptAnyProxy;
    }

    protected final boolean isAllowEmptyProxyChain() {
        return this.allowEmptyProxyChain;
    }

    /**
     * Set to determine whether empty proxy chains are allowed.
     * @see #customParseResponse(String, Assertion)
     * @param allowEmptyProxyChain whether to allow empty proxy chains or not.  True if so, false otherwise.
     */
    public final void setAllowEmptyProxyChain(final boolean allowEmptyProxyChain) {
        this.allowEmptyProxyChain = allowEmptyProxyChain;
    }
}
