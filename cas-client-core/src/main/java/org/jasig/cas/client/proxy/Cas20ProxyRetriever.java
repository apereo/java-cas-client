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
package org.jasig.cas.client.proxy;

import java.net.URL;
import java.net.URLEncoder;
import org.jasig.cas.client.ssl.HttpURLConnectionFactory;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.util.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a ProxyRetriever that follows the CAS 2.0 specification.
 * For more information on the CAS 2.0 specification, please see the <a
 * href="http://www.jasig.org/cas/protocol">specification
 * document</a>.
 * <p/>
 * In general, this class will make a call to the CAS server with some specified
 * parameters and receive an XML response to parse.
 *
 * @author Scott Battaglia
 * @since 3.0
 */
public final class Cas20ProxyRetriever implements ProxyRetriever {

    /** Unique Id for serialization. */
    private static final long serialVersionUID = 560409469568911792L;

    private static final Logger logger = LoggerFactory.getLogger(Cas20ProxyRetriever.class);

    /**
     * Url to CAS server.
     */
    private final String casServerUrl;

    private final String encoding;

    /** Url connection factory to use when communicating with the server **/
    private final HttpURLConnectionFactory urlConnectionFactory;

    @Deprecated
    public Cas20ProxyRetriever(final String casServerUrl, final String encoding) {
        this(casServerUrl, encoding, null);
    }

    /**
     * Main Constructor.
     *
     * @param casServerUrl the URL to the CAS server (i.e. http://localhost/cas/)
     * @param encoding the encoding to use.
     * @param urlFactory url connection factory use when retrieving proxy responses from the server
     */
    public Cas20ProxyRetriever(final String casServerUrl, final String encoding,
            final HttpURLConnectionFactory urlFactory) {
        CommonUtils.assertNotNull(casServerUrl, "casServerUrl cannot be null.");
        this.casServerUrl = casServerUrl;
        this.encoding = encoding;
        this.urlConnectionFactory = urlFactory;
    }

    public String getProxyTicketIdFor(final String proxyGrantingTicketId, final String targetService) {
        CommonUtils.assertNotNull(proxyGrantingTicketId, "proxyGrantingTicketId cannot be null.");
        CommonUtils.assertNotNull(targetService, "targetService cannot be null.");

        final URL url = constructUrl(proxyGrantingTicketId, targetService);
        final String response;

        if (this.urlConnectionFactory != null) {
            response = CommonUtils.getResponseFromServer(url, this.urlConnectionFactory, this.encoding);
        } else {
            response = CommonUtils.getResponseFromServer(url, this.encoding);
        }
        final String error = XmlUtils.getTextForElement(response, "proxyFailure");

        if (CommonUtils.isNotEmpty(error)) {
            logger.debug(error);
            return null;
        }

        return XmlUtils.getTextForElement(response, "proxyTicket");
    }

    private URL constructUrl(final String proxyGrantingTicketId, final String targetService) {
        try {
            return new URL(this.casServerUrl + (this.casServerUrl.endsWith("/") ? "" : "/") + "proxy" + "?pgt="
                    + proxyGrantingTicketId + "&targetService=" + URLEncoder.encode(targetService, "UTF-8"));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
