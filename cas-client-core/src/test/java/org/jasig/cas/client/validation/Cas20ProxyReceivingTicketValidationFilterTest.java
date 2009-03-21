package org.jasig.cas.client.validation;

import junit.framework.TestCase;

/**
 * Unit test for {@link Cas20ProxyReceivingTicketValidationFilter}
 * 
 * @author Brad Cupit (brad [at] lsu {dot} edu)
 */
public class Cas20ProxyReceivingTicketValidationFilterTest extends TestCase {
    public void testHasDefaultStorage() throws Exception {
        assertNotNull(Cas20ProxyReceivingTicketValidationFilter.getProxyGrantingTicketStorage());
    }
    
    public void testThrowsForNullStorage() throws Exception {
        Cas20ProxyReceivingTicketValidationFilter filter = newCas20ProxyReceivingTicketValidationFilter();
        filter.setProxyGrantingTicketStorage(null);
        
        try {
            filter.init();
            fail("expected an exception due to null ProxyGrantingTicketStorage");
        } catch (IllegalArgumentException exception) {
            // test passes
        }
    }
    
    /**
     * construct a working {@link Cas20ProxyReceivingTicketValidationFilter}
     */
    private Cas20ProxyReceivingTicketValidationFilter newCas20ProxyReceivingTicketValidationFilter() {
        final Cas20ProxyReceivingTicketValidationFilter filter = new Cas20ProxyReceivingTicketValidationFilter();
        filter.setServerName("localhost");
        filter.setTicketValidator(new Cas20ProxyTicketValidator(""));
        
        return filter;
    }
}
