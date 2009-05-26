package org.jasig.cas.client.validation;

import junit.framework.TestCase;

import java.util.Timer;
import java.util.TimerTask;

import org.jasig.cas.client.proxy.CleanUpTimerTask;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorage;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorageImpl;
import org.jasig.cas.client.util.MethodFlag;

/**
 * Unit test for {@link Cas20ProxyReceivingTicketValidationFilter}
 * 
 * @author Brad Cupit (brad [at] lsu {dot} edu)
 */
public class Cas20ProxyReceivingTicketValidationFilterTest extends TestCase {

    private final Timer defaultTimer = new Timer(true);

    private final ProxyGrantingTicketStorage storage = new ProxyGrantingTicketStorageImpl();

    private final CleanUpTimerTask defaultTimerTask = new CleanUpTimerTask(storage);

    public void testStartsThreadAtStartup() throws Exception {
        final MethodFlag scheduleMethodFlag = new MethodFlag();
        final Cas20ProxyReceivingTicketValidationFilter filter = newCas20ProxyReceivingTicketValidationFilter();

        final Timer timer = new Timer(true) {
            public void schedule(TimerTask task, long delay, long period) {
                scheduleMethodFlag.setCalled();
            }
        };

        filter.setMillisBetweenCleanUps(1);
        filter.setProxyGrantingTicketStorage(storage);
        filter.setTimer(timer);
        filter.setTimerTask(defaultTimerTask);

        filter.init();
        assertTrue(scheduleMethodFlag.wasCalled());
    }

    public void testShutsDownTimerThread() throws Exception {
        final MethodFlag cancelMethodFlag = new MethodFlag();
        final Cas20ProxyReceivingTicketValidationFilter filter = newCas20ProxyReceivingTicketValidationFilter();

        final Timer timer = new Timer(true) {
            public void cancel() {
                cancelMethodFlag.setCalled();
                super.cancel();
            }
        };

        filter.setProxyGrantingTicketStorage(storage);
        filter.setMillisBetweenCleanUps(1);
        filter.setTimer(timer);
        filter.setTimerTask(defaultTimerTask);
        filter.init();
        filter.destroy();
        
        assertTrue(cancelMethodFlag.wasCalled());
    }

public void testCallsCleanAllOnSchedule() throws Exception {
        final MethodFlag timerTaskFlag = new MethodFlag();
        final Cas20ProxyReceivingTicketValidationFilter filter = newCas20ProxyReceivingTicketValidationFilter();

        final TimerTask timerTask = new TimerTask() {
            public void run() {
                timerTaskFlag.setCalled();
            }
        };

        final int millisBetweenCleanUps = 250;
        filter.setProxyGrantingTicketStorage(storage);
        filter.setTimerTask(timerTask);
        filter.setTimer(defaultTimer);
        filter.setMillisBetweenCleanUps(millisBetweenCleanUps);

        filter.init();

        // wait long enough for the clean up to occur
        Thread.sleep(millisBetweenCleanUps * 2);

        assertTrue(timerTaskFlag.wasCalled());
        filter.destroy();
    }

    public void testDelaysFirstCleanAll() throws Exception {
        final MethodFlag timerTaskFlag = new MethodFlag();
        final Cas20ProxyReceivingTicketValidationFilter filter = newCas20ProxyReceivingTicketValidationFilter();

        final TimerTask timerTask = new TimerTask() {
            public void run() {
                timerTaskFlag.setCalled();
            }
        };

        final int millisBetweenCleanUps = 250;
        filter.setProxyGrantingTicketStorage(storage);
        filter.setMillisBetweenCleanUps(millisBetweenCleanUps);
        filter.setTimer(defaultTimer);
        filter.setTimerTask(timerTask);

        filter.init();

        assertFalse(timerTaskFlag.wasCalled());

        // wait long enough for the clean up to occur
        Thread.sleep(millisBetweenCleanUps * 2);

        assertTrue(timerTaskFlag.wasCalled());

        filter.destroy();
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
