/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.validation;

import junit.framework.TestCase;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.SimplePrincipal;

import java.util.HashMap;
import java.util.Map;

/**
 * Test cases for the {@link AssertionImpl}.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class AssertionImplTests extends TestCase {

    private static final Principal CONST_PRINCIPAL = new SimplePrincipal("test");

    private static final String CONST_PROXY_GRANTING_TICKET_IOU = "proxyGrantingTicketIou";

    private static final Map CONST_ATTRIBUTES = new HashMap();

    static {
        CONST_ATTRIBUTES.put("test", "test");
    }

    public void testPrincipalConstructor() {
        final Assertion assertion = new AssertionImpl(CONST_PRINCIPAL);

        assertEquals(CONST_PRINCIPAL, assertion.getPrincipal());
        assertTrue(assertion.getAttributes().isEmpty());
        assertNull(assertion.getProxyGrantingTicketId());
    }

    public void testCompleteConstructor() {
        final Assertion assertion = new AssertionImpl(CONST_PRINCIPAL,
                CONST_ATTRIBUTES, CONST_PROXY_GRANTING_TICKET_IOU);

        assertEquals(CONST_PRINCIPAL, assertion.getPrincipal());
        assertEquals(CONST_ATTRIBUTES, assertion.getAttributes());
        assertEquals(CONST_PROXY_GRANTING_TICKET_IOU, assertion
                .getProxyGrantingTicketId());
    }
}
