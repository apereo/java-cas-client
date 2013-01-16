/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.client.util;

import junit.framework.TestCase;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

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

    public void testRedirectUrlWithParam() {
        final String loginUrl = "http://localhost:8080/login?myName=foo";
        final String fullyConstructedUrl = CommonUtils.constructRedirectUrl(loginUrl, "foo", "foo", false, false);

        int count = 0;
        final char[] chars = fullyConstructedUrl.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '?') {
                count ++;
            }
        }

        assertEquals(1, count);
    }

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
        final Collection<Object> c = new ArrayList<Object>();
        c.add(new Object());
        CommonUtils.assertNotEmpty(c, CONST_MESSAGE);
        try {
            CommonUtils.assertNotEmpty(new ArrayList<Object>(), CONST_MESSAGE);
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

    public void testConstructServiceUrlWithTrailingSlash() {
        final String CONST_MY_URL = "https://www.myserver.com/hello/hithere/";
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/hello/hithere/");
        request.setScheme("https");
        request.setSecure(true);
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final String constructedUrl = CommonUtils.constructServiceUrl(request, response, null, "www.myserver.com", "ticket", false);

        assertEquals(CONST_MY_URL, constructedUrl);
    }

    public void testConstructUrlWithMultipleHostsNoPortsOrProtocol() {
        final String CONST_MY_URL = "https://www.myserver.com/hello/hithere/";
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/hello/hithere/");
        request.addHeader("Host", "www.myserver.com");
        request.setScheme("https");
        request.setSecure(true);
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final String constructedUrl = CommonUtils.constructServiceUrl(request, response, null, "www.amazon.com www.bestbuy.com www.myserver.com", "ticket", false);
        assertEquals(CONST_MY_URL, constructedUrl);
    }

    public void testConstructURlWithMultipleHostsAndPorts() {
        final String CONST_MY_URL = "https://www.myserver.com/hello/hithere/";
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/hello/hithere/");
        request.addHeader("Host", "www.myserver.com");
        request.setScheme("https");
        request.setSecure(true);
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final String constructedUrl = CommonUtils.constructServiceUrl(request, response, null, "http://www.amazon.com https://www.bestbuy.com https://www.myserver.com", "ticket", false);
        assertEquals(CONST_MY_URL, constructedUrl);
    }
}
