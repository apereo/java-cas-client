/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.integration.uportal;

import org.jasig.cas.authentication.principal.SimpleService;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.LocalConnectionContext;

import java.util.Enumeration;

/**
 * Extension to LocalConnectionContext that will retrieve and append a proxy
 * ticket to a given descriptor.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class CasConnectionContext extends LocalConnectionContext {

    /**
     * Instance of ICasSecurityContext.
     */
    private ICasSecurityContext casSecurityContext;

    public String getDescriptor(String descriptor,
                                final ChannelRuntimeData channelRuntimeData) {
        if (log.isTraceEnabled()) {
            log.trace("getDescriptor(" + descriptor + ", " + channelRuntimeData
                    + ")");
        }

        descriptor = descriptor == null ? "null" : descriptor;

        if (channelRuntimeData.getHttpRequestMethod().equals("GET")) {

            if (this.casSecurityContext != null) {
                final String proxyTicket = this.casSecurityContext
                        .getProxyTicket(new SimpleService(descriptor));

                if (proxyTicket != null) {
                    // append ticket parameter and value to query string
                    if (descriptor.indexOf('?') != -1) {
                        descriptor = descriptor + "&ticket=" + proxyTicket;
                    } else {
                        descriptor = descriptor + "?ticket=" + proxyTicket;
                    }
                }
            }
        }

        return descriptor;
    }

    public void init(final ChannelStaticData channelStaticData) {
        final ISecurityContext securityContext = channelStaticData.getPerson()
                .getSecurityContext();

        if (ICasSecurityContext.class.isAssignableFrom(securityContext
                .getClass())
                && securityContext.isAuthenticated()) {
            this.casSecurityContext = (ICasSecurityContext) securityContext;
        }

        final Enumeration enumeration = securityContext.getSubContexts();

        while (enumeration.hasMoreElements()) {
            final ISecurityContext context = (ISecurityContext) enumeration
                    .nextElement();

            if (ISecurityContext.class.isAssignableFrom(context.getClass())
                    && context.isAuthenticated()) {
                this.casSecurityContext = (ICasSecurityContext) context;
            }
        }

        if (this.casSecurityContext == null) {
            log.warn("Unable to find authenticated ICasSecurityContext");
        }
    }
}
