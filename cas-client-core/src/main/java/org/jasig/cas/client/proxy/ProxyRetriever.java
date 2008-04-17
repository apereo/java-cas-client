/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.proxy;

import java.io.Serializable;

/**
 * Interface to abstract the retrieval of a proxy ticket to make the
 * implementation a black box to the client.
 *
 * @author Scott Battaglia
 * @version $Revision: 11729 $ $Date: 2007-09-26 14:22:30 -0400 (Tue, 26 Sep 2007) $
 * @since 3.0
 */
public interface ProxyRetriever extends Serializable {

    /**
     * Retrieves a proxy ticket for a specific targetService.
     *
     * @param proxyGrantingTicketId the ProxyGrantingTicketId
     * @param targetService         the service we want to proxy.
     * @return the ProxyTicket Id if Granted, null otherwise.
     */
    String getProxyTicketIdFor(String proxyGrantingTicketId,
                               String targetService);
}
