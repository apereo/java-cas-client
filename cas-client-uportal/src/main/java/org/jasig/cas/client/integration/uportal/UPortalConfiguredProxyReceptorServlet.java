/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.integration.uportal;

import org.jasig.cas.client.proxy.AbstractProxyReceptorServlet;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorage;
import org.jasig.portal.spring.PortalApplicationContextFacade;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

/**
 * Implementation of AbstractProxyReceptorServlet that retrieves the
 * ProxyGrantingTicket storage from the Portal Application Context instead of a
 * WebApplicationContext.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class UPortalConfiguredProxyReceptorServlet extends
        AbstractProxyReceptorServlet {

    /**
     * Unique Id for Serialization.
     */
    private static final long serialVersionUID = 6596608588362834646L;


    protected ProxyGrantingTicketStorage retrieveProxyGrantingTicketStorageFromConfiguration(final ServletConfig servletConfig) throws ServletException {
        logger.info("Retrieving ProxyGrantingTicketStorage from PortalApplicationContextFacade.");
        return (ProxyGrantingTicketStorage) PortalApplicationContextFacade
                .getPortalApplicationContext()
                .getBean(
                        AbstractCasSecurityContextFactory.CONST_CAS_PROXY_GRANTING_TICKET_STORAGE);
    }
}
