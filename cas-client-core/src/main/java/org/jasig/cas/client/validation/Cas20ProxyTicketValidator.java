/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.validation;

import org.apache.commons.httpclient.HttpClient;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SimpleService;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorage;
import org.jasig.cas.client.proxy.ProxyRetriever;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.util.XmlUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Implementation of the TicketValidator interface that knows how to handle
 * proxy tickets.
 * <p/>
 * In your XML configuration file, proxy chains should be defined as
 * follows: &lt;list&gt; &lt;value&gt; proxy1 proxy2 proxy3&lt;/value&gt;
 * &lt;value&gt; proxy2 proxy4 proxy5&lt;/value&gt; &lt;value&gt; proxy4
 * proxy5 proxy6&lt;/value&gt; &lt;/list&gt;
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class Cas20ProxyTicketValidator extends Cas20ServiceTicketValidator {

    /* List of proxy chains that we accept. */
    private final List proxyChains;

    /**
     * Boolean whether we accept any proxy request or not.
     */
    private final boolean acceptAnyProxy;

    /**
     * @param casServerUrl   the url to the CAS server, minus the endpoint.
     * @param renew          flag for whether we require authentication to be via an initial authentication.
     * @param httpClient     an instance of HttpClient to do the calls.
     * @param proxyChains    the chains of proxy lists that we accept tickets from.
     * @param acceptAnyProxy flag on whether we accept any proxy or not.
     */
    public Cas20ProxyTicketValidator(final String casServerUrl, final boolean renew, final HttpClient httpClient, List proxyChains, boolean acceptAnyProxy) {
        this(casServerUrl, renew, httpClient, null, proxyChains, acceptAnyProxy, null, null);
    }

    public Cas20ProxyTicketValidator(final String casServerUrl, final boolean renew, final HttpClient httpClient, final Service proxyCallbackUrl, List proxyChains, boolean acceptAnyProxy, final ProxyGrantingTicketStorage proxyGrantingTicketStorage, final ProxyRetriever proxyRetriever) {
        super(casServerUrl, renew, httpClient, proxyCallbackUrl, proxyGrantingTicketStorage, proxyRetriever);

        CommonUtils.assertTrue(proxyChains != null || acceptAnyProxy,
                "proxyChains cannot be null or acceptAnyProxy must be true.");
        CommonUtils.assertTrue((proxyChains != null && !proxyChains
                .isEmpty())
                || acceptAnyProxy,
                "proxyChains cannot be empty or acceptAnyProxy must be true.");

        // assume each proxy chain has a length of 2
        final List tempProxyChains = new ArrayList(proxyChains.size() * 2);
        for (final Iterator iter = proxyChains.iterator(); iter.hasNext();) {
            final String[] values = ((String) iter.next()).split(" ");
            final Service[] principals = new Service[values.length];

            for (int i = 0; i < principals.length; i++) {
                principals[i] = new SimpleService(values[i]);
            }

            tempProxyChains.add(principals);
        }

        this.proxyChains = tempProxyChains;
        this.acceptAnyProxy = acceptAnyProxy;
    }

    protected String getValidationUrlName() {
        return "proxyValidate";
    }


    protected Assertion getValidAssertionInternal(final String response, final String principal, final String proxyGrantingTicketIou) throws ValidationException {
        final List proxies = XmlUtils.getTextForElements(response, "proxy");

        // this means there was nothing in the proxy chain, which is okay
        if (proxies.isEmpty() || this.acceptAnyProxy) {
            return getAssertionBasedOnProxyGrantingTicketIou(proxyGrantingTicketIou, principal);
        }

        final Service[] principals = new Service[proxies.size()];
        int i = 0;
        for (final Iterator iter = proxies.iterator(); iter.hasNext();) {
            principals[i++] = new SimpleService((String) iter.next());
        }

        for (Iterator iter = this.proxyChains.iterator(); iter.hasNext();) {
            if (Arrays.equals(principals, (Object[]) iter.next())) {
                return getAssertionBasedOnProxyGrantingTicketIou(proxyGrantingTicketIou, principal);
            }
        }

        throw new InvalidProxyChainValidationException();
    }
}
