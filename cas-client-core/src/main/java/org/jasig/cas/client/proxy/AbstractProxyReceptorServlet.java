/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.proxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.client.util.CommonUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Implementation of an HttpServlet that accepts ProxyGrantingTicketIous and
 * ProxyGrantingTickets and stores them in an implementation of
 * {@link ProxyGrantingTicketStorage}.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public abstract class AbstractProxyReceptorServlet extends HttpServlet {

    /**
     * The name we expect the instance of ProxyGrantingTicketStorage to be
     * instanciated under in the applicationContext.
     */
    public static final String CONST_PROXY_GRANTING_TICKET_STORAGE_BEAN_NAME = "proxyGrantingTicketStorage";

    /**
     * Constant representing the ProxyGrantingTicket IOU Request Parameter.
     */
    private static final String PARAM_PROXY_GRANTING_TICKET_IOU = "pgtIou";

    /**
     * Constant representing the ProxyGrantingTicket Request Parameter.
     */
    private static final String PARAM_PROXY_GRANTING_TICKET = "pgtId";

    /**
     * Instance of ProxyGrantingTicketStorage to store ProxyGrantingTickets.
     */
    private ProxyGrantingTicketStorage proxyGrantingTicketStorage;

    /**
     * Instance of Commons Logging
     */
    protected final Log logger = LogFactory.getLog(this.getClass());

    /**
     * Unique Id for Serialization.
     */
    private static final long serialVersionUID = 8766956323018042995L;

    protected final void doGet(final HttpServletRequest request,
                               final HttpServletResponse response) throws ServletException,
            IOException {
        final String proxyGrantingTicketIou = request
                .getParameter(PARAM_PROXY_GRANTING_TICKET_IOU);

        final String proxyGrantingTicket = request
                .getParameter(PARAM_PROXY_GRANTING_TICKET);

        if (CommonUtils.isBlank(proxyGrantingTicket)
                || CommonUtils.isBlank(proxyGrantingTicketIou)) {
            response.getWriter().write("");
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Received proxyGrantingTicketId ["
                    + proxyGrantingTicket + "] for proxyGrantingTicketIou ["
                    + proxyGrantingTicketIou + "]");
        }

        this.proxyGrantingTicketStorage.save(proxyGrantingTicketIou,
                proxyGrantingTicket);

        response.getWriter().write("<?xml version=\"1.0\"?>");
        response
                .getWriter()
                .write(
                        "<casClient:proxySuccess xmlns:casClient=\"http://www.yale.edu/tp/casClient\" />");
    }

    /**
     * Delegates to the protected method <code>retrieveProxyGrantingTicketStorageFromConfiguration()</code>.
     */
    public final void init(final ServletConfig servletConfig) throws ServletException {
        this.proxyGrantingTicketStorage = retrieveProxyGrantingTicketStorageFromConfiguration(servletConfig);
    }

    /**
     * Abstract class to retrieve the <code>ProxyGrantingTicketStorage</code> from the ServletConfig.  Its up to
     * implementing classes to figure out where they are initializing/retrieving the object from.
     *
     * @param servletConfig the Servlet Config that has access to the <code>ProxyGrantingTicketStorage</code>.
     * @return the initialized <code>ProxyGrantingTicketStorage</code>.
     * @throws ServletException if there is an exception retrieving the <code>ProxyGrantingTicketStorage</code>.
     */
    protected abstract ProxyGrantingTicketStorage retrieveProxyGrantingTicketStorageFromConfiguration(final ServletConfig servletConfig) throws ServletException;
}
