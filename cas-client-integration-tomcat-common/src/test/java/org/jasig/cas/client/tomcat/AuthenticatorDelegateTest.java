/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.cas.client.tomcat;


import static java.net.URLEncoder.encode;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;


/**
 * Testing configuration with rules for service redirection. Scenario: server whose IP is 123.12.3.4, hosting an app
 * running on port 8080, and a CAS server running on 8090. Both apps (/app e /cas) behind reverse http proxy.
 * 
 * @author fabiowg
 */
public class AuthenticatorDelegateTest extends TestCase {

    private static final String APP_REQUEST_URI = "/app/Screen.action";

    private HttpServletRequest request;
    private HttpServletResponse response;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        this.request = mock(HttpServletRequest.class);

        this.response = mock(HttpServletResponse.class);
        when(this.response.encodeURL(anyString())).then(new Answer<String>() {

            public String answer(InvocationOnMock invocation) throws Throwable {
                return encode((String) invocation.getArguments()[0], "UTF-8");
            }
        });
    }

    public void testRedirectionNoRulesInternalUrl() throws IOException {
        AuthenticatorDelegate delegate = new AuthenticatorDelegate();
        delegate.setServerName("123.12.3.4:8080");
        delegate.setCasServerLoginUrl("https://123.12.3.4:8090/cas/login");
        delegate.setServiceParameterName("TARGET");

        String requestURL = "https://123.12.3.4:8080" + APP_REQUEST_URI;
        when(this.request.getRequestURL()).thenReturn(new StringBuffer(requestURL));
        when(this.request.getRequestURI()).thenReturn(APP_REQUEST_URI);

        delegate.authenticate(this.request, this.response);

        verify(this.response).sendRedirect("https://123.12.3.4:8090/cas/login?TARGET=" + encodeTwice(requestURL));
    }

    public void testRedirectionRulesPublicReverseProxy() throws IOException {
        AuthenticatorDelegate delegate = delegateWithRules();

        String requestURL = "https://abc.def.com.br" + APP_REQUEST_URI;
        when(this.request.getRequestURL()).thenReturn(new StringBuffer(requestURL));
        when(this.request.getRequestURI()).thenReturn(APP_REQUEST_URI);

        delegate.authenticate(this.request, this.response);

        verify(this.response).sendRedirect("https://abc.def.com.br/cas/login?TARGET=" + encodeTwice(requestURL));
    }

    public void testRedirectionRulesPrivateReverseProxy() throws IOException {
        AuthenticatorDelegate delegate = delegateWithRules();

        String requestURL = "https://123.12.3.4" + APP_REQUEST_URI;
        when(this.request.getRequestURL()).thenReturn(new StringBuffer(requestURL));
        when(this.request.getRequestURI()).thenReturn(APP_REQUEST_URI);

        delegate.authenticate(this.request, this.response);

        verify(this.response).sendRedirect("https://123.12.3.4/cas/login?TARGET=" + encodeTwice(requestURL));
    }

    public void testRedirectionRulesPrivateNoReverseProxy() throws IOException {
        AuthenticatorDelegate delegate = delegateWithRules();

        String requestURL = "https://123.12.3.4:8080" + APP_REQUEST_URI;
        when(this.request.getRequestURL()).thenReturn(new StringBuffer(requestURL));
        when(this.request.getRequestURI()).thenReturn(APP_REQUEST_URI);

        delegate.authenticate(this.request, this.response);

        verify(this.response).sendRedirect("https://123.12.3.4:8090/cas/login?TARGET=" + encodeTwice(requestURL));
    }

    public void testRedirectionRulesTunnel() throws IOException {
        AuthenticatorDelegate delegate = delegateWithRules();

        String requestURL = "https://localhost:8080" + APP_REQUEST_URI;
        when(this.request.getRequestURL()).thenReturn(new StringBuffer(requestURL));
        when(this.request.getRequestURI()).thenReturn(APP_REQUEST_URI);

        delegate.authenticate(this.request, this.response);

        verify(this.response).sendRedirect("https://localhost:8090/cas/login?TARGET=" + encodeTwice(requestURL));
    }

    public void testRedirectionRulesTunnel2() throws IOException {
        AuthenticatorDelegate delegate = delegateWithRules();

        String requestURL = "https://localhost:38080" + APP_REQUEST_URI;
        when(this.request.getRequestURL()).thenReturn(new StringBuffer(requestURL));
        when(this.request.getRequestURI()).thenReturn(APP_REQUEST_URI);

        delegate.authenticate(this.request, this.response);

        verify(this.response).sendRedirect("https://localhost:38090/cas/login?TARGET=" + encodeTwice(requestURL));
    }

    private static String encodeTwice(String requestURL) throws UnsupportedEncodingException {
        return encode(encode(requestURL, "UTF-8"), "UTF-8");
    }

    private static AuthenticatorDelegate delegateWithRules() {
        AuthenticatorDelegate delegate = new AuthenticatorDelegate();
        delegate.setServerName("https?://([^/]+).* $1");
        delegate.setCasServerLoginUrl("https?://([^:]+):(\\d?)8080.* http://$1:$28090/cas/login || https?://([^/]+).* http://$1/cas/login");
        delegate.setServiceParameterName("TARGET");
        return delegate;
    }

}
