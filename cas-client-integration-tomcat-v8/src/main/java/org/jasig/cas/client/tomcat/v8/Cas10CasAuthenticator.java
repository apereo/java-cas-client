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
package org.jasig.cas.client.tomcat.v8;

import org.apache.catalina.LifecycleException;
import org.jasig.cas.client.validation.Cas10TicketValidator;
import org.jasig.cas.client.validation.TicketValidator;

/**
 * Authenticator that handles CAS 1.0 protocol.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1.12
 */
public final class Cas10CasAuthenticator extends AbstractCasAuthenticator {

    public static final String AUTH_METHOD = "CAS10";

    private static final String NAME = Cas10CasAuthenticator.class.getName();

    private Cas10TicketValidator ticketValidator;

    protected TicketValidator getTicketValidator() {
        return this.ticketValidator;
    }

    protected String getAuthenticationMethod() {
        return AUTH_METHOD;
    }

    protected String getName() {
        return NAME;
    }

    protected void startInternal() throws LifecycleException {
        super.startInternal();
        this.ticketValidator = new Cas10TicketValidator(getCasServerUrlPrefix());
    }
}
