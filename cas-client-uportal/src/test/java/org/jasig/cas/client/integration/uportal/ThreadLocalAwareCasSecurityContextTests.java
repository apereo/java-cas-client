package org.jasig.cas.client.integration.uportal;

import junit.framework.TestCase;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SimpleService;

/**
 * @author Scott
 */
public final class ThreadLocalAwareCasSecurityContextTests extends TestCase {

    private ThreadLocalAwareCasSecurityContext context;


    protected void setUp() throws Exception {
        final ThreadLocalAwareCasSecurityContextFactory factory = new ThreadLocalAwareCasSecurityContextFactory();
        this.context = (ThreadLocalAwareCasSecurityContext) factory.getSecurityContext();
    }

    public void testGetService() {
        final Service service = new SimpleService("test");
        ServiceHolder.setService(service);

        assertEquals(service, this.context.getService());

        ServiceHolder.clearContext();
    }
}
