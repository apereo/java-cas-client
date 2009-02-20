package org.jasig.cas.client.proxy;

import junit.framework.TestCase;

/**
 * Unit test for {@link ProxyGrantingTicketStorageImpl}
 * 
 * @author Brad Cupit (brad [at] lsu {dot} edu)
 */
public class ProxyGrantingTicketStorageImplTest extends TestCase {
    public void testCleanUp() throws Exception {
        String proxyGrantingTicketIou = "proxyGrantingTicketIou";
        
        int timeout = 250;
        ProxyGrantingTicketStorageImpl storage = new ProxyGrantingTicketStorageImpl(timeout);
        storage.save(proxyGrantingTicketIou, "proxyGrantingTicket");
        
        // sleep long enough for the ticket to timeout
        Thread.sleep(timeout * 2);
        
        storage.cleanUp();
        
        assertNull(storage.retrieve(proxyGrantingTicketIou));
    }
}
