/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.validation;

import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.jasig.cas.client.util.CommonUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Concrete Implementation of the {@link Assertion}.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 * 
 */
public final class AssertionImpl implements Assertion {

    /** Unique Id for serialization. */
	private static final long serialVersionUID = -7767943925833639221L;

	/** The date from which the assertion is valid. */
    private final Date validFromDate;

    /** The date the assertion is valid until. */
    private final Date validUntilDate;

    /** Map of key/value pairs associated with this assertion. I.e. authentication type. */
    private final Map attributes;

    /** The principal for which this assertion is valid for. */
    private final AttributePrincipal principal;

    /**
     * Constructs a new Assertion with a Principal of the supplied name, a valid from date of now, no valid until date, and no attributes.
     *
     * @param name the name of the principal for which this assertion is valid.
     */
    public AssertionImpl(final String name) {
        this(new AttributePrincipalImpl(name));    
    }

    /**
     * Creates a new Assrtion with the supplied Principal.
     *
     * @param principal the Principal to associate with the Assertion.
     */
    public AssertionImpl(final AttributePrincipal principal) {
        this(principal, new HashMap());
    }

    /**
     * Create a new Assertion with the supplied principal and Assertion attributes.
     *
     * @param principal the Principal to associate with the Assertion.
     * @param attributes the key/value pairs for this attribute.
     */
    public AssertionImpl(final AttributePrincipal principal, final Map attributes) {
        this(principal, new Date(), null, attributes);
    }

    /**
     * Creats a new Assertion with the supplied principal, Assertion attributes, and start and valid until dates.
     *
     * @param principal the Principal to associate with the Assertion.
     * @param validFromDate when the assertion is valid from.
     * @param validUntilDate when the assertion is valid to.
     * @param attributes the key/value pairs for this attribute.
     */
    public AssertionImpl(final AttributePrincipal principal, final Date validFromDate, final Date validUntilDate, final Map attributes) {
        this.principal = principal;
        this.validFromDate = validFromDate;
        this.validUntilDate = validUntilDate;
        this.attributes = attributes;

        CommonUtils.assertNotNull(this.principal, "principal cannot be null.");
        CommonUtils.assertNotNull(this.validFromDate, "validFromDate cannot be null.");
        CommonUtils.assertNotNull(this.attributes, "attributes cannot be null.");
    }
    public Date getValidFromDate() {
        return this.validFromDate;
    }

    public Date getValidUntilDate() {
        return this.validUntilDate;
    }

    public Map getAttributes() {
        return this.attributes;
    }

    public AttributePrincipal getPrincipal() {
        return this.principal;
    }
}
