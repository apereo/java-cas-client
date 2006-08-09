/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.client.integration.uportal;

import org.jasig.cas.authentication.principal.Service;

/**
 * Interface implemented by CAS security contexts. These implementations are
 * aware of proxying, and can retrieve a ticket from CAS for accessing a
 * specific service.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public interface ICasSecurityContext {

    /**
     * Authentication type for CAS authentication
     */
    public static final int CAS_AUTHTYPE = 0x1701;

    /**
     * Retrieve a Proxy Ticket Id for a particular service we wish to proxy against.
     *
     * @param service the service to retrieve a proxy ticket for.
     * @return the ticket id, or null if no ticket could be retrieved.
     */
    String getProxyTicket(Service service);
}
