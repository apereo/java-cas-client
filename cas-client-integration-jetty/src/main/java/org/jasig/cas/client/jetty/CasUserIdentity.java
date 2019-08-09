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
package org.jasig.cas.client.jetty;

import org.eclipse.jetty.server.UserIdentity;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.validation.Assertion;

import javax.security.auth.Subject;
import java.security.Principal;
import java.util.Collection;

/**
 * CAS user identity backed by assertion data.
 *
 * @author Marvin S. Addison
 */
public class CasUserIdentity implements UserIdentity {

    /** CAS principal. */
    private final AttributePrincipal principal;

    /** Assertion attribute containing role data. */
    private final String roleAttribute;


    /**
     * Creates a new instance from a CAS assertion containing principal information.
     *
     * @param assertion CAS assertion resulting from successful ticket validation.
     * @param roleAttribute Principal attribute containing role data.
     */
    public CasUserIdentity(final Assertion assertion, final String roleAttribute) {
        CommonUtils.assertNotNull(assertion, "Assertion cannot be null");
        this.principal = assertion.getPrincipal();
        this.roleAttribute = roleAttribute;
    }

    @Override
    public Subject getSubject() {
        final Subject subject = new Subject();
        subject.getPrincipals().add(principal);
        return subject;
    }

    @Override
    public Principal getUserPrincipal() {
        return principal;
    }

    @Override
    public boolean isUserInRole(final String role, final Scope scope) {
        if (roleAttribute != null) {
            final Object value = principal.getAttributes().get(roleAttribute);
            if (value instanceof Collection) {
                return ((Collection) value).contains(role);
            } else if (value instanceof String) {
                return value.equals(role);
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return principal.getName();
    }
}
