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
package org.apereo.cas.client.validation;

import org.apereo.cas.client.Protocol;
import org.apereo.cas.client.configuration.ConfigurationKeys;
import org.apereo.cas.client.proxy.AbstractEncryptedProxyGrantingTicketStorageImpl;
import org.apereo.cas.client.proxy.Cas20ProxyRetriever;
import org.apereo.cas.client.proxy.CleanUpTimerTask;
import org.apereo.cas.client.proxy.ProxyGrantingTicketStorage;
import org.apereo.cas.client.proxy.ProxyGrantingTicketStorageImpl;
import org.apereo.cas.client.ssl.HttpURLConnectionFactory;
import org.apereo.cas.client.ssl.HttpsURLConnectionFactory;
import org.apereo.cas.client.util.CommonUtils;
import org.apereo.cas.client.util.PrivateKeyUtils;
import org.apereo.cas.client.util.ReflectUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Creates either a CAS20ProxyTicketValidator or a CAS20ServiceTicketValidator depending on whether any of the
 * proxy parameters are set.
 * <p/>
 * This filter can also pass additional parameters to the ticket validator.  Any init parameter not included in the
 * reserved list {@link Cas20ProxyReceivingTicketValidationFilter#RESERVED_INIT_PARAMS}.
 *
 * @author Scott Battaglia
 * @author Brad Cupit (brad [at] lsu {dot} edu)
 * @version $Revision$ $Date$
 * @since 3.1
 */
public class Cas20ProxyReceivingTicketValidationFilter extends AbstractTicketValidationFilter {

    private static final String[] RESERVED_INIT_PARAMS =
        new String[]{ConfigurationKeys.ARTIFACT_PARAMETER_NAME.getName(), ConfigurationKeys.SERVER_NAME.getName(), ConfigurationKeys.SERVICE.getName(), ConfigurationKeys.RENEW.getName(),
            ConfigurationKeys.LOGOUT_PARAMETER_NAME.getName(),
            ConfigurationKeys.ARTIFACT_PARAMETER_OVER_POST.getName(), ConfigurationKeys.EAGERLY_CREATE_SESSIONS.getName(), ConfigurationKeys.ENCODE_SERVICE_URL.getName(),
            ConfigurationKeys.SSL_CONFIG_FILE.getName(), ConfigurationKeys.ROLE_ATTRIBUTE.getName(), ConfigurationKeys.IGNORE_CASE.getName(),
            ConfigurationKeys.CAS_SERVER_LOGIN_URL.getName(), ConfigurationKeys.GATEWAY.getName(), ConfigurationKeys.AUTHENTICATION_REDIRECT_STRATEGY_CLASS.getName(),
            ConfigurationKeys.GATEWAY_STORAGE_CLASS.getName(), ConfigurationKeys.CAS_SERVER_URL_PREFIX.getName(), ConfigurationKeys.ENCODING.getName(),
            ConfigurationKeys.TOLERANCE.getName(), ConfigurationKeys.IGNORE_PATTERN.getName(), ConfigurationKeys.IGNORE_URL_PATTERN_TYPE.getName(),
            ConfigurationKeys.HOSTNAME_VERIFIER.getName(), ConfigurationKeys.HOSTNAME_VERIFIER_CONFIG.getName(),
            ConfigurationKeys.EXCEPTION_ON_VALIDATION_FAILURE.getName(), ConfigurationKeys.REDIRECT_AFTER_VALIDATION.getName(), ConfigurationKeys.USE_SESSION.getName(),
            ConfigurationKeys.SECRET_KEY.getName(), ConfigurationKeys.CIPHER_ALGORITHM.getName(), ConfigurationKeys.PROXY_RECEPTOR_URL.getName(),
            ConfigurationKeys.PROXY_GRANTING_TICKET_STORAGE_CLASS.getName(), ConfigurationKeys.MILLIS_BETWEEN_CLEAN_UPS.getName(), ConfigurationKeys.ACCEPT_ANY_PROXY.getName(),
            ConfigurationKeys.ALLOWED_PROXY_CHAINS.getName(), ConfigurationKeys.TICKET_VALIDATOR_CLASS.getName(),
            ConfigurationKeys.PROXY_CALLBACK_URL.getName(), ConfigurationKeys.RELAY_STATE_PARAMETER_NAME.getName(), ConfigurationKeys.METHOD.getName(),
            ConfigurationKeys.PRIVATE_KEY_PATH.getName(), ConfigurationKeys.PRIVATE_KEY_ALGORITHM.getName()
        };

    protected Class<? extends Cas20ServiceTicketValidator> defaultServiceTicketValidatorClass;

    protected Class<? extends Cas20ProxyTicketValidator> defaultProxyTicketValidatorClass;

    /**
     * The URL to send to the CAS server as the URL that will process proxying requests on the CAS client.
     */
    private String proxyReceptorUrl;

    private Timer timer;

    private TimerTask timerTask;

    private int millisBetweenCleanUps;

    private PrivateKey privateKey;

    /**
     * Storage location of ProxyGrantingTickets and Proxy Ticket IOUs.
     */
    private ProxyGrantingTicketStorage proxyGrantingTicketStorage = new ProxyGrantingTicketStorageImpl();

    public Cas20ProxyReceivingTicketValidationFilter() {
        this(Protocol.CAS2);
        this.defaultServiceTicketValidatorClass = Cas20ServiceTicketValidator.class;
        this.defaultProxyTicketValidatorClass = Cas20ProxyTicketValidator.class;
    }

    protected Cas20ProxyReceivingTicketValidationFilter(final Protocol protocol) {
        super(protocol);
    }

    public static PrivateKey buildPrivateKey(final String keyPath, final String keyAlgorithm) {
        if (keyPath != null) {
            return PrivateKeyUtils.createKey(keyPath, keyAlgorithm);
        }
        return null;
    }

    @Override
    public void init() {
        super.init();
        CommonUtils.assertNotNull(this.proxyGrantingTicketStorage, "proxyGrantingTicketStorage cannot be null.");

        if (this.timer == null) {
            this.timer = new Timer(true);
        }

        if (this.timerTask == null) {
            this.timerTask = new CleanUpTimerTask(this.proxyGrantingTicketStorage);
        }
        this.timer.schedule(this.timerTask, this.millisBetweenCleanUps, this.millisBetweenCleanUps);
    }

    @Override
    protected void initInternal(final FilterConfig filterConfig) throws ServletException {
        setProxyReceptorUrl(getString(ConfigurationKeys.PROXY_RECEPTOR_URL));

        final var proxyGrantingTicketStorageClass = getClass(ConfigurationKeys.PROXY_GRANTING_TICKET_STORAGE_CLASS);

        if (proxyGrantingTicketStorageClass != null) {
            this.proxyGrantingTicketStorage = ReflectUtils.newInstance(proxyGrantingTicketStorageClass);

            if (this.proxyGrantingTicketStorage instanceof AbstractEncryptedProxyGrantingTicketStorageImpl) {
                final var p = (AbstractEncryptedProxyGrantingTicketStorageImpl) this.proxyGrantingTicketStorage;
                final var cipherAlgorithm = getString(ConfigurationKeys.CIPHER_ALGORITHM);
                final var secretKey = getString(ConfigurationKeys.SECRET_KEY);

                p.setCipherAlgorithm(cipherAlgorithm);

                try {
                    if (secretKey != null) {
                        p.setSecretKey(secretKey);
                    }
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        this.millisBetweenCleanUps = getInt(ConfigurationKeys.MILLIS_BETWEEN_CLEAN_UPS);

        this.privateKey = buildPrivateKey(getString(ConfigurationKeys.PRIVATE_KEY_PATH), getString(ConfigurationKeys.PRIVATE_KEY_ALGORITHM));
        super.initInternal(filterConfig);
    }

    /**
     * Constructs a Cas20ServiceTicketValidator or a Cas20ProxyTicketValidator based on supplied parameters.
     *
     * @param filterConfig the Filter Configuration object.
     * @return a fully constructed TicketValidator.
     */
    @Override
    protected final TicketValidator getTicketValidator(final FilterConfig filterConfig) {
        final var allowAnyProxy = getBoolean(ConfigurationKeys.ACCEPT_ANY_PROXY);
        final var allowedProxyChains = getString(ConfigurationKeys.ALLOWED_PROXY_CHAINS);
        final var casServerUrlPrefix = getString(ConfigurationKeys.CAS_SERVER_URL_PREFIX);
        final var ticketValidatorClass = getClass(ConfigurationKeys.TICKET_VALIDATOR_CLASS);
        final Cas20ServiceTicketValidator validator;

        if (allowAnyProxy || CommonUtils.isNotBlank(allowedProxyChains)) {
            final var v = createNewTicketValidator(ticketValidatorClass, casServerUrlPrefix,
                this.defaultProxyTicketValidatorClass);
            v.setAcceptAnyProxy(allowAnyProxy);
            v.setAllowedProxyChains(CommonUtils.createProxyList(allowedProxyChains));
            validator = v;
        } else {
            validator = createNewTicketValidator(ticketValidatorClass, casServerUrlPrefix,
                this.defaultServiceTicketValidatorClass);
        }
        validator.setProxyCallbackUrl(getString(ConfigurationKeys.PROXY_CALLBACK_URL));
        validator.setProxyGrantingTicketStorage(this.proxyGrantingTicketStorage);

        final HttpURLConnectionFactory factory = new HttpsURLConnectionFactory(getHostnameVerifier(),
            getSSLConfig());
        validator.setURLConnectionFactory(factory);

        validator.setProxyRetriever(new Cas20ProxyRetriever(casServerUrlPrefix, getString(ConfigurationKeys.ENCODING), factory));
        validator.setRenew(getBoolean(ConfigurationKeys.RENEW));
        validator.setEncoding(getString(ConfigurationKeys.ENCODING));

        final Map<String, String> additionalParameters = new HashMap<>();
        final var params = Arrays.asList(RESERVED_INIT_PARAMS);

        for (final Enumeration<?> e = filterConfig.getInitParameterNames(); e.hasMoreElements(); ) {
            final var s = (String) e.nextElement();

            if (!params.contains(s)) {
                additionalParameters.put(s, filterConfig.getInitParameter(s));
            }
        }

        validator.setPrivateKey(this.privateKey);

        validator.setCustomParameters(additionalParameters);
        return validator;
    }

    /**
     * This processes the ProxyReceptor request before the ticket validation code executes.
     */
    @Override
    protected final boolean preFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
                                      final FilterChain filterChain) throws IOException, ServletException {
        final var request = (HttpServletRequest) servletRequest;
        final var response = (HttpServletResponse) servletResponse;
        final var requestUri = request.getRequestURI();

        if (CommonUtils.isEmpty(this.proxyReceptorUrl) || !requestUri.endsWith(this.proxyReceptorUrl)) {
            return true;
        }

        try {
            CommonUtils.readAndRespondToProxyReceptorRequest(request, response, this.proxyGrantingTicketStorage);
        } catch (final RuntimeException e) {
            logger.error(e.getMessage(), e);
            throw e;
        }

        return false;
    }

    @Override
    public void destroy() {
        super.destroy();
        this.timer.cancel();
    }

    public final void setProxyReceptorUrl(final String proxyReceptorUrl) {
        this.proxyReceptorUrl = proxyReceptorUrl;
    }

    public void setProxyGrantingTicketStorage(final ProxyGrantingTicketStorage storage) {
        this.proxyGrantingTicketStorage = storage;
    }

    public void setTimer(final Timer timer) {
        this.timer = timer;
    }

    public void setTimerTask(final TimerTask timerTask) {
        this.timerTask = timerTask;
    }

    public void setMillisBetweenCleanUps(final int millisBetweenCleanUps) {
        this.millisBetweenCleanUps = millisBetweenCleanUps;
    }

    private static <T> T createNewTicketValidator(final Class<? extends Cas20ServiceTicketValidator> ticketValidatorClass, final String casServerUrlPrefix,
                                                  final Class<T> clazz) {
        if (ticketValidatorClass == null) {
            return ReflectUtils.newInstance(clazz, casServerUrlPrefix);
        }

        return (T) ReflectUtils.newInstance(ticketValidatorClass, casServerUrlPrefix);
    }
}
