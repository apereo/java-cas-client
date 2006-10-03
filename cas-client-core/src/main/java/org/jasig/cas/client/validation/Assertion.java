/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.validation;

import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.Service;

import java.io.Serializable;
import java.util.Map;

/**
 * Interface to represent a successful response from the CAS Server.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public interface Assertion extends Serializable {

    /**
     * Method to retrieve the principal.
     *
     * @return the principal.
     */
    Principal getPrincipal();

    /**
     * Map of attributes returned by the CAS server. A client must know what
     * attributes he is looking for as CAS makes no claims about what attributes
     * are returned.
     *
     * @return the map of attributes.
     */
    Map getAttributes();

    /**
     * Retrieves a proxy ticket for the specific service.
     *
     * @param service The service to proxy to.
     * @return the Proxy Ticket Id or null.
     */
    String getProxyTicketFor(Service service);
}
