/*
 * Copyright 2010 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.tomcat;

import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.client.util.CommonUtils;

/**
 * Performs CAS logout when the request URI matches a regular expression.
 *
 * @author Marvin S. Addison
 * @version $Revision$
 *
 */
public class RegexUriLogoutHandler extends AbstractLogoutHandler {
    private String logoutUriRegex;

    private Pattern logoutUriPattern;

    /**
     * @param regex Logout URI regular expression.  CANNOT be null.
     */
    public void setLogoutUriRegex(final String regex) {
        this.logoutUriRegex = regex;
    }

    /**
     * Initalializes the component for use.
     */
    public void init() {
        CommonUtils.assertNotNull(this.logoutUriRegex, "A logout URI regular expression is required.");
        this.logoutUriPattern = Pattern.compile(this.logoutUriRegex);
    }
    
    /** {@inheritDoc} */
    public boolean isLogoutRequest(final HttpServletRequest request) {
        return this.logoutUriPattern.matcher(request.getRequestURI()).matches();
    }
}
