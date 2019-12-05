/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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
package org.apereo.cas.client.tomcat.v90;

import org.apache.catalina.Wrapper;
import org.apache.catalina.realm.RealmBase;
import org.apereo.cas.client.tomcat.CasRealm;

import java.security.Principal;

/**
 * Base <code>Realm</code> implementation for all CAS realms.
 *
 * @author Marvin S. Addison
 * @version $Revision$
 *
 */
public abstract class AbstractCasRealm extends RealmBase implements CasRealm {

    /** {@inheritDoc} */
    @Override
    public Principal authenticate(final Principal p) {
        return getDelegate().authenticate(p);
    }

    /** {@inheritDoc} */
    @Override
    public String[] getRoles(final Principal p) {
        return getDelegate().getRoles(p);
    }

    @Override
    public boolean hasRole(final Principal principal, final String role) {
        return getDelegate().hasRole(principal, role);
    }

    @Override
    public boolean hasRole(final Wrapper wrapper, final Principal principal, final String role) {
        return hasRole(principal, role);
    }

    public String getInfo() {
        return getClass().getName() + "/1.0";
    }
    
    @Override
    protected String getPassword(final String userName) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Principal getPrincipal(final String userName) {
        throw new UnsupportedOperationException();
    }

    /**
     * @return Delegate that all {@link CasRealm} operations are delegated to.
     */
    protected abstract CasRealm getDelegate();
}
