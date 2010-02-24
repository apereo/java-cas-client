/*
  $Id$

  Copyright (C) 2008-2009 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware
  Email:   middleware@vt.edu
  Version: $Revision$
  Updated: $Date$
*/
package org.jasig.cas.client.ssl;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * Verifies a SSL peer host name based on an explicit whitelist of allowed hosts.
 *
 * @author Middleware
 * @version $Revision$
 *
 */
public class WhitelistHostnameVerifier implements HostnameVerifier {
    /** Allowed hosts */
    private String[] allowedHosts;


    /**
     * Creates a new instance using the given array of allowed hosts.
     * 
     * @param allowed Array of allowed hosts.
     */
    public WhitelistHostnameVerifier(final String[] allowed) {
        this.allowedHosts = allowed;
    }


    /**
     * Creates a new instance using the given list of allowed hosts.
     * 
     * @param allowedList Comma-separated list of allowed hosts.
     */
    public WhitelistHostnameVerifier(final String allowedList) {
        this.allowedHosts = allowedList.split(",\\s*");
    }


    /** {@inheritDoc} */
    public boolean verify(final String hostname, final SSLSession session) {
        for (int i = 0; i < this.allowedHosts.length; i++) {
            if (hostname.equalsIgnoreCase(this.allowedHosts[i])) {
                return true;
            }
        }
        return false;
    }

}
