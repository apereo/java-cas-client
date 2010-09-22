/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.tomcat.v6;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Request;
import org.jasig.cas.client.util.CommonUtils;

/**
 * Monitors a specific url for logout requests.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1.12
 */
public final class UrlBasedLogoutValve extends AbstractLogoutValve {
    private static final String NAME = UrlBasedLogoutValve.class.getName();

    private String logoutUri;

    /**
     * The logout url to watch for logout requests.
     *
     * @param logoutUri  the url.  CANNOT be null.  MUST be relative and start with "/"
     */
    public void setLogoutUri(final String logoutUri) {
        this.logoutUri = logoutUri;
    }

    public void start() throws LifecycleException {
        super.start();
        try {
            CommonUtils.assertNotNull(this.logoutUri, "logoutUri cannot be null.");
            CommonUtils.assertTrue(this.logoutUri.startsWith("/"), "logoutUri must start with \"/\"");
        } catch (final IllegalArgumentException e) {
            throw new LifecycleException(e);
        }
        this.log.info("Startup completed.");
    }

    protected boolean isLogoutRequest(final Request request) {
        return this.logoutUri.equals(request.getRequestURI());
    }

    /** {@inheritDoc} */
    protected String getName() {
        return NAME;
    }
}
