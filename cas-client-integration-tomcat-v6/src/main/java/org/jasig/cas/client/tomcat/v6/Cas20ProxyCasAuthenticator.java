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
package org.jasig.cas.client.tomcat.v6;

import org.apache.catalina.LifecycleException;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.validation.Cas20ProxyTicketValidator;
import org.jasig.cas.client.validation.TicketValidator;

/**
 * Authenticator that handles the CAS 2.0 protocol with proxying support.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1.12
 */
public final class Cas20ProxyCasAuthenticator extends AbstractCasAuthenticator {

    public static final String AUTH_METHOD = "CAS20-PROXY";

    private static final String NAME = Cas20ProxyCasAuthenticator.class.getName();

    private Cas20ProxyTicketValidator ticketValidator;

    private boolean acceptAnyProxy;

    private String allowedProxyChains;

    public void setAcceptAnyProxy(final boolean acceptAnyProxy) {
        this.acceptAnyProxy = acceptAnyProxy;
    }

    public void setAllowedProxyChains(final String allowedProxyChains) {
        this.allowedProxyChains = allowedProxyChains;
    }

    protected TicketValidator getTicketValidator() {
        return this.ticketValidator;
    }

    protected String getAuthenticationMethod() {
        return AUTH_METHOD;
    }

    protected String getName() {
        return NAME;
    }

    public void start() throws LifecycleException {
        super.start();
        this.ticketValidator = new Cas20ProxyTicketValidator(getCasServerUrlPrefix());
        this.ticketValidator.setRenew(isRenew());
        this.ticketValidator.setProxyCallbackUrl(getProxyCallbackUrl());
        this.ticketValidator.setProxyGrantingTicketStorage(ProxyCallbackValve.getProxyGrantingTicketStorage());
        this.ticketValidator.setAcceptAnyProxy(this.acceptAnyProxy);
        this.ticketValidator.setAllowedProxyChains(CommonUtils.createProxyList(this.allowedProxyChains));
        if (getEncoding() != null) {
            this.ticketValidator.setEncoding(getEncoding());
        }
        lifecycle.fireLifecycleEvent(AFTER_START_EVENT, null);
        logger.info("Startup completed.");
    }
}
