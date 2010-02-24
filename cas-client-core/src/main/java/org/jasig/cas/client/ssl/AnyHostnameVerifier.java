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
 * Hostname verifier that performs no host name verification for an SSL peer
 * such that all hosts are allowed.
 *
 * @author Middleware
 * @version $Revision$
 *
 */
public class AnyHostnameVerifier implements HostnameVerifier {

    /** {@inheritDoc} */
    public boolean verify(final String hostname, final SSLSession session) {
        return true;
    }

}
