package org.jasig.cas.client.authentication;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class DefaultAuthenticationRedirectStrategyTests {

    private DefaultAuthenticationRedirectStrategy strategy;

    @Before
    public void setUp() throws Exception {
        this.strategy = new DefaultAuthenticationRedirectStrategy();
    }

    @Test
    public void didWeRedirect() throws Exception {
        final String redirectUrl = "http://www.jasig.org";
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();

        this.strategy.redirect(request, response, redirectUrl);
        assertEquals(redirectUrl, response.getRedirectedUrl());
    }
}
