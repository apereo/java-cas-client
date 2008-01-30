/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.util;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Tests for the CommonUtils.
 *
 * @author Scott Battaglia
 * @version $Revision: 11731 $ $Date: 2007-09-27 11:27:21 -0400 (Wed, 27 Sep 2007) $
 * @since 3.0
 */
public final class CommonUtilsTests extends TestCase {

    public void testAssertNotNull() {
        final String CONST_MESSAGE = "test";
        CommonUtils.assertNotNull(new Object(), CONST_MESSAGE);
        try {
            CommonUtils.assertNotNull(null, CONST_MESSAGE);
        } catch (IllegalArgumentException e) {
            assertEquals(CONST_MESSAGE, e.getMessage());
        }
    }

    public void testAssertNotEmpty() {
        final String CONST_MESSAGE = "test";
        final Collection c = new ArrayList();
        c.add(new Object());
        CommonUtils.assertNotEmpty(c, CONST_MESSAGE);
        try {
            CommonUtils.assertNotEmpty(new ArrayList(), CONST_MESSAGE);
        } catch (IllegalArgumentException e) {
            assertEquals(CONST_MESSAGE, e.getMessage());
        }

        try {
            CommonUtils.assertNotEmpty(null, CONST_MESSAGE);
        } catch (IllegalArgumentException e) {
            assertEquals(CONST_MESSAGE, e.getMessage());
        }
    }

    public void testAssertTrue() {
        final String CONST_MESSAGE = "test";
        CommonUtils.assertTrue(true, CONST_MESSAGE);
        try {
            CommonUtils.assertTrue(false, CONST_MESSAGE);
        } catch (IllegalArgumentException e) {
            assertEquals(CONST_MESSAGE, e.getMessage());
        }
    }

    public void testIsEmpty() {
        assertFalse(CommonUtils.isEmpty("test"));
        assertFalse(CommonUtils.isEmpty(" test"));
        assertTrue(CommonUtils.isEmpty(""));
        assertTrue(CommonUtils.isEmpty(null));
        assertFalse(CommonUtils.isEmpty("   "));
    }

    public void testIsNotEmpty() {
        assertTrue(CommonUtils.isNotEmpty("test"));
        assertTrue(CommonUtils.isNotEmpty(" test"));
        assertFalse(CommonUtils.isNotEmpty(""));
        assertFalse(CommonUtils.isNotEmpty(null));
        assertTrue(CommonUtils.isNotEmpty("   "));
    }

    public void testIsBlank() {
        assertFalse(CommonUtils.isBlank("test"));
        assertFalse(CommonUtils.isBlank(" test"));
        assertTrue(CommonUtils.isBlank(""));
        assertTrue(CommonUtils.isBlank(null));
        assertTrue(CommonUtils.isBlank("   "));
    }

    public void testIsNotBlank() {
        assertTrue(CommonUtils.isNotBlank("test"));
        assertTrue(CommonUtils.isNotBlank(" test"));
        assertFalse(CommonUtils.isNotBlank(""));
        assertFalse(CommonUtils.isNotBlank(null));
        assertFalse(CommonUtils.isNotBlank("   "));
    }
}
