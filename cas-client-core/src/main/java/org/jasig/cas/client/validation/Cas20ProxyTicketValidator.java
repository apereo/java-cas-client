/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.client.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SimpleService;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.util.XmlUtils;

/**
 * Implementation of the TicketValidator interface that knows how to handle
 * proxy tickets.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class Cas20ProxyTicketValidator extends Cas20ServiceTicketValidator {

    /* List of proxy chains that we accept. */
    private List proxyChains;

    /** Boolean whether we accept any proxy request or not. */
    private boolean acceptAnyProxy;

    protected String getValidationUrlName() {
        return "proxyValidate";
    }

    protected Assertion getValidAssertionInternal(final String response,
        final Assertion assertion) throws ValidationException {
        final List proxies = XmlUtils.getTextForElements(response, "proxy");
        final Service[] principals = new Service[proxies.size()];

        // this means there was nothing in the proxy chain, which is okay
        if (principals.length == 0 || this.acceptAnyProxy) {
            return assertion;
        }

        int i = 0;
        for (final Iterator iter = proxies.iterator(); iter.hasNext();) {
            principals[i++] = new SimpleService((String) iter.next());
        }

        boolean found = false;
        for (Iterator iter = this.proxyChains.iterator(); iter.hasNext();) {
            if (Arrays.equals(principals, (Object[]) iter.next())) {
                found = true;
                break;
            }
        }

        if (!found) {
            throw new InvalidProxyChainValidationException();
        }

        return new AssertionImpl(assertion.getPrincipal(), assertion
            .getAttributes(), assertion.getProxyGrantingTicketId());
    }

    /**
     * In your XML configuration file, proxy chains should be defined as
     * follows: &lt;list&gt; &lt;value&gt; proxy1 proxy2 proxy3&lt;/value&gt;
     * &lt;value&gt; proxy2 proxy4 proxy5&lt;/value&gt; &lt;value&gt; proxy4
     * proxy5 proxy6&lt;/value&gt; &lt;/list&gt;
     * 
     * @param proxyChains
     */
    public final void setProxyChains(final List proxyChains) {
        this.proxyChains = proxyChains;
    }

    /**
     * Set this flag to true if you don't care where the proxied request came
     * from.
     * 
     * @param acceptAnyProxy flag on whether we accept any proxy or not.
     */
    public void setAcceptAnyProxy(final boolean acceptAnyProxy) {
        this.acceptAnyProxy = acceptAnyProxy;
    }

    protected void afterPropertiesSetInternal() {
        super.afterPropertiesSetInternal();

        CommonUtils.assertTrue(this.proxyChains != null || this.acceptAnyProxy,
            "proxyChains cannot be null or acceptAnyProxy must be true.");
        CommonUtils.assertTrue((this.proxyChains != null && !this.proxyChains
            .isEmpty())
            || this.acceptAnyProxy,
            "proxyChains cannot be empty or acceptAnyProxy must be true.");

        final List tempProxyChains = new ArrayList();
        for (final Iterator iter = this.proxyChains.iterator(); iter.hasNext();) {
            final String[] values = ((String) iter.next()).split(" ");
            final Service[] principals = new Service[values.length];

            for (int i = 0; i < principals.length; i++) {
                principals[i] = new SimpleService(values[i]);
            }

            tempProxyChains.add(principals);
        }

        this.proxyChains = tempProxyChains;
    }
}
