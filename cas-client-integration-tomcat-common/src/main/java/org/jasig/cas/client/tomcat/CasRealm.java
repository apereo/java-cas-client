/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.client.tomcat;

import java.security.Principal;

/**
 * Describes Tomcat <code>Realm</code> implementations that do not require password
 * for authentication.
 *
 * @author Marvin S. Addison
 * @version $Revision$
 * @since 3.1.12
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
