package org.jasig.cas.client.integration.uportal;

import junit.framework.TestCase;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class UPortalConfiguredProxyReceptorServletTests extends TestCase {

    public void testInitialization() throws Exception {
        final UPortalConfiguredProxyReceptorServlet servlet = new UPortalConfiguredProxyReceptorServlet();
        servlet.init(new MockServletConfig(new MockServletContext()));
    }
}
