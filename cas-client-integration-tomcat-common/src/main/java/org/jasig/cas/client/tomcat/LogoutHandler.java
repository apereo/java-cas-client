/*
 * Copyright 2010 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.tomcat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Strategy pattern interface for ending a CAS authentication session.
 *
 * @author Marvin S. Addison
 * @version $Revision$
 *
 */
public interface LogoutHandler {
    /**
     * Determines whether the given request is a logout request.
     *
     * @param request HTTP request.
     *
     * @return True if request is a logout request, false otherwise.
     */
    boolean isLogoutRequest(HttpServletRequest request);

    /**
     * Ends the current authenticated user session bound to the given request.
     * The response is provided to allow the handler to customize the response
     * behavior on logout as needed.
     * 
     * @param request HTTP request.
     * @param response HTTP response.
     */
    void logout(HttpServletRequest request, HttpServletResponse response);
}
