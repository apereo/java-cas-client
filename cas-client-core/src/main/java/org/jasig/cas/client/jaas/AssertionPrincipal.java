/*
 * Copyright 2010 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.jaas;

import java.io.Serializable;

import org.jasig.cas.client.authentication.SimplePrincipal;
import org.jasig.cas.client.validation.Assertion;

/**
 * Principal implementation that contains the CAS ticket validation assertion.
 *
 * @author Marvin S. Addison
 * @version $Revision$
 * @since 3.1.11
 *
 */
public class AssertionPrincipal extends SimplePrincipal implements Serializable {
    
    /** AssertionPrincipal.java */
    private static final long serialVersionUID = 2288520214366461693L;

    /** CAS assertion describing authenticated state */
    private Assertion assertion;

    /**
     * Creates a new principal containing the CAS assertion.
     *
     * @param name Principal name.
     * @param assertion CAS assertion.
     */
    public AssertionPrincipal(final String name, final Assertion assertion) {
        super(name);
        this.assertion = assertion;
    }

    /**
     * @return CAS ticket validation assertion.
     */
    public Assertion getAssertion() {
        return this.assertion;
    }
}
