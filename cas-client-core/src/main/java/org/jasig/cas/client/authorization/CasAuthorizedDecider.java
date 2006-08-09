/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.client.authorization;

import org.jasig.cas.authentication.principal.Principal;

/**
 * Simple interface for determining whether a Principal is authorized to use the
 * application or not.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public interface CasAuthorizedDecider {

    /**
     * Determines whether someone can use the system or not.
     *
     * @param principal the person we are checking
     * @return true if authorized, false otherwise.
     */
    boolean isAuthorizedToUseApplication(final Principal principal);
}
