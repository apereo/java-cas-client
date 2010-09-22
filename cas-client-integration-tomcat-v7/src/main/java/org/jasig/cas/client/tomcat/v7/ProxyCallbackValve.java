/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.tomcat.v7;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorage;
import org.jasig.cas.client.util.CommonUtils;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Handles watching a url for the proxy callback.
 * <p>
 * Because its tough to share state between valves, we expose the storage mechanism via a static variable.
 * <p>
 * This valve should be ordered before the authentication valves.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1.12
 */
public final class ProxyCallbackValve extends ValveBase {

    private static ProxyGrantingTicketStorage PROXY_GRANTING_TICKET_STORAGE;
    
    /** Logger instance */
    private final Log log = LogFactory.getLog(getClass());

    private String proxyGrantingTicketStorageClass;

    private String proxyCallbackUrl;

    public static ProxyGrantingTicketStorage getProxyGrantingTicketStorage() {
        return PROXY_GRANTING_TICKET_STORAGE;
    }

    public void setProxyGrantingTicketStorageClass(final String proxyGrantingTicketStorageClass) {
        this.proxyGrantingTicketStorageClass = proxyGrantingTicketStorageClass;
    }

    public void setProxyCallbackUrl(final String proxyCallbackUrl) {
        this.proxyCallbackUrl = proxyCallbackUrl;
    }

    protected void startInternal() throws LifecycleException {
        super.startInternal();

        try {
            CommonUtils.assertNotNull(this.proxyCallbackUrl, "the proxy callback url cannot  be null");
            CommonUtils.assertTrue(this.proxyCallbackUrl.startsWith("/"), "proxy callback url must start with \"/\"");

            final Class proxyGrantingTicketStorage = Class.forName(proxyGrantingTicketStorageClass);
            PROXY_GRANTING_TICKET_STORAGE = (ProxyGrantingTicketStorage) proxyGrantingTicketStorage.newInstance();
        } catch (final Exception e) {
            throw new LifecycleException(e);
        }
        this.log.info("Startup completed.");
    }

    public void invoke(final Request request, final Response response) throws IOException, ServletException {
        if (this.proxyCallbackUrl.equals(request.getRequestURI())) {
            this.log.debug("Processing proxy callback request.");
            CommonUtils.readAndRespondToProxyReceptorRequest(request, response, PROXY_GRANTING_TICKET_STORAGE);
            return;
        }

        getNext().invoke(request, response);
    }
}
