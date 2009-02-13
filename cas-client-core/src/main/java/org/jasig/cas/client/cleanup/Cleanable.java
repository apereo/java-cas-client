package org.jasig.cas.client.cleanup;

/**
 * A simple interface representing an object which needs regular cleaning.
 * 
 * @see CleanUpRegistry
 * 
 * @author Brad Cupit (brad [at] lsu {dot} edu)
 */
public interface Cleanable {
    /**
     * This method will be called on a regular basis
     * to perform internal clean up (for example: removing
     * old items from a cache).
     * <p>
     * Objects implementing this interface so they can be
     * registered with the {@link CleanUpRegistry}.
     */
    void cleanUp();
}
