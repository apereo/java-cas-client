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
package org.jasig.cas.client.tomcat.v85;

import java.security.Principal;
import org.apache.catalina.Wrapper;
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

    public boolean hasRole(final Principal principal, final String role) {
        return getDelegate().hasRole(principal, role);
    }

    /**
     * Tomcat 7.0.8 changed their APIs so {@link #hasRole(java.security.Principal, String)} is only valid for 7.0.7 and below.
     */
    public boolean hasRole(final Wrapper wrapper, final Principal principal, final String role) {
        return hasRole(principal, role);
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
    protected String getPassword(final String userName) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    protected Principal getPrincipal(final String userName) {
        throw new UnsupportedOperationException();
    }

    /**
     * @return Delegate that all {@link CasRealm} operations are delegated to.
     */
    protected abstract CasRealm getDelegate();
}
