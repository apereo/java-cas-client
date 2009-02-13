package org.jasig.cas.client.cleanup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * An old-school singleton implementation of {@link CleanUpRegistry}.
 * This implementation does not require clients to add any extra Spring
 * configuration, hence the old-school singleton.
 *  
 * A thread or timer should occasionally call {@link #cleanAll()} to
 * run the {@link Cleanable#cleanUp()} method on each {@link Cleanable}.
 * 
 * @author Brad Cupit (brad [at] lsu {dot} edu)
 */
public final class CleanUpRegistryImpl implements CleanUpRegistry {
    private static CleanUpRegistryImpl cleanUpRegistry =  new CleanUpRegistryImpl();
    private List cleanables = Collections.synchronizedList(new ArrayList());

    private CleanUpRegistryImpl() {
    }
    
    public static CleanUpRegistryImpl getInstance() {
        return cleanUpRegistry;
    }
    
    /**
     * {@inheritDoc}
     */
    public void addCleanble(Cleanable cleanable) {
        cleanables.add(cleanable);
    }

    /**
     * {@inheritDoc}
     */
    public void cleanAll() {
        synchronized (cleanables) {
            for (Iterator iterator = cleanables.iterator(); iterator.hasNext();) {
                Cleanable cleanable = (Cleanable) iterator.next();
                cleanable.cleanUp();
            }
        }
    }
}
