/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.proxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.util.XmlUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
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
 * @version $Revision: 11729 $ $Date: 2007-09-26 14:22:30 -0400 (Tue, 26 Sep 2007) $
 * @since 3.0
 */
public final class Cas20ProxyRetriever implements ProxyRetriever {

    /** Unique Id for serialization. */
	private static final long serialVersionUID = 560409469568911791L;

	/**
     * Instance of Commons Logging.
     */
    private final Log log = LogFactory.getLog(this.getClass());

    /**
     * Url to CAS server.
     */
    private final String casServerUrl;

    /**
     * Main Constructor.
     *
     * @param casServerUrl the URL to the CAS server (i.e. http://localhost/cas/)
     */
    public Cas20ProxyRetriever(final String casServerUrl) {
        CommonUtils.assertNotNull(casServerUrl,
                "casServerUrl cannot be null.");
        this.casServerUrl = casServerUrl;
    }

    public String getProxyTicketIdFor(final String proxyGrantingTicketId,
                                      final String targetService) {

        final String url = constructUrl(proxyGrantingTicketId, targetService);
        final String response = CommonUtils.getResponseFromServer(url);
        final String error = XmlUtils.getTextForElement(response, "proxyFailure");

        if (CommonUtils.isNotEmpty(error)) {
            log.debug(error);
            return null;
        }

        return XmlUtils.getTextForElement(response, "proxyTicket");
    }

    private String constructUrl(final String proxyGrantingTicketId, final String targetService) {
        try {
        	return this.casServerUrl + (this.casServerUrl.endsWith("/") ? "" : "/") + "proxy" + "?pgt="
            + proxyGrantingTicketId + "&targetService="
            + URLEncoder.encode(targetService, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
