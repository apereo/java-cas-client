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
import org.jasig.cas.client.Protocol;
import org.jasig.cas.client.PublicTestHttpServer;
import org.jasig.cas.client.ssl.HttpsURLConnectionFactory;
import org.junit.Ignore;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.net.URL;
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

    private static final PublicTestHttpServer server = PublicTestHttpServer.instance(8092);

    public void testRedirectUrlWithParam() {
        final String loginUrl = "http://localhost:8080/login?myName=foo";
        final String fullyConstructedUrl = CommonUtils.constructRedirectUrl(loginUrl, "foo", "foo", false, false);

        int count = 0;
        final char[] chars = fullyConstructedUrl.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '?') {
                count++;
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
        final String constructedUrl = CommonUtils.constructServiceUrl(request, response, null, "www.myserver.com",
            "service", "ticket", false);

        assertEquals(CONST_MY_URL, constructedUrl);
    }

    public void testConstructServiceUrlWithServerNameContainingPath() {
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/hello/hithere/");
        request.setScheme("https");
        request.setSecure(true);
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final String constructedUrl = CommonUtils.constructServiceUrl(request, response, null, "www.my.server.com/app",
            Protocol.CAS3.getServiceParameterName(), Protocol.CAS3.getArtifactParameterName(), false);

        assertEquals("https://www.my.server.com/app/hello/hithere/", constructedUrl);
    }

    public void testConstructServiceUrlWithServerNameContainingPathAndSchema() {
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/hello/hithere/");
        request.setScheme("https");
        request.setSecure(true);
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final String constructedUrl = CommonUtils.constructServiceUrl(request, response, null, "https://www.my.server.com/app",
            Protocol.CAS3.getServiceParameterName(), Protocol.CAS3.getArtifactParameterName(), false);

        assertEquals("https://www.my.server.com/app/hello/hithere/", constructedUrl);
    }

    public void testConstructServiceUrlWithParamsCas() {
        final String CONST_MY_URL = "https://www.myserver.com/hello/hithere/";
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/hello/hithere/");
        request.setScheme("https");
        request.setSecure(true);
        request.setQueryString("service=this&ticket=that&custom=custom");

        final MockHttpServletResponse response = new MockHttpServletResponse();
        final String constructedUrl = CommonUtils.constructServiceUrl(request, response, null, "www.myserver.com",
            Protocol.CAS3.getServiceParameterName(), Protocol.CAS3.getArtifactParameterName(), false);

        assertEquals("https://www.myserver.com/hello/hithere/?custom=custom", constructedUrl);
    }

    public void testConstructServiceUrlWithParamsCasAndServerNameWithSchema() {
        final String CONST_MY_URL = "https://www.myserver.com/hello/hithere/";
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/hello/hithere/");
        request.setScheme("https");
        request.setSecure(true);
        request.setQueryString("service=this&ticket=that&custom=custom");

        final MockHttpServletResponse response = new MockHttpServletResponse();
        final String constructedUrl = CommonUtils.constructServiceUrl(request, response, null, "https://www.myserver.com",
            Protocol.CAS3.getServiceParameterName(), Protocol.CAS3.getArtifactParameterName(), false);

        assertEquals("https://www.myserver.com/hello/hithere/?custom=custom", constructedUrl);
    }


    public void testConstructServiceUrlWithParamsSaml() {
        final String CONST_MY_URL = "https://www.myserver.com/hello/hithere/";
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/hello/hithere/");
        request.setScheme("https");
        request.setSecure(true);
        request.setQueryString("TARGET=this&SAMLart=that&custom=custom");

        final MockHttpServletResponse response = new MockHttpServletResponse();
        final String constructedUrl = CommonUtils.constructServiceUrl(request, response, null, "www.myserver.com",
            Protocol.SAML11.getServiceParameterName(), Protocol.SAML11.getArtifactParameterName(), false);

        assertEquals("https://www.myserver.com/hello/hithere/?custom=custom", constructedUrl);
    }

    public void testConstructServiceUrlWithEncodedParamsSaml() {
        final String CONST_MY_URL = "https://www.myserver.com/hello/hithere/";
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/hello/hithere/");
        request.setScheme("https");
        request.setSecure(true);
        request.setQueryString("TARGET%3Dthis%26SAMLart%3Dthat%26custom%3Dcustom");

        final MockHttpServletResponse response = new MockHttpServletResponse();
        final String constructedUrl = CommonUtils.constructServiceUrl(request, response, null, "www.myserver.com",
            Protocol.SAML11.getServiceParameterName(), Protocol.SAML11.getArtifactParameterName(), false);

        assertEquals("https://www.myserver.com/hello/hithere/?custom=custom", constructedUrl);
    }

    public void testConstructServiceUrlWithNoServiceParametersPassed() {
        final String CONST_MY_URL = "https://www.myserver.com/hello/hithere/";
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/hello/hithere/");
        request.setScheme("https");
        request.setSecure(true);
        request.setQueryString("TARGET=Test1&service=Test2&custom=custom");

        final MockHttpServletResponse response = new MockHttpServletResponse();
        final String constructedUrl = CommonUtils.constructServiceUrl(request, response, null, "www.myserver.com",
            Protocol.SAML11.getArtifactParameterName(), true);

        assertEquals("https://www.myserver.com/hello/hithere/?custom=custom", constructedUrl);
    }

    public void testConstructServiceUrlWithEncodedParams2Saml() {
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/hello/hithere/");
        request.setScheme("https");
        request.setSecure(true);
        request.setQueryString("TARGET%3Dthis%26SAMLart%3Dthat%26custom%3Dcustom%20value%20here%26another%3Dgood");

        final MockHttpServletResponse response = new MockHttpServletResponse();
        final String constructedUrl = CommonUtils.constructServiceUrl(request, response, null, "www.myserver.com",
            Protocol.SAML11.getServiceParameterName(), Protocol.SAML11.getArtifactParameterName(), true);

        assertEquals("https://www.myserver.com/hello/hithere/?custom=custom+value+here&another=good", constructedUrl);
    }

    public void testConstructServiceUrlWithoutEncodedParamsSamlAndNoEncoding() {
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/hello/hithere/");
        request.setScheme("https");
        request.setSecure(true);
        request.setQueryString("TARGET=this&SAMLart=that&custom=custom value here&another=good");

        final MockHttpServletResponse response = new MockHttpServletResponse();
        final String constructedUrl = CommonUtils.constructServiceUrl(request, response, null, "www.myserver.com",
            Protocol.SAML11.getServiceParameterName(), Protocol.SAML11.getArtifactParameterName(), false);

        assertEquals("https://www.myserver.com/hello/hithere/?custom=custom value here&another=good", constructedUrl);
    }

    public void testConstructServiceUrlWithEncodedParamsSamlAndNoEncoding() {
        final String CONST_MY_URL = "https://www.myserver.com/hello/hithere/";
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/hello/hithere/");
        request.setScheme("https");
        request.setSecure(true);
        request.setQueryString("TARGET=this&SAMLart=that&custom=custom+value+here&another=good");

        final MockHttpServletResponse response = new MockHttpServletResponse();
        final String constructedUrl = CommonUtils.constructServiceUrl(request, response, null, "www.myserver.com",
            Protocol.SAML11.getServiceParameterName(), Protocol.SAML11.getArtifactParameterName(), true);

        assertEquals("https://www.myserver.com/hello/hithere/?custom=custom+value+here&another=good", constructedUrl);
    }

    private void constructUrlNonStandardPortAndNoPortInConfigTest(final String serverNameList) {
        final String CONST_MY_URL = "https://www.myserver.com:555/hello/hithere/";
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/hello/hithere/");
        request.addHeader("Host", "www.myserver.com");
        request.setScheme("https");
        request.setSecure(true);
        request.setServerPort(555);
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final String constructedUrl = CommonUtils.constructServiceUrl(request, response, null,
            serverNameList, "service", "ticket", false);
        assertEquals(CONST_MY_URL, constructedUrl);
    }

    public void testConstructUrlNonStandardPortAndNoScheme() {
        constructUrlNonStandardPortAndNoPortInConfigTest("www.myserver.com");
    }

    public void testConstructUrlNonStandardPortAndScheme() {
        constructUrlNonStandardPortAndNoPortInConfigTest("https://www.myserver.com");
    }

    public void testConstructUrlWithMultipleHostsNoPortsOrProtocol() {
        final String CONST_MY_URL = "https://www.myserver.com/hello/hithere/";
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/hello/hithere/");
        request.addHeader("Host", "www.myserver.com");
        request.setScheme("https");
        request.setSecure(true);
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final String constructedUrl = CommonUtils.constructServiceUrl(request, response, null,
            "www.amazon.com www.bestbuy.com www.myserver.com", "service", "ticket", false);
        assertEquals(CONST_MY_URL, constructedUrl);
    }

    public void testConstructURlWithMultipleHostsAndPorts() {
        final String CONST_MY_URL = "https://www.myserver.com/hello/hithere/";
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/hello/hithere/");
        request.addHeader("Host", "www.myserver.com");
        request.setScheme("https");
        request.setSecure(true);
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final String constructedUrl = CommonUtils.constructServiceUrl(request, response, null,
            "http://www.amazon.com https://www.bestbuy.com https://www.myserver.com", "service", "ticket", false);
        assertEquals(CONST_MY_URL, constructedUrl);
    }

    public void testGetResponseFromServer() throws Exception {
        final String RESPONSE = "test1\r\ntest2";
        server.content = RESPONSE.getBytes(server.encoding);

        final String responsedContent = CommonUtils.getResponseFromServer(new URL("http://localhost:8092"), new HttpsURLConnectionFactory(), null);
        assertEquals(RESPONSE, responsedContent);
    }

    public void testUrlEncode() {
        assertEquals("this+is+a+very+special+parameter+with+%3D%25%2F",
            CommonUtils.urlEncode("this is a very special parameter with =%/"));
    }

    public void testUrlEncodeWithQueryParameters() {
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/idp/authN/ExtCas");
        request.setQueryString("conversation=e1s1&ticket=ST-1234-123456789-a&entityId=https://test.edu/sp?alias=1234-1234-1234-1234&something=else");
        request.addHeader("Host", "www.myserver.com");
        request.setScheme("https");
        request.setSecure(true);
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final String constructedUrl = CommonUtils.constructServiceUrl(request, response, null,
            "https://my.server.com",
            "service", "ticket", false);
        assertEquals("https://my.server.com/idp/authN/ExtCas?conversation=e1s1&entityId=https://test.edu/sp?alias=1234-1234-1234-1234&something=else",
            constructedUrl);
    }
}
