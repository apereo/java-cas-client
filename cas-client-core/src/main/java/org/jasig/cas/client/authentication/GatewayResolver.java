/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.authentication;

import javax.servlet.http.HttpServletRequest;

/**
 * Implementations of this should only have a default constructor if
 * you plan on constructing them via the web.xml.
 * 
 * @author Scott Battaglia
 * @version $Revision$
 * @since 1.0
 *
 */
public interface GatewayResolver {

	/**
	 * Determines if the request has  been gatewayed already.  Should also do gateway clean up.
	 * 
	 * @param request the Http Servlet Request
	 * @param serviceUrl the service url
	 * @return true if yes, false otherwise.
	 */
	boolean hasGatewayedAlready(HttpServletRequest request, String serviceUrl);
	
	/**
	 * Storage the request for gatewaying and return the service url, which can be modified.
	 * 
	 * @param request the HttpServletRequest.
	 * @param serviceUrl the service url
	 * @return the potentially modified service url to redirect to
	 */
	String storeGatewayInformation(HttpServletRequest request, String serviceUrl);
}
