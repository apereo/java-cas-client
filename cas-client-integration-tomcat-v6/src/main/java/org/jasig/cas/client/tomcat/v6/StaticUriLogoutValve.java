/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.tomcat.v6;

import org.apache.catalina.LifecycleException;
import org.jasig.cas.client.tomcat.LogoutHandler;
import org.jasig.cas.client.tomcat.StaticUriLogoutHandler;

/**
 * Monitors a specific request URI for logout requests.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @version $Revision$ $Date$
 * @since 3.1.12
 */
public final class StaticUriLogoutValve extends AbstractLogoutValve {
    private static final String NAME = StaticUriLogoutValve.class.getName();

    private StaticUriLogoutHandler logoutHandler = new StaticUriLogoutHandler();

    public void setRedirectUrl(final String redirectUrl) {
        this.logoutHandler.setRedirectUrl(redirectUrl);
    }
    
    public void setLogoutUri(final String logoutUri) {
        this.logoutHandler.setLogoutUri(logoutUri);
    }

    public void start() throws LifecycleException {
        super.start();
        this.logoutHandler.init();
        this.log.info("Startup completed.");
    }

    /** {@inheritDoc} */
    protected String getName() {
        return NAME;
    }

    /** {@inheritDoc} */
    protected LogoutHandler getLogoutHandler() {
        return logoutHandler;
    }
}
