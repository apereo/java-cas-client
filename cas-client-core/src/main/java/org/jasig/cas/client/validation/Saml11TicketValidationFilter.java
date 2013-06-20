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
import javax.servlet.ServletException;
import org.jasig.cas.client.ssl.HttpURLConnectionFactory;
import org.jasig.cas.client.ssl.HttpsURLConnectionFactory;

/**
 * Implementation of TicketValidationFilter that can instanciate a SAML 1.1 Ticket Validator.
 * <p>
 * Deployers can provide the "casServerUrlPrefix" and "tolerance" properties of the Saml11TicketValidator via the
 * context or filter init parameters.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public class Saml11TicketValidationFilter extends AbstractTicketValidationFilter {

    public Saml11TicketValidationFilter() {
        setArtifactParameterName("SAMLart");
        setServiceParameterName("TARGET");
    }

    protected final void initInternal(final FilterConfig filterConfig) throws ServletException {
        super.initInternal(filterConfig);

        logger.warn("SAML1.1 compliance requires the [artifactParameterName] and [serviceParameterName] to be set to specified values.");
        logger.warn("This filter will overwrite any user-provided values (if any are provided)");

        setArtifactParameterName("SAMLart");
        setServiceParameterName("TARGET");
    }

    protected final TicketValidator getTicketValidator(final FilterConfig filterConfig) {
        final Saml11TicketValidator validator = new Saml11TicketValidator(getPropertyFromInitParams(filterConfig,
                "casServerUrlPrefix", null));
        final String tolerance = getPropertyFromInitParams(filterConfig, "tolerance", "1000");
        validator.setTolerance(Long.parseLong(tolerance));
        validator.setRenew(parseBoolean(getPropertyFromInitParams(filterConfig, "renew", "false")));

        final HttpURLConnectionFactory factory = new HttpsURLConnectionFactory(getHostnameVerifier(filterConfig),
                getSSLConfig(filterConfig));
        validator.setURLConnectionFactory(factory);

        validator.setEncoding(getPropertyFromInitParams(filterConfig, "encoding", null));
        validator.setDisableXmlSchemaValidation(parseBoolean(getPropertyFromInitParams(filterConfig,
                "disableXmlSchemaValidation", "false")));
        return validator;
    }
}
