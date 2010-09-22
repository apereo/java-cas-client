/*
 * Copyright 2010 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.tomcat;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.client.util.CommonUtils;

/**
 * Performs CAS logout when the request URI matches a fixed context-relative
 * URI.
 *
 * @author Marvin S. Addison
 * @version $Revision$
 *
 */
public class StaticUriLogoutHandler extends AbstractLogoutHandler {
    private String logoutUri;

    /**
     * The logout URI to watch for logout requests.
     *
     * @param logoutUri  Logout URI.  CANNOT be null.  MUST be relative and start with "/"
     */
    public void setLogoutUri(final String logoutUri) {
        this.logoutUri = logoutUri;
    }

    /**
     * Initalializes the component for use.
     */
    public void init() {
        CommonUtils.assertNotNull(this.logoutUri, "logoutUri cannot be null.");
        CommonUtils.assertTrue(this.logoutUri.startsWith("/"), "logoutUri must start with \"/\"");
    }

    /** {@inheritDoc} */
    public boolean isLogoutRequest(final HttpServletRequest request) {
        return this.logoutUri.equals(request.getRequestURI());
    }

}
