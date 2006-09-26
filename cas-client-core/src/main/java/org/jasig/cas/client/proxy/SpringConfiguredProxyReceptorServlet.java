/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.proxy;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.util.Map;

/**
 * Implementation of an HttpServlet that accepts ProxyGrantingTicketIous and
 * ProxyGrantingTickets and stores them in an implementation of
 * {@link ProxyGrantingTicketStorage}.
 * <p/>
 * Note that <code>ProxyReceptorServlet</code> attempts to load a
 * {@link ProxyGrantingTicketStorage} from the ApplicationContext either via the
 * name "proxyGrantingTicketStorage" or by type. One of these two must exist
 * within the applicationContext or the initialization of the
 * ProxyReceptorServlet will fail.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class SpringConfiguredProxyReceptorServlet extends
        AbstractProxyReceptorServlet {

    /**
     * Unique Id for serialization
     */
    private static final long serialVersionUID = -5642050740265266568L;

    public void init(final ServletConfig servletConfig) throws ServletException {
        final WebApplicationContext context = WebApplicationContextUtils
                .getRequiredWebApplicationContext(servletConfig.getServletContext());

        if (context.containsBean(CONST_PROXY_GRANTING_TICKET_STORAGE_BEAN_NAME)) {
            this
                    .setProxyGrantingTicketStorage((ProxyGrantingTicketStorage) context
                            .getBean(CONST_PROXY_GRANTING_TICKET_STORAGE_BEAN_NAME,
                                    ProxyGrantingTicketStorage.class));
            return;
        }

        final Map map = context
                .getBeansOfType(ProxyGrantingTicketStorage.class);

        if (map.isEmpty()) {
            throw new ServletException("No ProxyGrantingTicketStorage found!");
        }

        if (map.size() > 1) {
            throw new ServletException(
                    "Expecting one ProxyGrantingTicketStorage and found multiple instances.");
        }

        setProxyGrantingTicketStorage((ProxyGrantingTicketStorage) map.get(map
                .keySet().iterator().next()));
    }

}
