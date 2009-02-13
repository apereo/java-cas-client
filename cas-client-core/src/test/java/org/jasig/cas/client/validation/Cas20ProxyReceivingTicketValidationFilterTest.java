package org.jasig.cas.client.validation;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.jasig.cas.client.cleanup.CleanUpRegistry;
import org.jasig.cas.client.cleanup.Cleanable;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorageImpl;

/**
 * Unit test for {@link Cas20ProxyReceivingTicketValidationFilter}
 * 
 * @author Brad Cupit (brad [at] lsu {dot} edu)
 */
public class Cas20ProxyReceivingTicketValidationFilterTest extends TestCase {
    public void testRegistersPGTicketStorageWithCleanUpRegistry() throws Exception {
        final TestCleanUpRegistry cleanUpRegistry = new TestCleanUpRegistry();
        
        final Cas20ProxyReceivingTicketValidationFilter filter = newCas20ProxyReceivingTicketValidationFilter();
        filter.setCleanUpRegistry(cleanUpRegistry);
        filter.init();
        
        assertEquals(1, cleanUpRegistry.getCleanables().size());
        assertSame(ProxyGrantingTicketStorageImpl.class, cleanUpRegistry.getCleanables().get(0).getClass());
    }

    private Cas20ProxyReceivingTicketValidationFilter newCas20ProxyReceivingTicketValidationFilter() {
        final Cas20ProxyReceivingTicketValidationFilter filter = new Cas20ProxyReceivingTicketValidationFilter();
        filter.setServerName("localhost");
        filter.setTicketValidator(new Cas20ProxyTicketValidator(""));
        
        return filter;
    }
    
    /**
     * A test implementation of {@link CleanUpRegistry} that allows us to see
     * which {@link Cleanable}s were registered.
     * 
     * @author Brad Cupit (brad [at] lsu {dot} edu)
     */
    private static final class TestCleanUpRegistry implements CleanUpRegistry {
        private final List cleanables = new ArrayList();
        
        public void addCleanble(Cleanable cleanable) {
            cleanables.add(cleanable);
        }
        
        public void cleanAll() {
        }
        
        public List getCleanables() {
            return cleanables;
        }
    };
}
