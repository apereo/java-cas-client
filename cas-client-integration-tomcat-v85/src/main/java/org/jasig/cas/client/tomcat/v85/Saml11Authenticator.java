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
package org.jasig.cas.client.tomcat.v85;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Request;
import org.jasig.cas.client.validation.Saml11TicketValidator;
import org.jasig.cas.client.validation.TicketValidator;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * CAS authenticator that uses the SAML 1.1 protocol.
 *
 * @author Marvin S. Addison
 * @version $Revision$
 * @since 3.1.12
 *
 */
public final class Saml11Authenticator extends AbstractAuthenticator {

    public static final String AUTH_METHOD = "SAML11";

    private static final String NAME = Saml11Authenticator.class.getName();

    private Saml11TicketValidator ticketValidator;

    /** SAML protocol clock drift tolerance in ms */
    private int tolerance = -1;

    /**
     * @param ms SAML clock drift tolerance in milliseconds.
     */
    public void setTolerance(final int ms) {
        this.tolerance = ms;
    }

    protected void startInternal() throws LifecycleException {
        super.startInternal();
        this.ticketValidator = new Saml11TicketValidator(getCasServerUrlPrefix());
        if (this.tolerance > -1) {
            this.ticketValidator.setTolerance(this.tolerance);
        }
        if (getEncoding() != null) {
            this.ticketValidator.setEncoding(getEncoding());
        }
        this.ticketValidator.setRenew(isRenew());
    }

    protected TicketValidator getTicketValidator() {
        return this.ticketValidator;
    }

    protected String getAuthenticationMethod() {
        return AUTH_METHOD;
    }

    /** {@inheritDoc} */
    protected String getArtifactParameterName() {
        return "SAMLart";
    }

    /** {@inheritDoc} */
    protected String getServiceParameterName() {
        return "TARGET";
    }

    protected String getName() {
        return NAME;
    }
}
