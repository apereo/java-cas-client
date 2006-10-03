/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.validation;

import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.client.proxy.ProxyRetriever;
import org.jasig.cas.client.util.CommonUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Concrete implementation of an Assertion.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class AssertionImpl implements Assertion {

    /**
     * Unique id for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Map of the attributes returned by the CAS server. This is optional as the
     * CAS server spec makes no mention of attributes.
     */
    private final Map attributes;

    /**
     * The principal who was authenticated.
     */
    private final Principal principal;

    /**
     * The Proxy Granting Ticket Id returned by the server.
     */
    private final String proxyGrantingTicketId;

    /**
     * Reference to ProxyRetriever so that clients can retrieve proxy tickets for a service.
     */
    private final ProxyRetriever proxyRetriever;


    /**
     * Simple constructor that accepts a Principal.
     *
     * @param principal the Principal this assertion is for.
     */
    public AssertionImpl(final Principal principal) {
        this(principal, null, null, null);
    }

    /**
     * Constructor that accepts a Principal and a map of attributes.
     *
     * @param principal  the Principal this assertion is for.
     * @param attributes a map of attributes about the principal.
     */
    public AssertionImpl(final Principal principal, final Map attributes) {
        this(principal, attributes, null, null);
    }

    /**
     * @param principal             the Principal this assertion is for.
     * @param attributes            a map of attributes about the principal.
     * @param proxyRetriever        used to retrieve proxy tickets from CAS Server.
     * @param proxyGrantingTicketId the Id to use to request proxy tickets.
     */
    public AssertionImpl(final Principal principal, final Map attributes,
                         final ProxyRetriever proxyRetriever, final String proxyGrantingTicketId) {
        CommonUtils.assertNotNull(principal, "principal cannot be null");

        this.principal = principal;
        this.attributes = attributes == null ? new HashMap() : attributes;
        this.proxyGrantingTicketId = CommonUtils
                .isNotEmpty(proxyGrantingTicketId) ? proxyGrantingTicketId : null;
        this.proxyRetriever = proxyRetriever;
    }

    public final Map getAttributes() {
        return this.attributes;
    }

    public String getProxyTicketFor(final Service service) {
        if (proxyRetriever == null || proxyGrantingTicketId == null) {
            return null;
        }

        return this.proxyRetriever.getProxyTicketIdFor(this.proxyGrantingTicketId, service);
    }

    public final Principal getPrincipal() {
        return this.principal;
    }
}
