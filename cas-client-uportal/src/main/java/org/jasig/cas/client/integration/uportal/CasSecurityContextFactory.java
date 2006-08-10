/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.client.integration.uportal;

import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.ISecurityContextFactory;

/**
 * Implementation of an {@link ISecurityContextFactory} that on creation will
 * retrieve references to Spring managed CAS client objects and pass them to all
 * new CasSecurityContexts that are created.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class CasSecurityContextFactory extends AbstractCasSecurityContextFactory {

    public CasSecurityContextFactory() {
        super();
    }

    /**
     * Instantiate a new instance of CasSecurityContext.
     *
     * @return a new ISecurityContext instance, specifically an instance of CasSecurityContext.
     */
    public ISecurityContext getSecurityContext() {
        log
                .trace("Returning CasSecurityContext from CasSecurityContextFactory.");
        return new CasSecurityContext(this.ticketValidator, this.service, this.proxyRetriever
        );
    }
}
