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
package org.jasig.cas.client.configuration;

import org.jasig.cas.client.Protocol;
import org.jasig.cas.client.authentication.AuthenticationRedirectStrategy;
import org.jasig.cas.client.authentication.DefaultGatewayResolverImpl;
import org.jasig.cas.client.authentication.GatewayResolver;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorage;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorageImpl;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;

import javax.net.ssl.HostnameVerifier;

/**
 * Holder interface for all known configuration keys.
 *
 * @author Scott Battaglia
 * @since 3.4.0
 */
public interface ConfigurationKeys {

    ConfigurationKey<String> ARTIFACT_PARAMETER_NAME = new ConfigurationKey<String>("artifactParameterName", Protocol.CAS2.getArtifactParameterName());
    ConfigurationKey<String> SERVER_NAME = new ConfigurationKey<String>("serverName", null);
    ConfigurationKey<String> SERVICE = new ConfigurationKey<String>("service");
    ConfigurationKey<Boolean> RENEW = new ConfigurationKey<Boolean>("renew", Boolean.FALSE);
    ConfigurationKey<String> LOGOUT_PARAMETER_NAME = new ConfigurationKey<String>("logoutParameterName", "logoutRequest");
    ConfigurationKey<Boolean> ARTIFACT_PARAMETER_OVER_POST = new ConfigurationKey<Boolean>("artifactParameterOverPost", Boolean.FALSE);
    ConfigurationKey<Boolean> EAGERLY_CREATE_SESSIONS = new ConfigurationKey<Boolean>("eagerlyCreateSessions", Boolean.TRUE);
    ConfigurationKey<Boolean> ENCODE_SERVICE_URL = new ConfigurationKey<Boolean>("encodeServiceUrl", Boolean.TRUE);
    ConfigurationKey<String> SSL_CONFIG_FILE = new ConfigurationKey<String>("sslConfigFile", null);
    ConfigurationKey<String> ROLE_ATTRIBUTE = new ConfigurationKey<String>("roleAttribute", null);
    ConfigurationKey<Boolean> IGNORE_CASE = new ConfigurationKey<Boolean>("ignoreCase", Boolean.FALSE);
    ConfigurationKey<String> CAS_SERVER_LOGIN_URL = new ConfigurationKey<String>("casServerLoginUrl", null);
    ConfigurationKey<Boolean> GATEWAY = new ConfigurationKey<Boolean>("gateway", Boolean.FALSE);
    ConfigurationKey<Class<? extends AuthenticationRedirectStrategy>> AUTHENTICATION_REDIRECT_STRATEGY_CLASS = new ConfigurationKey<Class<? extends AuthenticationRedirectStrategy>>("authenticationRedirectStrategyClass", null);
    ConfigurationKey<Class<? extends GatewayResolver>> GATEWAY_STORAGE_CLASS = new ConfigurationKey<Class<? extends GatewayResolver>>("gatewayStorageClass", DefaultGatewayResolverImpl.class);
    ConfigurationKey<String> CAS_SERVER_URL_PREFIX = new ConfigurationKey<String>("casServerUrlPrefix", null);
    ConfigurationKey<String> ENCODING = new ConfigurationKey<String>("encoding", null);
    ConfigurationKey<Long> TOLERANCE = new ConfigurationKey<Long>("tolerance", 1000L);

    /**
     * @deprecated As of 3.4. This constant is not used by the client and will
     * be removed in future versions.
     */
    @Deprecated
    ConfigurationKey<Boolean> DISABLE_XML_SCHEMA_VALIDATION = new ConfigurationKey<Boolean>("disableXmlSchemaValidation", Boolean.FALSE);
    ConfigurationKey<String> IGNORE_PATTERN = new ConfigurationKey<String>("ignorePattern", null);
    ConfigurationKey<String> IGNORE_URL_PATTERN_TYPE = new ConfigurationKey<String>("ignoreUrlPatternType", "REGEX");
    ConfigurationKey<Class<? extends HostnameVerifier>> HOSTNAME_VERIFIER = new ConfigurationKey<Class<? extends HostnameVerifier>>("hostnameVerifier", null);
    ConfigurationKey<String> HOSTNAME_VERIFIER_CONFIG = new ConfigurationKey<String>("hostnameVerifierConfig", null);
    ConfigurationKey<Boolean> EXCEPTION_ON_VALIDATION_FAILURE = new ConfigurationKey<Boolean>("exceptionOnValidationFailure", Boolean.TRUE);
    ConfigurationKey<Boolean> REDIRECT_AFTER_VALIDATION = new ConfigurationKey<Boolean>("redirectAfterValidation", Boolean.TRUE);
    ConfigurationKey<Boolean> USE_SESSION = new ConfigurationKey<Boolean>("useSession", Boolean.TRUE);
    ConfigurationKey<String> SECRET_KEY = new ConfigurationKey<String>("secretKey", null);
    ConfigurationKey<String> CIPHER_ALGORITHM = new ConfigurationKey<String>("cipherAlgorithm", "DESede");
    ConfigurationKey<String> PROXY_RECEPTOR_URL = new ConfigurationKey<String>("proxyReceptorUrl", null);
    ConfigurationKey<Class<? extends ProxyGrantingTicketStorage>> PROXY_GRANTING_TICKET_STORAGE_CLASS = new ConfigurationKey<Class<? extends ProxyGrantingTicketStorage>>("proxyGrantingTicketStorageClass", ProxyGrantingTicketStorageImpl.class);
    ConfigurationKey<Integer> MILLIS_BETWEEN_CLEAN_UPS = new ConfigurationKey<Integer>("millisBetweenCleanUps", 60000);
    ConfigurationKey<Boolean> ACCEPT_ANY_PROXY = new ConfigurationKey<Boolean>("acceptAnyProxy", Boolean.FALSE);
    ConfigurationKey<String> ALLOWED_PROXY_CHAINS = new ConfigurationKey<String>("allowedProxyChains", null);
    ConfigurationKey<Class<? extends Cas20ServiceTicketValidator>> TICKET_VALIDATOR_CLASS = new ConfigurationKey<Class<? extends Cas20ServiceTicketValidator>>("ticketValidatorClass", null);
    ConfigurationKey<String> PROXY_CALLBACK_URL = new ConfigurationKey<String>("proxyCallbackUrl", null);
    ConfigurationKey<String> RELAY_STATE_PARAMETER_NAME = new ConfigurationKey<String>("relayStateParameterName", "RelayState");
    ConfigurationKey<String> LOGOUT_CALLBACK_PATH = new ConfigurationKey<String>("logoutCallbackPath", null);
}
