/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.client.proxy;

import org.jasig.cas.authentication.principal.Service;

/**
 * Interface to abstract the retrieval of a proxy ticket to make the
 * implementation a black box to the client.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public interface ProxyRetriever {

    /**
     * Retrieves a proxy ticket for a specific targetService.
     *
     * @param proxyGrantingTicketId the ProxyGrantingTicketId
     * @param targetService         the service we want to proxy.
     * @return the ProxyTicket Id if Granted, null otherwise.
     */
    String getProxyTicketIdFor(String proxyGrantingTicketId,
                               Service targetService);
}
