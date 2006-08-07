package org.jasig.cas.client.integration.uportal;

import junit.framework.TestCase;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SimpleService;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Datet$
 * @since 3.0
 *
 */
public class ServiceHolderTests extends TestCase {

    public void testSetGetService() {
        final Service service = new SimpleService("test");
        ServiceHolder.setService(service);

        assertEquals(service, ServiceHolder.getService());
    }

    public void testClearContext() {
        final Service service = new SimpleService("test");
        ServiceHolder.setService(service);

        assertEquals(service, ServiceHolder.getService());

        ServiceHolder.clearContext();

        assertNull(ServiceHolder.getService());
    }
}
