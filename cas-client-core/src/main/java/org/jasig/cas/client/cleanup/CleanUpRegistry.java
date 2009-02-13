package org.jasig.cas.client.cleanup;


/**
 * A central location for all {@link Cleanable}s to register themselves.
 * Then a thread or timer can occasionally call {@link #cleanAll()} to
 * run the {@link Cleanable#cleanUp()} methods.
 * <p>
 * See {@link CleanUpListener} or {@link CleanUpJob} for two implementations
 * of a timer which calls {@link #cleanAll()}. Either implementation will
 * work, though you only need to choose one.
 * 
 * @author Brad Cupit (brad [at] lsu {dot} edu)
 */
public interface CleanUpRegistry {
    /**
     * Adds a {@link Cleanable} to the list of objects whose
     * {@link Cleanable#cleanUp()} method will be called.
     * 
     * @param cleanable the {@link Cleanable} to add to the list. If a
     * {@link Cleanable} is added twice, it's {@link Cleanable#cleanUp()}
     * method will be called twice each time the timer goes off
     * (this is probably not the desired outcome).
     */
    public void addCleanble(Cleanable cleanable);

    /**
     * Runs {@link Cleanable#cleanUp()} on each {@link Cleanable}
     * passed in to {@link #addCleanble(Cleanable)}
     */
    public void cleanAll();
}
