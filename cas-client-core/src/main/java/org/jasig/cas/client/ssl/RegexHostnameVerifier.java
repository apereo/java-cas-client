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

import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * Validates an SSL peer's hostname using a regular expression that a candidate
 * host must match in order to be verified.
 *
 * @author Middleware
 * @version $Revision$
 *
 */
public class RegexHostnameVerifier implements HostnameVerifier {
    /** Allowed hostname pattern */
    private Pattern pattern;
    
    
    /**
     * Creates a new instance using the given regular expression.
     * 
     * @param regex Regular expression describing allowed hosts.
     */
    public RegexHostnameVerifier(final String regex) {
        this.pattern = Pattern.compile(regex);
    }


    /** {@inheritDoc} */
    public boolean verify(final String hostname, final SSLSession session) {
        return pattern.matcher(hostname).matches();
    }

}
