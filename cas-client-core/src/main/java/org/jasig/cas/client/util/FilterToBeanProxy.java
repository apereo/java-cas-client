/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.client.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.*;
import java.io.IOException;

/**
 * Utility class to retrieve a Filter from a Spring-managed configuration file.
 * Based on the FilterToBeanProxy class in Acegi Security (but simplified)
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class FilterToBeanProxy implements Filter {

    /**
     * The filter we are proxying.
     */
    private Filter filter;

    /**
     * Instance of commons logging.
     */
    protected final Log log = LogFactory.getLog(this.getClass());

    public final void destroy() {
        // nothing to do
    }

    public final void doFilter(final ServletRequest request,
                               final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        this.filter.doFilter(request, response, chain);
    }

    public final void init(final FilterConfig filterConfig)
            throws ServletException {
        final String targetBean = filterConfig.getInitParameter("targetBean");

        if (CommonUtils.isBlank(targetBean)) {
            throw new ServletException(
                    "init-parameter missing: targetBean is required.");
        }

        final ApplicationContext ctx = this.getContext(filterConfig);

        if (!ctx.containsBean(targetBean)) {
            throw new ServletException("targetBean '" + targetBean
                    + "' not found in context");
        }

        log.trace("Loading filter from Spring ApplicationContext with name: " + targetBean);
        this.filter = (Filter) ctx.getBean(targetBean, Filter.class);
    }

    protected ApplicationContext getContext(FilterConfig filterConfig) {
        return WebApplicationContextUtils
                .getRequiredWebApplicationContext(filterConfig.getServletContext());
    }
}
