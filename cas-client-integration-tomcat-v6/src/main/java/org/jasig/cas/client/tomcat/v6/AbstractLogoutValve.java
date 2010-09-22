/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.tomcat.v6;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.jasig.cas.client.tomcat.LogoutHandler;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Abstract base class for Container-managed log out.  Removes the attributes
 * from the session.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @version $Revision$ $Date$
 * @since 3.1.12
 */
public abstract class AbstractLogoutValve extends AbstractLifecycleValve {
    public final void invoke(final Request request, final Response response) throws IOException, ServletException {
        if (getLogoutHandler().isLogoutRequest(request)) {
            getLogoutHandler().logout(request, response);
            // Do not proceed up valve chain
            return;
        } else {
            this.log.debug("URI is not a logout request: " + request.getRequestURI());
            getNext().invoke(request, response);
        }
    }
    
    protected abstract LogoutHandler getLogoutHandler();

}
