package org.jasig.cas.client.cleanup;

import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import junit.framework.TestCase;

import org.jasig.cas.client.util.MethodFlag;
import org.springframework.mock.web.MockServletContext;

/**
 * Unit test for {@link CleanUpListener}
 * 
 * @author Brad Cupit (brad [at] lsu {dot} edu)
 */
public class CleanUpListenerTest extends TestCase {
    private final CleanUpRegistry defaultCleanUpRegistry = CleanUpRegistryImpl.getInstance();
    private final Timer defaultTimer = new Timer(true);
    
    public void testStartsThreadAtStartup() throws Exception {
        final MethodFlag scheduleMethodFlag = new MethodFlag();
        
        final Timer timer = new Timer(true) {
            public void schedule(TimerTask task, long delay, long period) {
                scheduleMethodFlag.setCalled();
            }
        };
        
        final CleanUpListener cleanUpListener = new CleanUpListener(timer, defaultCleanUpRegistry);
        cleanUpListener.contextInitialized(new TestServletContextEvent(1));
        
        assertTrue(scheduleMethodFlag.wasCalled());
    }
    
    public void testShutsDownTimerThread() throws Exception {
        final MethodFlag cancelMethodFlag = new MethodFlag();
        
        final Timer timer = new Timer(true) {
            public void cancel() {
                cancelMethodFlag.setCalled();
                super.cancel();
            }
        };
        
        final CleanUpListener cleanUpListener = new CleanUpListener(timer, defaultCleanUpRegistry);
        cleanUpListener.contextInitialized(new TestServletContextEvent(1));
        cleanUpListener.contextDestroyed(null);
        
        assertTrue(cancelMethodFlag.wasCalled());
    }
    
    public void testCallsCleanAllOnSchedule() throws Exception {
        final MethodFlag cleanAllMethodFlag = new MethodFlag();
        
        final CleanUpRegistry cleanUpRegistry = new CleanUpRegistry() {
            public void addCleanble(Cleanable cleanable) {
            }

            public void cleanAll() {
                cleanAllMethodFlag.setCalled();
            }
        };
        
        long millisBetweenCleanUps = 250;
        
        final CleanUpListener cleanUpListener = new CleanUpListener(defaultTimer, cleanUpRegistry);
        cleanUpListener.contextInitialized(new TestServletContextEvent(millisBetweenCleanUps));
        
        // wait long enough for the clean up to occur
        Thread.sleep(millisBetweenCleanUps * 2);
        
        assertTrue(cleanAllMethodFlag.wasCalled()); 
    }
    
    public void testDelaysFirstCleanAll() throws Exception {
        final MethodFlag cleanAllMethodFlag = new MethodFlag();
        
        final CleanUpRegistry cleanUpRegistry = new CleanUpRegistry() {
            public void addCleanble(Cleanable cleanable) {
            }
            
            public void cleanAll() {
                cleanAllMethodFlag.setCalled();
            }
        };
        
        long millisBetweenCleanUps = 250;
        
        final CleanUpListener cleanUpListener = new CleanUpListener(defaultTimer, cleanUpRegistry);
        cleanUpListener.contextInitialized(new TestServletContextEvent(millisBetweenCleanUps));
        
        assertFalse(cleanAllMethodFlag.wasCalled());
        
        // wait long enough for the clean up to occur
        Thread.sleep(millisBetweenCleanUps * 2);
        
        assertTrue(cleanAllMethodFlag.wasCalled()); 
    }
    
    public void testReturnsDefaultWhenNoContextParamConfigured() throws Exception {
        final ServletContext servletContext = new MockServletContext();
        
        long millisBetweenCleanups = new CleanUpListener().getMillisBetweenCleanups(servletContext);
        assertEquals(CleanUpListener.DEFAULT_MILLIS_BETWEEN_CLEANUPS, millisBetweenCleanups);
    }
    
    public void testFailsWithInvalidNumber() throws Exception {
        final ServletContext servletContext = new MockServletContext() {
            public String getInitParameter(String name) {
                if (name.equals(CleanUpListener.MILLIS_BETWEEN_CLEANUPS_INIT_PARAM)) {
                    return "not a number";
                } else {
                    return null;
                }
            }
        };
        
        try {
            new CleanUpListener().getMillisBetweenCleanups(servletContext);
            fail("expected an exception");
        } catch (RuntimeException e) {
            // expected, test passes
        }
    }
    
    /**
     * A unit test helper class to mock a real {@link ServletContextEvent}
     * 
     * @author Brad Cupit (brad [at] lsu {dot} edu)
     */
    private static final class TestServletContextEvent extends ServletContextEvent {
        private TestServletContextEvent(long millisBetweenCleanUps) {
            super(new TestServletContext(millisBetweenCleanUps));
        }
    }
    
    /**
     * A unit test helper class to mock a real {@link ServletContext}
     * 
     * @author Brad Cupit (brad [at] lsu {dot} edu)
     */
    private static final class TestServletContext extends MockServletContext {
        private final long millisBetweenCleanUps;

        public TestServletContext(long millisBetweenCleanUps) {
            this.millisBetweenCleanUps = millisBetweenCleanUps;
        }

        public String getInitParameter(String name) {
            if (name.equals(CleanUpListener.MILLIS_BETWEEN_CLEANUPS_INIT_PARAM)) {
                return Long.toString(millisBetweenCleanUps);
            } else {
                throw new RuntimeException("Unexpected init param requested: " + name);
            }
        }
    }
}
