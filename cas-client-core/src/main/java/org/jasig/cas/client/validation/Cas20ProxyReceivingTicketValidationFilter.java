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

import java.io.IOException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.client.Protocol;
import org.jasig.cas.client.configuration.ConfigurationKeys;
import org.jasig.cas.client.proxy.*;
import org.jasig.cas.client.ssl.HttpURLConnectionFactory;
import org.jasig.cas.client.ssl.HttpsURLConnectionFactory;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.util.ReflectUtils;

import static org.jasig.cas.client.configuration.ConfigurationKeys.*;

/**
 * Creates either a CAS20ProxyTicketValidator or a CAS20ServiceTicketValidator depending on whether any of the
 * proxy parameters are set.
 * <p/>
 * This filter can also pass additional parameters to the ticket validator.  Any init parameter not included in the
 * reserved list {@link org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter#RESERVED_INIT_PARAMS}.
 *
 * @author Scott Battaglia
 * @author Brad Cupit (brad [at] lsu {dot} edu)
 * @version $Revision$ $Date$
 * @since 3.1
 */
public class Cas20ProxyReceivingTicketValidationFilter extends AbstractTicketValidationFilter {

    private static final String[] RESERVED_INIT_PARAMS = new String[]{ARTIFACT_PARAMETER_NAME.getName(), SERVER_NAME.getName(), SERVICE.getName(), RENEW.getName(), LOGOUT_PARAMETER_NAME.getName(),
            ARTIFACT_PARAMETER_OVER_POST.getName(), EAGERLY_CREATE_SESSIONS.getName(), ENCODE_SERVICE_URL.getName(), SSL_CONFIG_FILE.getName(), ROLE_ATTRIBUTE.getName(), IGNORE_CASE.getName(),
            CAS_SERVER_LOGIN_URL.getName(), GATEWAY.getName(), AUTHENTICATION_REDIRECT_STRATEGY_CLASS.getName(), GATEWAY_STORAGE_CLASS.getName(), CAS_SERVER_URL_PREFIX.getName(), ENCODING.getName(),
            TOLERANCE.getName(), IGNORE_PATTERN.getName(), IGNORE_URL_PATTERN_TYPE.getName(), HOSTNAME_VERIFIER.getName(), HOSTNAME_VERIFIER_CONFIG.getName(),
            EXCEPTION_ON_VALIDATION_FAILURE.getName(), REDIRECT_AFTER_VALIDATION.getName(), USE_SESSION.getName(), SECRET_KEY.getName(), CIPHER_ALGORITHM.getName(), PROXY_RECEPTOR_URL.getName(),
            PROXY_GRANTING_TICKET_STORAGE_CLASS.getName(), MILLIS_BETWEEN_CLEAN_UPS.getName(), ACCEPT_ANY_PROXY.getName(), ALLOWED_PROXY_CHAINS.getName(), TICKET_VALIDATOR_CLASS.getName(),
            PROXY_CALLBACK_URL.getName(), RELAY_STATE_PARAMETER_NAME.getName()
    };

    /**
     * The URL to send to the CAS server as the URL that will process proxying requests on the CAS client.
     */
    private String proxyReceptorUrl;

    private Timer timer;

    private TimerTask timerTask;

    private int millisBetweenCleanUps;

    protected Class<? extends Cas20ServiceTicketValidator> defaultServiceTicketValidatorClass;

    protected Class<? extends Cas20ProxyTicketValidator> defaultProxyTicketValidatorClass;

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

    protected void initInternal(final FilterConfig filterConfig) throws ServletException {
        setProxyReceptorUrl(getString(ConfigurationKeys.PROXY_RECEPTOR_URL));

        final Class<? extends ProxyGrantingTicketStorage> proxyGrantingTicketStorageClass = getClass(ConfigurationKeys.PROXY_GRANTING_TICKET_STORAGE_CLASS);

        if (proxyGrantingTicketStorageClass != null) {
            this.proxyGrantingTicketStorage = ReflectUtils.newInstance(proxyGrantingTicketStorageClass);

            if (this.proxyGrantingTicketStorage instanceof AbstractEncryptedProxyGrantingTicketStorageImpl) {
                final AbstractEncryptedProxyGrantingTicketStorageImpl p = (AbstractEncryptedProxyGrantingTicketStorageImpl) this.proxyGrantingTicketStorage;
                final String cipherAlgorithm = getString(ConfigurationKeys.CIPHER_ALGORITHM);
                final String secretKey = getString(ConfigurationKeys.SECRET_KEY);

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
        super.initInternal(filterConfig);
    }

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

    private <T> T createNewTicketValidator(final Class<? extends Cas20ServiceTicketValidator> ticketValidatorClass, final String casServerUrlPrefix,
                                           final Class<T> clazz) {
        if (ticketValidatorClass == null) {
            return ReflectUtils.newInstance(clazz, casServerUrlPrefix);
        }

        return (T) ReflectUtils.newInstance(ticketValidatorClass, casServerUrlPrefix);
    }

    /**
     * Constructs a Cas20ServiceTicketValidator or a Cas20ProxyTicketValidator based on supplied parameters.
     *
     * @param filterConfig the Filter Configuration object.
     * @return a fully constructed TicketValidator.
     */
    protected final TicketValidator getTicketValidator(final FilterConfig filterConfig) {
        final boolean allowAnyProxy = getBoolean(ConfigurationKeys.ACCEPT_ANY_PROXY);
        final String allowedProxyChains = getString(ConfigurationKeys.ALLOWED_PROXY_CHAINS);
        final String casServerUrlPrefix = getString(ConfigurationKeys.CAS_SERVER_URL_PREFIX);
        final Class<? extends Cas20ServiceTicketValidator> ticketValidatorClass = getClass(ConfigurationKeys.TICKET_VALIDATOR_CLASS);
        final Cas20ServiceTicketValidator validator;

        if (allowAnyProxy || CommonUtils.isNotBlank(allowedProxyChains)) {
            final Cas20ProxyTicketValidator v = createNewTicketValidator(ticketValidatorClass, casServerUrlPrefix,
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

        final Map<String, String> additionalParameters = new HashMap<String, String>();
        final List<String> params = Arrays.asList(RESERVED_INIT_PARAMS);

        for (final Enumeration<?> e = filterConfig.getInitParameterNames(); e.hasMoreElements(); ) {
            final String s = (String) e.nextElement();

            if (!params.contains(s)) {
                additionalParameters.put(s, filterConfig.getInitParameter(s));
            }
        }

        validator.setCustomParameters(additionalParameters);
        return validator;
    }

    public void destroy() {
        super.destroy();
        this.timer.cancel();
    }

    /**
     * This processes the ProxyReceptor request before the ticket validation code executes.
     */
    protected final boolean preFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
                                      final FilterChain filterChain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;
        final String requestUri = request.getRequestURI();

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
}
