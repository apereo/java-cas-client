/*
 * Copyright 2010 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.authentication;

import java.io.Serializable;
import java.security.Principal;

import org.jasig.cas.client.util.CommonUtils;

/**
 * Simple security principal implementation.
 *
 * @author Marvin S. Addison
 * @version $Revision$
 * @since 3.1.11
 *
 */
public class SimplePrincipal implements Principal, Serializable {

    /** SimplePrincipal.java */
    private static final long serialVersionUID = -5645357206342793145L;

    /** The unique identifier for this principal. */
    private final String name;

    /**
     * Creates a new principal with the given name.
     * @param name Principal name.
     */
    public SimplePrincipal(final String name) {
        this.name = name;
        CommonUtils.assertNotNull(this.name, "name cannot be null.");
    }

    public final String getName() {
        return this.name;
    }

    public String toString() {
        return getName();
    }

    public boolean equals(final Object o) {
        if (o == null) {
            return false;
        } else if (!(o instanceof SimplePrincipal)) {
            return false;
        } else {
            return getName().equals(((SimplePrincipal)o).getName());
        }
    }

    public int hashCode() {
        return 37 * getName().hashCode();
    }
}
