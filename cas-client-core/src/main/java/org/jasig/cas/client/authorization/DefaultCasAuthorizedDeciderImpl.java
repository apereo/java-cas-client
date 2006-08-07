/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.client.authorization;

import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.client.util.CommonUtils;

import java.util.List;

/**
 * Default implementation of the CasAuthorizedDecider that delegates to a list
 * to check if someone is authorized.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class DefaultCasAuthorizedDeciderImpl implements
    CasAuthorizedDecider {

    /** The list of users authorized to use the system. */
    private List users;

    public boolean isAuthorizedToUseApplication(final Principal principal) {
        return this.users.contains(principal.getId());
    }

    public void init() {
        CommonUtils.assertNotEmpty(this.users, "users cannot be empty.");
    }

    public void setUsers(final List users) {
        this.users = users;
    }
}
