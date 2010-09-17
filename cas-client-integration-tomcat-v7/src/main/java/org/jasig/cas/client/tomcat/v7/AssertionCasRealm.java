/*
 * Copyright 2010 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.tomcat.v7;

import org.jasig.cas.client.tomcat.AssertionCasRealmDelegate;
import org.jasig.cas.client.tomcat.CasRealm;

/**
 * Tomcat <code>Realm</code> that implements {@link CasRealm} for principal and
 * role data backed by the CAS {@link Assertion}.
 * <p>
 * Authentication always succeeds and simply returns the given principal.
 *
 * @author Marvin S. Addison
 * @version $Revision$
 *
 */
public class AssertionCasRealm extends AbstractCasRealm {
    private final AssertionCasRealmDelegate delegate = new AssertionCasRealmDelegate();

    /**
     * @param name Name of the attribute in the principal that contains role data.
     */
    public void setRoleAttributeName(final String name) {
        delegate.setRoleAttributeName(name);
    }

    /** {@inheritDoc} */
    protected CasRealm getDelegate() {
        return delegate;
    }
}
