/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.client.proxy;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.util.XmlUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Implementation of a ProxyRetriever that follows the CAS 2.0 specification.
 * For more information on the CAS 2.0 specification, please see the <a
 * href="http://www.ja-sig.org/products/cas/overview/protocol/index.html">specification
 * document</a>.
 * <p/>
 * In general, this class will make a call to the CAS server with some specified
 * parameters and receive an XML response to parse.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class Cas20ProxyRetriever implements ProxyRetriever {

    /**
     * Instance of Commons Logging.
     */
    protected final Log log = LogFactory.getLog(this.getClass());

    /**
     * Url to CAS server.
     */
    private final String casServerUrl;

    /**
     * Instance of HttpClient for connecting to server.
     */
    private final HttpClient httpClient;

    /**
     * Main Constructor.
     *
     * @param casServerUrl the URL to the CAS server (i.e. http://localhost/cas/)
     * @param httpClient   an Instance of a thread-safe HttpClient.
     */
    public Cas20ProxyRetriever(final String casServerUrl, final HttpClient httpClient) {
        CommonUtils.assertNotNull(casServerUrl,
                "casServerUrl cannot be null.");
        CommonUtils
                .assertNotNull(httpClient, "httpClient cannot be null.");
        this.casServerUrl = casServerUrl;
        this.httpClient = httpClient;
    }

    public String getProxyTicketIdFor(final String proxyGrantingTicketId,
                                      final Service targetService) {

        final String url = constructUrl(proxyGrantingTicketId, targetService
                .getId());

        final GetMethod method = new GetMethod(url);
        try {
            this.httpClient.executeMethod(method);
            final String response = method.getResponseBodyAsString();

            final String error = XmlUtils.getTextForElement(response,
                    "proxyFailure");

            if (CommonUtils.isNotEmpty(error)) {
                log.debug(error);
                return null;
            }

            return XmlUtils.getTextForElement(response, "proxyTicket");

        } catch (IOException e) {
            log.error(e, e);
            return null;
        } finally {
            method.releaseConnection();
        }
    }

    private String constructUrl(final String proxyGrantingTicketId,
                                final String targetService) {
        try {
            return this.casServerUrl + "proxy" + "?pgt="
                    + proxyGrantingTicketId + "&targetService="
                    + URLEncoder.encode(targetService, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
