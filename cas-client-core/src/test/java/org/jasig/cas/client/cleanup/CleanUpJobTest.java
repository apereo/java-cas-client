package org.jasig.cas.client.cleanup;

import org.jasig.cas.client.util.MethodFlag;

import junit.framework.TestCase;

/**
 * Unit test for {@link CleanUpJob}
 * 
 * @author Brad Cupit (brad [at] lsu {dot} edu)
 */
public class CleanUpJobTest extends TestCase {
    public void testExecuteInternal() throws Exception {
        final MethodFlag cleanAllMethodFlag = new MethodFlag();
        
        CleanUpRegistry localCleanUpRegistry = new CleanUpRegistry() {
            public void addCleanble(Cleanable cleanable) {
            }

            public void cleanAll() {
                cleanAllMethodFlag.setCalled();
            }
        };
        
        final CleanUpJob cleanUpJob = new CleanUpJob(localCleanUpRegistry);
        cleanUpJob.executeInternal(null);
        
        assertTrue(cleanAllMethodFlag.wasCalled());
    }
}
