/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.authorization;

import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.client.util.CommonUtils;

import java.util.List;

/**
 * Default implementation of the AuthorizedDecider that delegates to a list
 * to check if someone is authorized.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class DefaultAuthorizedDeciderImpl implements
        AuthorizedDecider {

    /**
     * The list of users authorized to use the system.
     */
    private final List users;

    /**
     * Constructor that takes the list of acceptable users as its parameters.
     *
     * @param users the list of acceptable users.
     */
    public DefaultAuthorizedDeciderImpl(final List users) {
        CommonUtils.assertNotEmpty(users, "users cannot be empty.");
        this.users = users;
    }

    public boolean isAuthorizedToUseApplication(final Principal principal) {
        return this.users.contains(principal.getId());
    }
}
