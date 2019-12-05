/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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
package org.apereo.cas.client.jetty;

import org.eclipse.jetty.security.UserAuthentication;
import org.apereo.cas.client.util.CommonUtils;
import org.apereo.cas.client.validation.Assertion;

/**
 * CAS-specific user authentication.
 *
 * @author Marvin S. Addison
 */
public class CasAuthentication extends UserAuthentication {

    /** CAS authenticator that produced this authentication. */
    private final CasAuthenticator authenticator;

    /** CAS ticket that was successfully validated to permit authentication. */
    private final String ticket;


    /**
     * Creates a new instance.
     *
     * @param authenticator The authenticator that produced this authentication.
     * @param ticket The CAS ticket that was successfully validated to permit authentication.
     * @param assertion The CAS assertion produced from successful ticket validation.
     */
    public CasAuthentication(final CasAuthenticator authenticator, final String ticket, final Assertion assertion) {
        super(authenticator.getAuthMethod(), new CasUserIdentity(assertion, authenticator.getRoleAttribute()));
        CommonUtils.assertNotNull(ticket, "Ticket cannot be null");
        CommonUtils.assertNotNull(authenticator, "CasAuthenticator cannot be null");
        this.authenticator = authenticator;
        this.ticket = ticket;
    }

    /** @return The CAS ticket that was successfully validated to permit authentication. */
    public String getTicket() {
        return ticket;
    }

    @Override
    public void logout() {
        super.logout();
        this.authenticator.clearCachedAuthentication(ticket);
    }
}
