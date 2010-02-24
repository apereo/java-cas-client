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

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Unit test for {@link RegexHostnameVerifier} class.
 *
 * @author Middleware
 * @version $Revision$
 *
 */
public class RegexHostnameVerifierTests extends TestCase {

    /**
     * Test method for {@link RegexHostnameVerifier#verify(String, SSLSession)}.
     */
    public void testVerify() {
        final RegexHostnameVerifier verifier = new RegexHostnameVerifier("\\w+\\.vt\\.edu");
        Assert.assertTrue(verifier.verify("a.vt.edu", null));
        Assert.assertTrue(verifier.verify("host.vt.edu", null));
        Assert.assertFalse(verifier.verify("1-host.vt.edu", null));
        Assert.assertFalse(verifier.verify("mallory.example.com", null));
    }

}
