/*
 * Copyright 2010 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.tomcat.v6;

import java.security.Principal;

import org.apache.catalina.realm.RealmBase;
import org.jasig.cas.client.tomcat.CasRealm;

/**
 * Base <code>Realm</code> implementation for all CAS realms.
 *
 * @author Marvin S. Addison
 * @version $Revision$
 *
 */
public abstract class AbstractCasRealm extends RealmBase implements CasRealm {
    /** {@inheritDoc} */
    public Principal authenticate(final Principal p) {
        return getDelegate().authenticate(p);
    }

    /** {@inheritDoc} */
    public String[] getRoles(final Principal p) {
        return getDelegate().getRoles(p);
    }

    /** {@inheritDoc} */
    public boolean hasRole(final Principal principal, final String role) {
        return getDelegate().hasRole(principal, role);
    }

    /** {@inheritDoc} */
    public String toString() {
        return getName();
    }
    
    /** {@inheritDoc} */
    public String getInfo() {
        return getClass().getName() + "/1.0";
    }

    /** {@inheritDoc} */
    protected String getName() {
        return getClass().getSimpleName();
    }

    /** {@inheritDoc} */
    protected String getPassword(final String username) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    protected Principal getPrincipal(String username) {
        throw new UnsupportedOperationException();
    }

    /**
     * @return Delegate that all {@link CasRealm} operations are delegated to.
     */
    protected abstract CasRealm getDelegate();
}
