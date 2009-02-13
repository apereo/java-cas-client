package org.jasig.cas.client.util;

/**
 * A mutable boolean-like flag for unit tests which use
 * anonymous classes.
 * <p>
 * A simple boolean would be ideal, except Java requires us
 * to mark enclosing local variables as final.
 * 
 * @author Brad Cupit (brad [at] lsu {dot} edu)
 */
public class MethodFlag {
    boolean called = false;

    public boolean wasCalled() {
        return called;
    }
    
    public void setCalled() {
        called = true;
    }
}
