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
 * Implementation of AbstractTicketValidatorFilter that creates a Cas10TicketValidator.
 * <p>Deployers can provide the "casServerPrefix" and the "renew" attributes via the standard context or filter init
 * parameters.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public class Cas10TicketValidationFilter extends AbstractTicketValidationFilter {

    public Cas10TicketValidationFilter() {
        super(Protocol.CAS1);
    }

    protected final TicketValidator getTicketValidator(final FilterConfig filterConfig) {
        final String casServerUrlPrefix = getString(ConfigurationKeys.CAS_SERVER_URL_PREFIX);
        final Cas10TicketValidator validator = new Cas10TicketValidator(casServerUrlPrefix);
        validator.setRenew(getBoolean(ConfigurationKeys.RENEW));

        final HttpURLConnectionFactory factory = new HttpsURLConnectionFactory(getHostnameVerifier(),
                getSSLConfig());
        validator.setURLConnectionFactory(factory);
        validator.setEncoding(getString(ConfigurationKeys.ENCODING));

        return validator;
    }
}
