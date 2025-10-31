/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apereo.cas.client.configuration;

import org.apereo.cas.client.Protocol;
import org.apereo.cas.client.authentication.AuthenticationRedirectStrategy;
import org.apereo.cas.client.authentication.DefaultGatewayResolverImpl;
import org.apereo.cas.client.authentication.GatewayResolver;
import org.apereo.cas.client.proxy.ProxyGrantingTicketStorage;
import org.apereo.cas.client.proxy.ProxyGrantingTicketStorageImpl;
import org.apereo.cas.client.validation.Cas20ServiceTicketValidator;

import javax.net.ssl.HostnameVerifier;

/**
 * Holder interface for all known configuration keys.
 *
 * @author Scott Battaglia
 * @since 3.4.0
 */
public interface ConfigurationKeys {

    ConfigurationKey<String> ARTIFACT_PARAMETER_NAME = new ConfigurationKey<>("artifactParameterName", Protocol.CAS2.getArtifactParameterName());
    ConfigurationKey<String> SERVER_NAME = new ConfigurationKey<>("serverName", null);
    ConfigurationKey<String> SERVICE = new ConfigurationKey<>("service");
    ConfigurationKey<Boolean> RENEW = new ConfigurationKey<>("renew", Boolean.FALSE);
    ConfigurationKey<String> LOGOUT_PARAMETER_NAME = new ConfigurationKey<>("logoutParameterName", "logoutRequest");
    ConfigurationKey<Boolean> ARTIFACT_PARAMETER_OVER_POST = new ConfigurationKey<>("artifactParameterOverPost", Boolean.FALSE);
    ConfigurationKey<Boolean> EAGERLY_CREATE_SESSIONS = new ConfigurationKey<>("eagerlyCreateSessions", Boolean.TRUE);
    ConfigurationKey<Boolean> ENCODE_SERVICE_URL = new ConfigurationKey<>("encodeServiceUrl", Boolean.TRUE);
    ConfigurationKey<String> SSL_CONFIG_FILE = new ConfigurationKey<>("sslConfigFile", null);
    ConfigurationKey<String> ROLE_ATTRIBUTE = new ConfigurationKey<>("roleAttribute", null);
    ConfigurationKey<Boolean> IGNORE_CASE = new ConfigurationKey<>("ignoreCase", Boolean.FALSE);
    ConfigurationKey<String> CAS_SERVER_LOGIN_URL = new ConfigurationKey<>("casServerLoginUrl", null);
    ConfigurationKey<Boolean> GATEWAY = new ConfigurationKey<>("gateway", Boolean.FALSE);
    ConfigurationKey<String> METHOD = new ConfigurationKey<>("method", null);
    ConfigurationKey<Class<? extends AuthenticationRedirectStrategy>> AUTHENTICATION_REDIRECT_STRATEGY_CLASS =
        new ConfigurationKey<>("authenticationRedirectStrategyClass", null);
    ConfigurationKey<Class<? extends GatewayResolver>> GATEWAY_STORAGE_CLASS =
        new ConfigurationKey<>("gatewayStorageClass", DefaultGatewayResolverImpl.class);
    ConfigurationKey<String> CAS_SERVER_URL_PREFIX = new ConfigurationKey<>("casServerUrlPrefix", null);
    ConfigurationKey<String> ENCODING = new ConfigurationKey<>("encoding", null);
    ConfigurationKey<Long> TOLERANCE = new ConfigurationKey<>("tolerance", 1000L);
    ConfigurationKey<String> PRIVATE_KEY_PATH = new ConfigurationKey<>("privateKeyPath", null);
    ConfigurationKey<String> PRIVATE_KEY_ALGORITHM = new ConfigurationKey<>("privateKeyAlgorithm", "RSA");

    /**
     * @deprecated As of 3.4. This constant is not used by the client and will
     * be removed in future versions.
     */
    @Deprecated
    ConfigurationKey<Boolean> DISABLE_XML_SCHEMA_VALIDATION = new ConfigurationKey<>("disableXmlSchemaValidation", Boolean.FALSE);
    ConfigurationKey<String> IGNORE_PATTERN = new ConfigurationKey<>("ignorePattern", null);
    ConfigurationKey<String> IGNORE_URL_PATTERN_TYPE = new ConfigurationKey<>("ignoreUrlPatternType", "REGEX");
    ConfigurationKey<Class<? extends HostnameVerifier>> HOSTNAME_VERIFIER = new ConfigurationKey<>("hostnameVerifier", null);
    ConfigurationKey<String> HOSTNAME_VERIFIER_CONFIG = new ConfigurationKey<>("hostnameVerifierConfig", null);
    ConfigurationKey<Boolean> EXCEPTION_ON_VALIDATION_FAILURE = new ConfigurationKey<>("exceptionOnValidationFailure", Boolean.TRUE);
    ConfigurationKey<Boolean> REDIRECT_AFTER_VALIDATION = new ConfigurationKey<>("redirectAfterValidation", Boolean.TRUE);
    ConfigurationKey<Boolean> USE_SESSION = new ConfigurationKey<>("useSession", Boolean.TRUE);
    ConfigurationKey<String> SECRET_KEY = new ConfigurationKey<>("secretKey", null);
    ConfigurationKey<String> CIPHER_ALGORITHM = new ConfigurationKey<>("cipherAlgorithm", "DESede");
    ConfigurationKey<String> PROXY_RECEPTOR_URL = new ConfigurationKey<>("proxyReceptorUrl", null);
    ConfigurationKey<Class<? extends ProxyGrantingTicketStorage>> PROXY_GRANTING_TICKET_STORAGE_CLASS =
        new ConfigurationKey<>("proxyGrantingTicketStorageClass", ProxyGrantingTicketStorageImpl.class);
    ConfigurationKey<Integer> MILLIS_BETWEEN_CLEAN_UPS = new ConfigurationKey<>("millisBetweenCleanUps", 60000);
    ConfigurationKey<Boolean> ACCEPT_ANY_PROXY = new ConfigurationKey<>("acceptAnyProxy", Boolean.FALSE);
    ConfigurationKey<String> ALLOWED_PROXY_CHAINS = new ConfigurationKey<>("allowedProxyChains", null);
    ConfigurationKey<Class<? extends Cas20ServiceTicketValidator>> TICKET_VALIDATOR_CLASS = new ConfigurationKey<>("ticketValidatorClass", null);
    ConfigurationKey<String> PROXY_CALLBACK_URL = new ConfigurationKey<>("proxyCallbackUrl", null);
    ConfigurationKey<String> RELAY_STATE_PARAMETER_NAME = new ConfigurationKey<>("relayStateParameterName", "RelayState");
    ConfigurationKey<String> LOGOUT_CALLBACK_PATH = new ConfigurationKey<>("logoutCallbackPath", null);
    ConfigurationKey<String> JSONP_CALLBACK_PARAMETER_NAME = new ConfigurationKey<>("jsonpCallbackParameterName", "callback");
}
