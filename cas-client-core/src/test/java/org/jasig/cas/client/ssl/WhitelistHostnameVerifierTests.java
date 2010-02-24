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
 * Unit test for {@link WhitelistHostnameVerifier} class.
 *
 * @author Middleware
 * @version $Revision$
 *
 */
public class WhitelistHostnameVerifierTests extends TestCase {
    /**
     * Test method for {@link WhitelistHostnameVerifier#verify(String, SSLSession)}.
     */
    public void testVerify() {
        final WhitelistHostnameVerifier verifier = new WhitelistHostnameVerifier(
                "red.vt.edu, green.vt.edu,blue.vt.edu");
        Assert.assertTrue(verifier.verify("red.vt.edu", null));
        Assert.assertTrue(verifier.verify("green.vt.edu", null));
        Assert.assertTrue(verifier.verify("blue.vt.edu", null));
        Assert.assertFalse(verifier.verify("purple.vt.edu", null));
    }

}
