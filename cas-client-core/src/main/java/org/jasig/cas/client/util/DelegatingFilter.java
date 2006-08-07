/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.client.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A Delegating Filter looks up a parameter in the request object and matches
 * (either exact or using Regular Expressions) the value. If there is a match,
 * the associated filter is executed. Otherwise, the normal chain is executed.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class DelegatingFilter implements Filter {

    /** Instance of Commons Logging. */
    private Log log = LogFactory.getLog(this.getClass());

    /** The request parameter to look for in the Request object. */
    private String requestParameterName;

    /** The map of filters to delegate to and the criteria (as key). */
    private Map delegators = new HashMap();

    /** The default filter to use if there is no match. */
    private Filter defaultFilter;

    /**
     * Whether the key in the delegators map is an exact match or a regular
     * expression.
     */
    private boolean exactMatch = false;

    public void destroy() {
        // nothing to do here
    }

    public void doFilter(final ServletRequest request,
        final ServletResponse response, final FilterChain filterChain)
        throws IOException, ServletException {

        final String parameter = request
            .getParameter(this.requestParameterName);

        if (CommonUtils.isNotEmpty(parameter)) {
            for (final Iterator iter = this.delegators.keySet().iterator(); iter
                .hasNext();) {
                final String key = (String) iter.next();

                if ((parameter.equals(key) && this.exactMatch)
                    || (parameter.matches(key) && !this.exactMatch)) {
                    final Filter filter = (Filter) this.delegators.get(key);
                    if (log.isDebugEnabled()) {
                        log.debug("Match found for parameter ["
                            + this.requestParameterName + "] with value ["
                            + parameter + "]. Delegating to filter ["
                            + filter.getClass().getName() + "]");
                    }
                    filter.doFilter(request, response, filterChain);
                    return;
                }
            }
        }

        log.debug("No match found for parameter [" + this.requestParameterName
            + "] with value [" + parameter + "]");

        if (this.defaultFilter != null) {
            this.defaultFilter.doFilter(request, response, filterChain);
        } else {
            filterChain.doFilter(request, response);
        }
    }

    public void init(final FilterConfig filterConfig) throws ServletException {
        // nothing to do here.
    }

    public void init() {
        CommonUtils.assertNotNull(this.requestParameterName,
            "requestParameterName cannot be null.");
        CommonUtils.assertTrue(!this.delegators.isEmpty(),
            "delegators cannot be empty.");

        for (final Iterator iter = this.delegators.keySet().iterator(); iter
            .hasNext();) {
            final Object object = this.delegators.get(iter.next());

            if (!Filter.class.isAssignableFrom(object.getClass())) {
                throw new IllegalArgumentException(
                    "All value objects in the delegators map must be filters.");
            }
        }
    }

    /**
     * Sets the map of delegating filters.
     *
     * @param delegators the map of delegators to set.
     */
    public void setDelegators(final Map delegators) {
        this.delegators = delegators;
    }

    /**
     * Marks whether the value of the parameter needs to match exactly or not.
     * 
     * @param exactMatch the value of whether we need to match exactly or not.
     */
    public void setExactMatch(final boolean exactMatch) {
        this.exactMatch = exactMatch;
    }

    /**
     * Sets the name of the request parameter to monitor.
     * 
     * @param requestParameterName the name of the request parameter.
     */
    public void setRequestParameterName(final String requestParameterName) {
        this.requestParameterName = requestParameterName;
    }

    /**
     * Sets the default filter to use if there are no matches. This is optional
     * as the filter will just continue on the chain if there is no default.
     * 
     * @param defaultFilter the filter to use by default.
     */
    protected void setDefaultFilter(final Filter defaultFilter) {
        this.defaultFilter = defaultFilter;
    }

}
