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
package org.apereo.cas.client.authentication;

import org.apereo.cas.client.proxy.ProxyRetriever;
import org.apereo.cas.client.util.CommonUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serial;
import java.util.Collections;
import java.util.Map;

/**
 * Concrete implementation of the AttributePrincipal interface.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public class AttributePrincipalImpl extends SimplePrincipal implements AttributePrincipal {

    private static final Logger LOGGER = LoggerFactory.getLogger(AttributePrincipalImpl.class);

    /** Unique Id for Serialization */
    @Serial
    private static final long serialVersionUID = -1443182634624927187L;

    /** Map of key/value pairs about this principal. */
    private final Map<String, Object> attributes;

    /** The CAS 2 ticket used to retrieve a proxy ticket. */
    private final String proxyGrantingTicket;

    /** The method to retrieve a proxy ticket from a CAS server. */
    private final ProxyRetriever proxyRetriever;

    /**
     * Constructs a new principal with an empty map of attributes.
     *
     * @param name the unique identifier for the principal.
     */
    public AttributePrincipalImpl(final String name) {
        this(name, Collections.<String, Object>emptyMap());
    }

    /**
     * Constructs a new principal with the supplied name and attributes.
     *
     * @param name the unique identifier for the principal.
     * @param attributes the key/value pairs for this principal.
     */
    public AttributePrincipalImpl(final String name, final Map<String, Object> attributes) {
        this(name, attributes, null, null);
    }

    /**
     * Constructs a new principal with the supplied name and the proxying capabilities.
     *
     * @param name the unique identifier for the principal.
     * @param proxyGrantingTicket the ticket associated with this principal.
     * @param proxyRetriever the ProxyRetriever implementation to call back to the CAS server.
     */
    public AttributePrincipalImpl(final String name, final String proxyGrantingTicket,
                                  final ProxyRetriever proxyRetriever) {
        this(name, Collections.<String, Object>emptyMap(), proxyGrantingTicket, proxyRetriever);
    }

    /**
     * Constructs a new principal with the supplied name, attributes, and proxying capabilities.
     *
     * @param name the unique identifier for the principal.
     * @param attributes the key/value pairs for this principal.
     * @param proxyGrantingTicket the ticket associated with this principal.
     * @param proxyRetriever the ProxyRetriever implementation to call back to the CAS server.
     */
    public AttributePrincipalImpl(final String name, final Map<String, Object> attributes,
                                  final String proxyGrantingTicket, final ProxyRetriever proxyRetriever) {
        super(name);
        this.attributes = attributes;
        this.proxyGrantingTicket = proxyGrantingTicket;
        this.proxyRetriever = proxyRetriever;

        CommonUtils.assertNotNull(this.attributes, "attributes cannot be null.");
    }

    @Override
    public String getProxyTicketFor(final String service) {
        if (proxyGrantingTicket != null) {
            return this.proxyRetriever.getProxyTicketIdFor(this.proxyGrantingTicket, service);
        }

        LOGGER.debug("No ProxyGrantingTicket was supplied, so no Proxy Ticket can be retrieved.");
        return null;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    /**
     * Returns the proxy granting ticket associated with this principal, if available.
     *
     * @return the proxy granting ticket or null
     */
    public String getProxyGrantingTicket() {
        return proxyGrantingTicket;
    }
}
