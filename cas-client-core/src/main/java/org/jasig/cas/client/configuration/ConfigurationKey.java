package org.jasig.cas.client.configuration;

/**
 * Provides the type-safe list of possible keys we use for configuration.
 *
 * @author Scott Battaglia
 * @since 3.4.0
 */
public enum ConfigurationKey {

    SERVER_NAME("serverName", String.class),
    SERVICE("service", String.class),
    RENEW("renew", Boolean.class),
    ARTIFACT_PARAMETER_NAME("artifactParameterName", String.class),
    LOGOUT_PARAMETER_NAME("logoutParameterName", String.class),
    ARTIFACT_PARAMETER_OVER_POST("artifactParameterOverPost", Boolean.class),
    EAGERLY_CREATE_SESSIONS("eagerlyCreateSessions", Boolean.class),
    SERVICE_PARAMETER_NAME("serviceParameterName", String.class),
    ENCODE_SERVICE_URL("encodeServiceUrl", Boolean.class),
    SSL_CONFIG_FILE("sslConfigFile", String.class),
    ROLE_ATTRIBUTE("roleAttribute", String.class),
    IGNORE_CASE("ignoreCase", Boolean.class),
    CAS_SERVER_LOGIN_URL("casServerLoginUrl", String.class),
    GATEWAY("gateway", Boolean.class),
    AUTHENTICATION_REDIRECT_STRATEGY_CLASS("authenticationRedirectStrategyClass", Class.class),
    GATEWAY_STORAGE_CLASS("gatewayStorageClass", Class.class),
    CAS_SERVER_URL_PREFIX("casServerUrlPrefix", String.class),
    ENCODING("encoding", String.class),
    TOLERANCE("tolerance", Long.class),
    DISABLE_XML_SCHEMA_VALIDATION("disableXmlSchemaValidation", Boolean.class),
    IGNORE_PATTERN("ignorePattern", String.class),
    IGNORE_URL_PATTERN_TYPE("ignoreUrlPatternType", String.class),
    HOSTNAME_VERIFIER("hostnameVerifier", Class.class),
    HOSTNAME_VERIFIER_CONFIG("hostnameVerifierConfig", String.class),
    EXCEPTION_ON_VALIDATION_FAILURE("exceptionOnValidationFailure", Boolean.class),
    REDIRECT_AFTER_VALIDATION("redirectAfterValidation", Boolean.class),
    USE_SESSION("useSession", Boolean.class),
    SECRET_KEY("secretKey", String.class),
    CIPHER_ALGORITHM("cipherAlgorithm", String.class),
    PROXY_RECEPTOR_URL("proxyReceptorUrl", String.class),
    PROXY_GRANTING_TICKET_STORAGE_CLASS("proxyGrantingTicketStorageClass", Class.class),
    MILLIS_BETWEEN_CLEAN_UPS("millisBetweenCleanUps", Integer.class),
    ACCEPT_ANY_PROXY("acceptAnyProxy", Boolean.class),
    ALLOWED_PROXY_CHAINS("allowedProxyChains", String.class),
    TICKET_VALIDATOR_CLASS("ticketValidatorClass", Class.class),
    PROXY_CALLBACK_URL("proxyCallbackUrl", String.class),
    FRONT_LOGOUT_PARAMETER_NAME("frontLogoutParameterName", String.class),
    RELAY_STATE_PARAMETER_NAME("relayStateParameterName", String.class);

    private Class<?> propertyType;

    private String simpleName;

    private ConfigurationKey(final String simpleName, final Class<?> propertyType) {
        this.simpleName = simpleName;
        this.propertyType = propertyType;
    }


    /**
     * The simple, String version of this key that implementations of {@link org.jasig.cas.client.configuration.ConfigurationStrategy}
     * can optionally use to resolve the key.
     *
     * @return the simple name.  MUST NOT BE NULL.
     */
    public String getSimpleName() {
        return this.simpleName;
    }

    /**
     * Ultimately, what this property is (i.e. String, boolean, etc.)
     *
     * @return the class that represents the type of property this is.  MUST NOT BE NULL.
     */
    public Class<?> getPropertyType() {
        return this.propertyType;
    }
}
