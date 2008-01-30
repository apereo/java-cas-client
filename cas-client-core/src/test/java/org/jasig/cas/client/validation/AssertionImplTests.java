/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.validation;

import junit.framework.TestCase;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * Test cases for the {@link AssertionImpl}.
 *
 * @author Scott Battaglia
 * @version $Revision: 11737 $ $Date: 2007-10-03 09:14:02 -0400 (Tue, 03 Oct 2007) $
 * @since 3.0
 */
public final class AssertionImplTests extends TestCase {

    private static final AttributePrincipal CONST_PRINCIPAL = new AttributePrincipalImpl("test");

    private static final Map CONST_ATTRIBUTES = new HashMap();

    static {
        CONST_ATTRIBUTES.put("test", "test");
    }

    public void testPrincipalConstructor() {
        final Assertion assertion = new AssertionImpl(CONST_PRINCIPAL);

        assertEquals(CONST_PRINCIPAL, assertion.getPrincipal());
        assertTrue(assertion.getAttributes().isEmpty());
        assertNull(assertion.getPrincipal().getProxyTicketFor("test"));
    }

    public void testCompleteConstructor() {
        final Assertion assertion = new AssertionImpl(CONST_PRINCIPAL,
                CONST_ATTRIBUTES);

        assertEquals(CONST_PRINCIPAL, assertion.getPrincipal());
        assertEquals(CONST_ATTRIBUTES, assertion.getAttributes());
    }
}
