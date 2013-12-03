/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
 * @version $Revision: 22071 $
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
