/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.integration.uportal;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SimpleService;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.web.filter.AbstractCasFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter to construct the service url from the request and place it in a
 * ThreadLocal so its available to the
 * {@link ThreadLocalAwareCasSecurityContext} in order to use it for Ticket
 * validation.
 * <p>This filter places the Service in a {@link ServiceHolder}.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class ThreadLocalAwareCasServiceFilter extends AbstractCasFilter {

    public ThreadLocalAwareCasServiceFilter(final String service, final boolean isServerName) {
        super(service, isServerName);
    }

    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response, final FilterChain filterChain)
            throws IOException, ServletException {
        final boolean hasTicket = CommonUtils.isNotBlank(request
                .getParameter(getArgumentExtractor().getArtifactParameterName()));
        try {
            if (hasTicket) {
                final Service service = new SimpleService(constructServiceUrl(
                        request, response));
                ServiceHolder.setService(service);
            }

            filterChain.doFilter(request, response);
        } finally {
            if (hasTicket) {
                ServiceHolder.clearContext();
            }
        }
    }
}
