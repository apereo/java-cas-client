/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public final class DefaultGatewayResolverImpl implements GatewayResolver {
	
    public static final String CONST_CAS_GATEWAY = "_const_cas_gateway_";

	public boolean hasGatewayedAlready(final HttpServletRequest request,
			final String serviceUrl) {
		final HttpSession session = request.getSession(false);
		
		if (session == null) {
			return false;
		}
		
		final boolean result = session.getAttribute(CONST_CAS_GATEWAY) != null;
		session.removeAttribute(CONST_CAS_GATEWAY);
		return result;
	}

	public String storeGatewayInformation(final HttpServletRequest request,
			final String serviceUrl) {
		request.getSession(true).setAttribute(CONST_CAS_GATEWAY, "yes");
		return serviceUrl;
	}
}
