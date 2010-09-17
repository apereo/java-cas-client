/*
 * Copyright 2010 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.tomcat;

import java.security.Principal;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.client.authentication.AttributePrincipal;

/**
 * {@link CasRealm} implementation with prinicpal and role data backed by the {@link Assertion}.
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
        if (p instanceof AttributePrincipal) {
            final Collection roles = getRoleCollection(p);
            if (roles != null) {
                final String[] array = new String[roles.size()];
                roles.toArray(array);
                return array;
            } else {
                return new String[0];
            }
        } else {
            throw new IllegalArgumentException("Expected instance of AttributePrincipal but got " + p);
        }
    }

    /** {@inheritDoc} */
    public boolean hasRole(final Principal principal, final String role) {
        final Collection roles = getRoleCollection(principal);
        if (roles != null) {
            return roles.contains(role);
        } else {
            return false;
        }
    }
    
    private Collection getRoleCollection(final Principal p) {
        if (p instanceof AttributePrincipal) {
            final Collection attributes =
                (Collection) ((AttributePrincipal) p).getAttributes().get(roleAttributeName);
            if (attributes == null) {
                log.debug(p + " has no attribute named " + roleAttributeName);
            }
            return attributes;
        } else {
            return null;
        }
    }
}
