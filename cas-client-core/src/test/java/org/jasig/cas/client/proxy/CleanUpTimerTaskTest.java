package org.jasig.cas.client.proxy;

import java.util.TimerTask;

import org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter;

import junit.framework.TestCase;

/**
 * Unit test for the {@link CleanUpTimerTask}
 * 
 * @author Brad Cupit (brad [at] lsu {dot} edu)
 */
public class CleanUpTimerTaskTest extends TestCase {

    public void testRun() throws Exception {
        final ProxyGrantingTicketStorageTestImpl storage = new ProxyGrantingTicketStorageTestImpl();
        new Cas20ProxyReceivingTicketValidationFilter().setProxyGrantingTicketStorage(storage);

        final TimerTask timerTask = new CleanUpTimerTask(storage);

        timerTask.run();
        assertTrue(storage.cleanUpWasCalled());
    }
    
    /**
     * implementation of the storage interface used only for testing
     * 
     * @author Brad Cupit (brad [at] lsu {dot} edu)
     */
    private static final class ProxyGrantingTicketStorageTestImpl implements ProxyGrantingTicketStorage {
        private boolean cleanUpCalled = false;
        
        public boolean cleanUpWasCalled() {
            return cleanUpCalled;
        }
        
        public void cleanUp() {
            cleanUpCalled = true;
        }

        public String retrieve(String proxyGrantingTicketIou) {
            return null;
        }

        public void save(String proxyGrantingTicketIou, String proxyGrantingTicket) {
        }
    }
}
