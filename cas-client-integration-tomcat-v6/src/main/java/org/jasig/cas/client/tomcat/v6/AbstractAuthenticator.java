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

import java.io.IOException;
import java.security.Principal;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Realm;
import org.apache.catalina.authenticator.AuthenticatorBase;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.deploy.LoginConfig;
import org.jasig.cas.client.tomcat.AuthenticatorDelegate;
import org.jasig.cas.client.tomcat.CasRealm;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.validation.TicketValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base authenticator for all authentication protocols supported by CAS.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1.12
 */
public abstract class AbstractAuthenticator extends AuthenticatorBase implements LifecycleListener {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final AuthenticatorDelegate delegate = new AuthenticatorDelegate();

    private String casServerUrlPrefix;

    private String encoding;

    private boolean encode;

    private boolean renew;

    protected abstract String getAuthenticationMethod();

    /**
     * Abstract method that subclasses should use to provide the name of the artifact parameter (i.e. ticket)
     *
     * @return the artifact parameter name.  CANNOT be NULL.
     */
    protected abstract String getArtifactParameterName();

    /**
     * Abstract method that subclasses should use to provide the name of the service parameter (i.e. service)
     *
     * @return the service parameter name.  CANNOT be NULL.
     */
    protected abstract String getServiceParameterName();

    /**
     * Returns the single instance of the ticket validator to use to validate tickets.  Sub classes should include
     * the one appropriate for the
     *
     * @return a fully configured ticket validator.  CANNOT be NULL.
     */
    protected abstract TicketValidator getTicketValidator();

    public void start() throws LifecycleException {
        super.start();
        logger.debug("{} starting.", getName());
        final Realm realm = this.context.getRealm();
        try {
            CommonUtils.assertTrue(realm instanceof CasRealm, "Expected CasRealm but got " + realm.getInfo());
            CommonUtils.assertNotNull(this.casServerUrlPrefix, "casServerUrlPrefix cannot be null.");
            CommonUtils.assertNotNull(this.delegate.getCasServerLoginUrl(), "casServerLoginUrl cannot be null.");
            CommonUtils.assertTrue(this.delegate.getServerName() != null || this.delegate.getServiceUrl() != null,
                    "either serverName or serviceUrl must be set.");
            this.delegate.setRealm((CasRealm) realm);
        } catch (final Exception e) {
            throw new LifecycleException(e);
        }
        // Complete delegate initialization after the component is started.
        // See #lifecycleEvent() method.
        addLifecycleListener(this);
    }

    protected final String getCasServerUrlPrefix() {
        return this.casServerUrlPrefix;
    }

    public final void setCasServerUrlPrefix(final String casServerUrlPrefix) {
        this.casServerUrlPrefix = casServerUrlPrefix;
    }

    public final void setCasServerLoginUrl(final String casServerLoginUrl) {
        this.delegate.setCasServerLoginUrl(casServerLoginUrl);
    }

    public final boolean isEncode() {
        return this.encode;
    }

    public final void setEncode(final boolean encode) {
        this.encode = encode;
    }

    protected final boolean isRenew() {
        return this.renew;
    }

    public void setRenew(final boolean renew) {
        this.renew = renew;
    }

    public final void setServerName(final String serverName) {
        this.delegate.setServerName(serverName);
    }

    public final void setServiceUrl(final String serviceUrl) {
        this.delegate.setServiceUrl(serviceUrl);
    }

    protected final String getEncoding() {
        return this.encoding;
    }

    public final void setEncoding(final String encoding) {
        this.encoding = encoding;
    }

    /** {@inheritDoc} */
    public final boolean authenticate(final Request request, final Response response, final LoginConfig loginConfig)
            throws IOException {
        Principal principal = request.getUserPrincipal();
        boolean result = false;
        if (principal == null) {
            // Authentication sets the response headers for status and redirect if needed
            principal = this.delegate.authenticate(request.getRequest(), response);
            if (principal != null) {
                register(request, response, principal, getAuthenticationMethod(), null, null);
                result = true;
            }
        } else {
            result = true;
        }
        return result;
    }

    /** {@inheritDoc} */
    public void lifecycleEvent(final LifecycleEvent event) {
        if (AFTER_START_EVENT.equals(event.getType())) {
            logger.debug("{} processing lifecycle event {}", getName(), AFTER_START_EVENT);
            this.delegate.setTicketValidator(getTicketValidator());
            this.delegate.setArtifactParameterName(getArtifactParameterName());
            this.delegate.setServiceParameterName(getServiceParameterName());
        }
    }

    /** {@inheritDoc} */
    public String getInfo() {
        return getName() + "/1.0";
    }

    /**
     * @return  Authenticator descriptive name.
     */
    protected abstract String getName();
}
