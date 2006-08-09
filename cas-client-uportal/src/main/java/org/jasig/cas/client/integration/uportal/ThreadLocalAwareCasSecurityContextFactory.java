/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.client.integration.uportal;

import org.jasig.portal.security.ISecurityContext;

/**
 * Factory to instantiate ThreadLocalAwareCasSecurityContexts.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class ThreadLocalAwareCasSecurityContextFactory extends
        AbstractCasSecurityContextFactory {

    public ISecurityContext getSecurityContext() {
        return new ThreadLocalAwareCasSecurityContext(this.ticketValidator,
                this.service, this.proxyRetriever);
    }
}
