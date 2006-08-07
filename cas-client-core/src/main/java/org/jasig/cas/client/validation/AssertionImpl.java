/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.client.validation;

import org.jasig.cas.authentication.principal.Principal;
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

    /** Unique id for serialization. */
    private static final long serialVersionUID = 1L;

    /**
     * Map of the attributes returned by the CAS server. This is optional as the
     * CAS server spec makes no mention of attributes.
     */
    private final Map attributes;

    /** The principal who was authenticated. */
    private final Principal principal;

    /** The Proxy Granting Ticket Id returned by the server. */
    private final String proxyGrantingTicketId;

    public AssertionImpl(final Principal principal) {
        this(principal, null, null);
    }

    public AssertionImpl(final Principal principal, final Map attributes,
        final String proxyGrantingTicketId) {
        CommonUtils.assertNotNull(principal, "principal cannot be null");

        this.principal = principal;
        this.attributes = attributes == null ? new HashMap() : attributes;
        this.proxyGrantingTicketId = CommonUtils
            .isNotEmpty(proxyGrantingTicketId) ? proxyGrantingTicketId : null;
    }

    public Map getAttributes() {
        return this.attributes;
    }

    public Principal getPrincipal() {
        return this.principal;
    }

    public String getProxyGrantingTicketId() {
        return this.proxyGrantingTicketId;
    }
}
