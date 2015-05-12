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
package org.jasig.cas.client.validation;

import javax.servlet.FilterConfig;

import org.jasig.cas.client.Protocol;
import org.jasig.cas.client.configuration.ConfigurationKeys;
import org.jasig.cas.client.ssl.HttpURLConnectionFactory;
import org.jasig.cas.client.ssl.HttpsURLConnectionFactory;

/**
 * Implementation of TicketValidationFilter that can instanciate a SAML 1.1 Ticket Validator.
 * <p/>
 * Deployers can provide the "casServerUrlPrefix" and "tolerance" properties of the Saml11TicketValidator via the
 * context or filter init parameters.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @version $Revision$ $Date$
 * @since 3.1
 */
public class Saml11TicketValidationFilter extends AbstractTicketValidationFilter {

    public Saml11TicketValidationFilter() {
        super(Protocol.SAML11);
    }

    protected final TicketValidator getTicketValidator(final FilterConfig filterConfig) {
        final Saml11TicketValidator validator = new Saml11TicketValidator(getString(ConfigurationKeys.CAS_SERVER_URL_PREFIX));
        final long tolerance = getLong(ConfigurationKeys.TOLERANCE);
        validator.setTolerance(tolerance);
        validator.setRenew(getBoolean(ConfigurationKeys.RENEW));

        final HttpURLConnectionFactory factory = new HttpsURLConnectionFactory(getHostnameVerifier(), getSSLConfig());
        validator.setURLConnectionFactory(factory);

        validator.setEncoding(getString(ConfigurationKeys.ENCODING));
        return validator;
    }
}
