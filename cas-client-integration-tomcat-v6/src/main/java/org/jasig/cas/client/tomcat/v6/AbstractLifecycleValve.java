/*
 * Copyright 2010 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.tomcat.v6;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.catalina.valves.ValveBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base <code>Valve</code> implementation for valves that need Catalina lifecycle
 * management, including {@link #start()} and {@link #stop()} methods.
 *
 * @author Marvin S. Addison
 * @version $Revision$
 *
 */
public abstract class AbstractLifecycleValve extends ValveBase implements Lifecycle {
    /** Logger instance */
    protected final Log log = LogFactory.getLog(getClass());
    
    /** Lifecycle listeners */
    private LifecycleSupport lifecycle = new LifecycleSupport(this);
    
    
    /** {@inheritDoc} */
    public void addLifecycleListener(final LifecycleListener listener) {
        lifecycle.addLifecycleListener(listener);
    }

    /** {@inheritDoc} */
    public LifecycleListener[] findLifecycleListeners() {
        return lifecycle.findLifecycleListeners();
    }

    /** {@inheritDoc} */
    public void removeLifecycleListener(final LifecycleListener listener) {
        lifecycle.removeLifecycleListener(listener);
    }

    /** {@inheritDoc} */
    public void start() throws LifecycleException {
        log.debug(getName() + " starting.");
    }

    /** {@inheritDoc} */
    public void stop() throws LifecycleException {
        log.debug(getName() + " stopping.");
    }

    /**
     * @return Descriptive valve name.
     */
    protected abstract String getName();
}
