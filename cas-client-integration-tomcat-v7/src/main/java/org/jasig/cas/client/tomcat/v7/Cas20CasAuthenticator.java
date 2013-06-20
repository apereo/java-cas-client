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
package org.jasig.cas.client.tomcat.v7;

import org.apache.catalina.LifecycleException;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.jasig.cas.client.validation.TicketValidator;

/**
 *  Authenticator that handles the CAS 2.0 protocol.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1.12
 */
public final class Cas20CasAuthenticator extends AbstractCasAuthenticator {

    public static final String AUTH_METHOD = "CAS20";

    private static final String NAME = Cas20CasAuthenticator.class.getName();

    private Cas20ServiceTicketValidator ticketValidator;

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
        this.ticketValidator = new Cas20ServiceTicketValidator(getCasServerUrlPrefix());
        if (getEncoding() != null) {
            this.ticketValidator.setEncoding(getEncoding());
        }
        this.ticketValidator.setProxyCallbackUrl(getProxyCallbackUrl());
        this.ticketValidator.setProxyGrantingTicketStorage(ProxyCallbackValve.getProxyGrantingTicketStorage());
        this.ticketValidator.setRenew(isRenew());
    }
}
