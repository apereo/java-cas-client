package org.jasig.cas.client.util;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.2.1
 */
public final class CasFilterTests {

    @Test
    public void serverName() {
        final String serverNameWithoutSlash = "http://www.cnn.com";
        final String serverNameWithSlash = "http://www.cnn.com/";

        final TestCasFilter testCasFilter = new TestCasFilter();
        testCasFilter.setServerName(serverNameWithoutSlash);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        request.setContextPath("/cas");
        request.setRequestURI("/cas/test");

        assertTrue(testCasFilter.constructServiceUrl(request, response).startsWith("http://www.cnn.com/cas/test"));

        testCasFilter.setServerName(serverNameWithSlash);
        assertTrue(testCasFilter.constructServiceUrl(request, response).startsWith("http://www.cnn.com/cas/test"));


    }

    private static class TestCasFilter extends AbstractCasFilter {
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            // nothing to do
        }
    }
}
