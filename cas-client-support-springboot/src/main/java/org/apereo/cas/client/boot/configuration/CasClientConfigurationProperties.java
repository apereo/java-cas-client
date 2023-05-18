/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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
package org.apereo.cas.client.boot.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link ConfigurationProperties} for CAS Java client filters.
 * <p>
 * Will be used to customize CAS filters via simple properties or YAML files in standard Spring Boot PropertySources.
 *
 * @author Dmitriy Kopylenko
 * @since 3.6.0
 */
@ConfigurationProperties(prefix = "cas", ignoreUnknownFields = false)
public class CasClientConfigurationProperties {

    /**
     * CAS server URL E.g. https://example.com/cas or https://cas.example. Required.
     */
    @NonNull
    private String serverUrlPrefix;

    /**
     * CAS server login URL E.g. https://example.com/cas/login or https://cas.example/login. Required.
     */
    @NonNull
    private String serverLoginUrl;

    /**
     * CAS-protected client application host URL E.g. https://myclient.example.com Required.
     */
    @NonNull
    private String clientHostUrl;

    private String hostnameVerifier;

    private String sslConfigFile;

    /**
     * List of URL patterns protected by CAS authentication filter.
     */
    private List<String> authenticationUrlPatterns = new ArrayList<>();

    /**
     * List of URL patterns protected by CAS validation filter.
     */
    private List<String> validationUrlPatterns = new ArrayList<>();

    /**
     * List of URL patterns protected by CAS request wrapper filter.
     */
    private List<String> requestWrapperUrlPatterns = new ArrayList<>();

    /**
     * List of URL patterns protected by CAS assertion thread local filter.
     */
    private List<String> assertionThreadLocalUrlPatterns = new ArrayList<>();

    /**
     * Authentication filter gateway parameter.
     */
    private Boolean gateway;

    /**
     * Validation filter useSession parameter.
     */
    private Boolean useSession = Boolean.TRUE;

    /**
     * Validation filter redirectAfterValidation.
     */
    private Boolean redirectAfterValidation = Boolean.TRUE;

    /**
     * Cas20ProxyReceivingTicketValidationFilter acceptAnyProxy parameter.
     */
    private Boolean acceptAnyProxy;

    /**
     * Cas20ProxyReceivingTicketValidationFilter allowedProxyChains parameter.
     */
    private List<String> allowedProxyChains = new ArrayList<>();

    /**
     * Cas20ProxyReceivingTicketValidationFilter proxyCallbackUrl parameter.
     */
    private String proxyCallbackUrl;

    /**
     * Cas20ProxyReceivingTicketValidationFilter proxyReceptorUrl parameter.
     */
    private String proxyReceptorUrl;

    /**
     * Name of attributes to fetch from assertion
     * to use when populating spring security context.
     */
    private List<String> attributeAuthorities = new ArrayList<>();

    /**
     * ValidationType the CAS protocol validation type. Defaults to CAS3 if not explicitly set.
     */
    private EnableCasClient.ValidationType validationType = EnableCasClient.ValidationType.CAS3;

    private Boolean skipTicketValidation = false;

    private SingleLogout singleLogout;

    public static class SingleLogout{
        /**
         * whether to receive the single logout request from cas server.
         */
        private boolean enabled = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }
    }

    public String getServerUrlPrefix() {
        return serverUrlPrefix;
    }

    public void setServerUrlPrefix(final String serverUrlPrefix) {
        this.serverUrlPrefix = serverUrlPrefix;
    }

    public String getServerLoginUrl() {
        return serverLoginUrl;
    }

    public void setServerLoginUrl(final String serverLoginUrl) {
        this.serverLoginUrl = serverLoginUrl;
    }

    public String getClientHostUrl() {
        return clientHostUrl;
    }

    public void setClientHostUrl(final String clientHostUrl) {
        this.clientHostUrl = clientHostUrl;
    }

    public Boolean getAcceptAnyProxy() {
        return acceptAnyProxy;
    }

    public void setAcceptAnyProxy(final Boolean acceptAnyProxy) {
        this.acceptAnyProxy = acceptAnyProxy;
    }

    public List<String> getAllowedProxyChains() {
        return allowedProxyChains;
    }

    public void setAllowedProxyChains(final List<String> allowedProxyChains) {
        this.allowedProxyChains = allowedProxyChains;
    }

    public String getProxyCallbackUrl() {
        return proxyCallbackUrl;
    }

    public void setProxyCallbackUrl(final String proxyCallbackUrl) {
        this.proxyCallbackUrl = proxyCallbackUrl;
    }

    public String getProxyReceptorUrl() {
        return proxyReceptorUrl;
    }

    public void setProxyReceptorUrl(final String proxyReceptorUrl) {
        this.proxyReceptorUrl = proxyReceptorUrl;
    }

    public Boolean getGateway() {
        return gateway;
    }

    public void setGateway(final Boolean gateway) {
        this.gateway = gateway;
    }

    public Boolean getUseSession() {
        return useSession;
    }

    public void setUseSession(final Boolean useSession) {
        this.useSession = useSession;
    }

    public Boolean getRedirectAfterValidation() {
        return redirectAfterValidation;
    }

    public void setRedirectAfterValidation(final Boolean redirectAfterValidation) {
        this.redirectAfterValidation = redirectAfterValidation;
    }

    public List<String> getAssertionThreadLocalUrlPatterns() {
        return assertionThreadLocalUrlPatterns;
    }

    public void setAssertionThreadLocalUrlPatterns(final List<String> assertionThreadLocalUrlPatterns) {
        this.assertionThreadLocalUrlPatterns = assertionThreadLocalUrlPatterns;
    }

    public List<String> getRequestWrapperUrlPatterns() {
        return requestWrapperUrlPatterns;
    }

    public void setRequestWrapperUrlPatterns(final List<String> requestWrapperUrlPatterns) {
        this.requestWrapperUrlPatterns = requestWrapperUrlPatterns;
    }

    public List<String> getValidationUrlPatterns() {
        return validationUrlPatterns;
    }

    public void setValidationUrlPatterns(final List<String> validationUrlPatterns) {
        this.validationUrlPatterns = validationUrlPatterns;
    }

    public List<String> getAuthenticationUrlPatterns() {
        return authenticationUrlPatterns;
    }

    public void setAuthenticationUrlPatterns(final List<String> authenticationUrlPatterns) {
        this.authenticationUrlPatterns = authenticationUrlPatterns;
    }

    public EnableCasClient.ValidationType getValidationType() {
        return validationType;
    }

    public void setValidationType(final EnableCasClient.ValidationType validationType) {
        this.validationType = validationType;
    }

    public Boolean getSkipTicketValidation() {
        return skipTicketValidation;
    }

    public void setSkipTicketValidation(final Boolean skipTicketValidation) {
        this.skipTicketValidation = skipTicketValidation;
    }

    public SingleLogout getSingleLogout() {
        return singleLogout;
    }

    public void setSingleLogout(final SingleLogout singleLogout) {
        this.singleLogout = singleLogout;
    }

    public List<String> getAttributeAuthorities() {
        return attributeAuthorities;
    }

    public void setAttributeAuthorities(final List<String> attributeAuthorities) {
        this.attributeAuthorities = attributeAuthorities;
    }

    public String getHostnameVerifier() {
        return hostnameVerifier;
    }

    public void setHostnameVerifier(final String hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
    }

    public String getSslConfigFile() {
        return sslConfigFile;
    }

    public void setSslConfigFile(final String sslConfigFile) {
        this.sslConfigFile = sslConfigFile;
    }
}
