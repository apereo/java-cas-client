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
package org.jasig.cas.client.tomcat.v7;

import org.jasig.cas.client.tomcat.AssertionCasRealmDelegate;
import org.jasig.cas.client.tomcat.CasRealm;

/**
 * Tomcat <code>Realm</code> that implements {@link CasRealm} for principal and
 * role data backed by the CAS {@link org.jasig.cas.client.validation.Assertion}.
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
