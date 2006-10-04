/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.web.filter;

import junit.framework.TestCase;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.client.authorization.AuthorizationException;
import org.jasig.cas.client.authorization.AuthorizedDecider;
import org.jasig.cas.client.validation.AssertionImpl;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * Tests for the AuthorizationFilter.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class AuthorizationFilterTests extends TestCase {

    private AuthorizationFilter casAuthorizationFilter;

    protected void setUp() throws Exception {
        this.casAuthorizationFilter = new AuthorizationFilter(new AuthorizedDecider() {

            public boolean isAuthorizedToUseApplication(Principal principal) {
                return principal.getId().equals("scott");
            }
        });

        this.casAuthorizationFilter.init(new MockFilterConfig());
    }

    protected void tearDown() throws Exception {
        this.casAuthorizationFilter.destroy();
    }

    public void testSuccesfulAuthorization() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockHttpSession session = new MockHttpSession();
        request.setSession(session);

        session.setAttribute(AbstractCasFilter.CONST_ASSERTION,
                new AssertionImpl(new SimplePrincipal("scott")));

        try {
            this.casAuthorizationFilter.doFilter(request, response,
                    new FilterChain() {

                        public void doFilter(ServletRequest arg0,
                                             ServletResponse arg1) throws IOException,
                                ServletException {
                            // nothing to do
                        }
                    });
        } catch (Exception e) {
            fail("No exception expected");
        }
    }

    public void testFailedAuthorization() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockHttpSession session = new MockHttpSession();
        request.setSession(session);

        session.setAttribute(AbstractCasFilter.CONST_ASSERTION,
                new AssertionImpl(new SimplePrincipal("test")));

        try {
            this.casAuthorizationFilter.doFilter(request, response, null);
            fail("ServletException expected.");
        } catch (AuthorizationException e) {
            // expectd
        } catch (Exception e) {
            fail("AuthorizationException expected, not IOException.");
        }
    }

    public void testNoAssertionFound() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockHttpSession session = new MockHttpSession();
        request.setSession(session);

        try {
            this.casAuthorizationFilter.doFilter(request, response, null);
            fail("ServletException expected.");
        } catch (Exception e) {
            // expected
        }
    }
}
