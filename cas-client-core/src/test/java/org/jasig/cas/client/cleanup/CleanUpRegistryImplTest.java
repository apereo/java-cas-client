package org.jasig.cas.client.cleanup;

import org.jasig.cas.client.util.MethodFlag;

import junit.framework.TestCase;

/**
 * Unit test for {@link CleanUpRegistryImpl}
 * 
 * @author Brad Cupit (brad [at] lsu {dot} edu)
 */
public class CleanUpRegistryImplTest extends TestCase {
    private final CleanUpRegistry cleanUpRegistry = CleanUpRegistryImpl.getInstance(); 
    
    public void testCleanAll() throws Exception {
        final MethodFlag cleanUpMethodFlag = new MethodFlag();
        
        cleanUpRegistry.addCleanble(new Cleanable() {
            public void cleanUp() {
                cleanUpMethodFlag.setCalled();
            }
        });
        
        cleanUpRegistry.cleanAll();
        
        assertTrue(cleanUpMethodFlag.wasCalled());
    }
}
