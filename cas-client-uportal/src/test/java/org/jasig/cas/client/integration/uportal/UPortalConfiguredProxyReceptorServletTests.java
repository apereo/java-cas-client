package org.jasig.cas.client.integration.uportal;

import junit.framework.TestCase;
import org.springframework.mock.web.MockServletContext;
import org.springframework.mock.web.MockServletConfig;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class UPortalConfiguredProxyReceptorServletTests extends TestCase {

    public void testInitialization() throws Exception {
        final UPortalConfiguredProxyReceptorServlet servlet = new UPortalConfiguredProxyReceptorServlet();
        servlet.init(new MockServletConfig(new MockServletContext()));
    }
}
