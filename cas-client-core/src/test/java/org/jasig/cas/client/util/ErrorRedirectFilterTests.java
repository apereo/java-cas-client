package org.jasig.cas.client.util;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;

import static org.junit.Assert.*;

public final class ErrorRedirectFilterTests {

    private static final String REDIRECT_URL = "/ise.html";

    private ErrorRedirectFilter errorRedirectFilter;

    private FilterChain filterChain;


    @Before
    public void setUp() throws Exception {
        this.errorRedirectFilter = new ErrorRedirectFilter();

        final MockFilterConfig filterConfig = new MockFilterConfig();
        filterConfig.addInitParameter(IllegalStateException.class.getName(), REDIRECT_URL);
        this.errorRedirectFilter.init(filterConfig);
        this.filterChain = new MockFilterChain();
    }


    @Test
    public void noRootCause() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();

        // this should be okay as the mock filter chain allows one call
        this.errorRedirectFilter.doFilter(request, response, this.filterChain);

        // this will fail as the mock filter chain will throw IllegalStateException
        this.errorRedirectFilter.doFilter(request, response, this.filterChain);

        assertEquals(REDIRECT_URL, response.getRedirectedUrl());

    }
}
