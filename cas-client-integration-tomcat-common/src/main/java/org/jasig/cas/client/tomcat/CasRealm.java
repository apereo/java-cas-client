/*
 * Copyright 2010 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.tomcat;

import java.security.Principal;

/**
 * Describes Tomcat <code>Realm</code> implementations that do not require password
 * for authentication.
 *
 * @author Marvin S. Addison
 * @version $Revision$
 *
 */
public interface CasRealm {
    /**
     * Authenticates the given principal.
     *
     * @param p Principal to authenticate.
     *
     * @return New principal.
     */
    Principal authenticate(Principal p);

    /**
     * Gets the roles defined for the given principal.
     *
     * @param p the principal to retrieve the roles for.
     * @return  Roles for given principal or empty array if none exist.
     */
    String[] getRoles(Principal p);
    
    /**
     * Determines whether the given principal possesses the given role.
     *
     * @param principal Principal to evaluate.
     * @param role Role to test for possession.
     * 
     * @return True if principal has given role, false otherwise.
     */
    boolean hasRole(Principal principal, String role);
}
