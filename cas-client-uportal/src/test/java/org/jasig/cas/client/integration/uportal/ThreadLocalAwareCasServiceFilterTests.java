package org.jasig.cas.client.integration.uportal;

import junit.framework.TestCase;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.jasig.cas.authentication.principal.SimpleService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class ThreadLocalAwareCasServiceFilterTests extends TestCase {

    private ThreadLocalAwareCasServiceFilter filter;


    protected void setUp() throws Exception {
        this.filter = new ThreadLocalAwareCasServiceFilter();
        this.filter.setServiceUrl("http://localhost");
        this.filter.init();
    }

    public void testServiceSetter() throws IOException, ServletException {
        final MockHttpServletRequest request = new MockHttpServletRequest();

        request.setParameter("ticket", "test");
        this.filter.doFilter(request, new MockHttpServletResponse(), new FilterChain() {

            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException, ServletException {
                assertNotNull(ServiceHolder.getService());
                assertEquals(new SimpleService("http://localhost"), ServiceHolder.getService());
            }
        });
    }

    public void testNoServiceSetter() throws IOException, ServletException {
        final MockHttpServletRequest request = new MockHttpServletRequest();

        this.filter.doFilter(request, new MockHttpServletResponse(), new FilterChain() {

            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException, ServletException {
                assertNull(ServiceHolder.getService());
            }
        });
    }
}
