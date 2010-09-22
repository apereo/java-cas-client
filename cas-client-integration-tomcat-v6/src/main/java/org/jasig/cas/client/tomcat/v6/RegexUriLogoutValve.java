/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.tomcat.v6;

import org.apache.catalina.LifecycleException;
import org.jasig.cas.client.tomcat.LogoutHandler;
import org.jasig.cas.client.tomcat.RegexUriLogoutHandler;

/**
 * Performs CAS logout when the request URI matches a regular expression.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @version $Revision$ $Date$
 * @since 3.1.12
 */
public final class RegexUriLogoutValve extends AbstractLogoutValve {
    private static final String NAME = RegexUriLogoutValve.class.getName();
    
    private RegexUriLogoutHandler logoutHandler = new RegexUriLogoutHandler();

    public void setRedirectUrl(final String redirectUrl) {
        this.logoutHandler.setRedirectUrl(redirectUrl);
    }

    public void setLogoutUriRegex(final String regex) {
        this.logoutHandler.setLogoutUriRegex(regex);
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
