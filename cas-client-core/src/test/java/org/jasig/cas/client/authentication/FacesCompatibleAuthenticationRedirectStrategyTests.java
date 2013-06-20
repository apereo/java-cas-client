package org.jasig.cas.client.authentication;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.Assert.*;

public class FacesCompatibleAuthenticationRedirectStrategyTests {

    private FacesCompatibleAuthenticationRedirectStrategy strategy;

    @Before
    public void setUp() throws Exception {
        this.strategy = new FacesCompatibleAuthenticationRedirectStrategy();
    }

    @Test
    public void didWeRedirect() throws Exception {
        final String redirectUrl = "http://www.jasig.org";
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();

        this.strategy.redirect(request, response, redirectUrl);
        assertEquals(redirectUrl, response.getRedirectedUrl());
    }

    @Test
    public void facesPartialResponse() throws Exception {
        final String redirectUrl = "http://www.jasig.org";
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        request.setParameter("javax.faces.partial.ajax", "true");
        this.strategy.redirect(request, response, redirectUrl);
        assertNull(response.getRedirectedUrl());
        assertTrue(response.getContentAsString().contains(redirectUrl));
    }
}
