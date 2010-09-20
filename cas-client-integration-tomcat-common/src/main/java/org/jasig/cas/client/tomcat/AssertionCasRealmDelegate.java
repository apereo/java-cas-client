/*
 * Copyright 2010 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.tomcat;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
 *
 */
public class AssertionCasRealmDelegate implements CasRealm {
    /** Default role attribute name */
    public static final String DEFAULT_ROLE_NAME = "role";
    
    /** Log instance */
    private final Log log = LogFactory.getLog(getClass());

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
        CommonUtils.assertTrue(p instanceof AttributePrincipal, "Expected instance of AttributePrincipal but got " + p.getClass());

        final Collection roles = getRoleCollection(p);
        final String[] array = new String[roles.size()];
        roles.toArray(array);
        return array;
    }

    /** {@inheritDoc} */
    public boolean hasRole(final Principal principal, final String role) {
        return getRoleCollection(principal).contains(role);
    }

    /**
     * Retrieves the attributes for a Principal.  To make life easy this should NEVER return null.
     *
     * @param p the principal to check.
     * @return the list of attribute values that matched this role, or an empty collection if they don't.
     */
    private Collection getRoleCollection(final Principal p) {
        if (!(p instanceof AttributePrincipal)) {
            return Collections.emptyList();
        }

        final Object attributes = ((AttributePrincipal) p).getAttributes().get(this.roleAttributeName);

        if (attributes == null) {
            return Collections.emptyList();
        }

        if (attributes instanceof Collection) {
            return (Collection) attributes;
        }

        return Arrays.asList(new Object[] {attributes});
    }
}
