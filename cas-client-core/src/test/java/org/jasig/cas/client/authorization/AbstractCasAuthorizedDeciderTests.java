/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.client.authorization;

import junit.framework.TestCase;
import org.jasig.cas.authentication.principal.SimplePrincipal;

/**
 * Abstract test for all CasAuthorizedDecider implementations.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public abstract class AbstractCasAuthorizedDeciderTests extends TestCase {

    private CasAuthorizedDecider casAuthorizedDecider;

    protected abstract CasAuthorizedDecider getCasAuthorizedDeciderImpl();

    protected final void setUp() throws Exception {
        this.casAuthorizedDecider = getCasAuthorizedDeciderImpl();
    }

    public final void testIsAuthorized() {
        assertTrue(this.casAuthorizedDecider
                .isAuthorizedToUseApplication(new SimplePrincipal("scott")));
    }

    public final void testIsNotAuthorized() {
        assertFalse(this.casAuthorizedDecider
                .isAuthorizedToUseApplication(new SimplePrincipal("not")));
    }
}
