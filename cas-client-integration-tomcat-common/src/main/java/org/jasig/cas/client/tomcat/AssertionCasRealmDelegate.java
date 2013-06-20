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
package org.jasig.cas.client.tomcat;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.util.CommonUtils;

/**
 * {@link CasRealm} implementation with prinicpal and role data backed by the {@link org.jasig.cas.client.validation.Assertion}.
 * In particular, an {@link AttributePrincipal} is expected from which the values of
 * the role attribute are retrieved.  The default role attribute name is "role",
 * but this can be customized via {@link #setRoleAttributeName(String)}.
 * <p>
 * Authentication always succeeds and simply returns the given principal.
 *
 * @author Marvin S. Addison
 * @version $Revision$
 * @since 3.1.12
 *
 */
public class AssertionCasRealmDelegate implements CasRealm {

    /** Default role attribute name */
    public static final String DEFAULT_ROLE_NAME = "role";

    /** Name of the role attribute in the principal's attributes */
    private String roleAttributeName = DEFAULT_ROLE_NAME;

    /**
     * @param name Name of the attribute in the principal that contains role data.
     */
    public void setRoleAttributeName(final String name) {
        this.roleAttributeName = name;
    }

    /** {@inheritDoc} */
    public Principal authenticate(final Principal p) {
        return p;
    }

    /** {@inheritDoc} */
    public String[] getRoles(final Principal p) {
        CommonUtils.assertTrue(p instanceof AttributePrincipal,
                "Expected instance of AttributePrincipal but got " + p.getClass());

        final Collection<String> roles = getRoleCollection(p);
        final String[] array = new String[roles.size()];
        roles.toArray(array);
        return array;
    }

    /** {@inheritDoc} */
    public boolean hasRole(final Principal principal, final String role) {
        if ("*".equals(role)) {
            return true;
        }
        return getRoleCollection(principal).contains(role);
    }

    /**
     * Retrieves the attributes for a Principal.  To make life easy this should NEVER return null.
     *
     * @param p the principal to check.
     * @return the list of attribute values that matched this role, or an empty collection if they don't.
     */
    @SuppressWarnings("unchecked")
    private Collection<String> getRoleCollection(final Principal p) {
        if (!(p instanceof AttributePrincipal)) {
            return Collections.emptyList();
        }

        final Object attributes = ((AttributePrincipal) p).getAttributes().get(this.roleAttributeName);

        if (attributes == null) {
            return Collections.emptyList();
        }

        if (attributes instanceof Collection<?>) {
            return (Collection<String>) attributes;
        }

        return Arrays.asList(attributes.toString());
    }
}
