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
import org.jasig.cas.client.proxy.*;
import org.jasig.cas.client.ssl.HttpURLConnectionFactory;
import org.jasig.cas.client.ssl.HttpsURLConnectionFactory;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.util.ReflectUtils;

/**
 * Creates either a CAS20ProxyTicketValidator or a CAS20ServiceTicketValidator depending on whether any of the
 * proxy parameters are set.
 * <p>
 * This filter can also pass additional parameters to the ticket validator.  Any init parameter not included in the
 * reserved list {@link org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter#RESERVED_INIT_PARAMS}.
 *
 * @author Scott Battaglia
 * @author Brad Cupit (brad [at] lsu {dot} edu)
 * @version $Revision$ $Date$
 * @since 3.1
 *
 */
public class Cas20ProxyReceivingTicketValidationFilter extends AbstractTicketValidationFilter {

    private static final String[] RESERVED_INIT_PARAMS = new String[] { "proxyGrantingTicketStorageClass",
            "proxyReceptorUrl", "acceptAnyProxy", "allowedProxyChains", "casServerUrlPrefix", "proxyCallbackUrl",
            "renew", "exceptionOnValidationFailure", "redirectAfterValidation", "useSession", "serverName", "service",
            "artifactParameterName", "serviceParameterName", "encodeServiceUrl", "millisBetweenCleanUps",
            "hostnameVerifier", "encoding", "config", "ticketValidatorClass" };

    private static final int DEFAULT_MILLIS_BETWEEN_CLEANUPS = 60 * 1000;

    /**
     * The URL to send to the CAS server as the URL that will process proxying requests on the CAS client. 
     */
    private String proxyReceptorUrl;

    private Timer timer;

    private TimerTask timerTask;

    private int millisBetweenCleanUps;

    /**
     * Storage location of ProxyGrantingTickets and Proxy Ticket IOUs.
     */
    private ProxyGrantingTicketStorage proxyGrantingTicketStorage = new ProxyGrantingTicketStorageImpl();

    protected void initInternal(final FilterConfig filterConfig) throws ServletException {
        setProxyReceptorUrl(getPropertyFromInitParams(filterConfig, "proxyReceptorUrl", null));

        final String proxyGrantingTicketStorageClass = getPropertyFromInitParams(filterConfig,
                "proxyGrantingTicketStorageClass", null);

        if (proxyGrantingTicketStorageClass != null) {
            this.proxyGrantingTicketStorage = ReflectUtils.newInstance(proxyGrantingTicketStorageClass);

            if (this.proxyGrantingTicketStorage instanceof AbstractEncryptedProxyGrantingTicketStorageImpl) {
                final AbstractEncryptedProxyGrantingTicketStorageImpl p = (AbstractEncryptedProxyGrantingTicketStorageImpl) this.proxyGrantingTicketStorage;
                final String cipherAlgorithm = getPropertyFromInitParams(filterConfig, "cipherAlgorithm",
                        AbstractEncryptedProxyGrantingTicketStorageImpl.DEFAULT_ENCRYPTION_ALGORITHM);
                final String secretKey = getPropertyFromInitParams(filterConfig, "secretKey", null);

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

        logger.trace("Setting proxyReceptorUrl parameter: {}", this.proxyReceptorUrl);
        this.millisBetweenCleanUps = Integer.parseInt(getPropertyFromInitParams(filterConfig, "millisBetweenCleanUps",
                Integer.toString(DEFAULT_MILLIS_BETWEEN_CLEANUPS)));
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

    private <T> T createNewTicketValidator(final String ticketValidatorClass, final String casServerUrlPrefix,
            final Class<T> clazz) {
        if (CommonUtils.isBlank(ticketValidatorClass)) {
            return ReflectUtils.newInstance(clazz, casServerUrlPrefix);
        }

        return ReflectUtils.newInstance(ticketValidatorClass, casServerUrlPrefix);
    }

    /**
     * Constructs a Cas20ServiceTicketValidator or a Cas20ProxyTicketValidator based on supplied parameters.
     *
     * @param filterConfig the Filter Configuration object.
     * @return a fully constructed TicketValidator.
     */
    protected final TicketValidator getTicketValidator(final FilterConfig filterConfig) {
        final String allowAnyProxy = getPropertyFromInitParams(filterConfig, "acceptAnyProxy", null);
        final String allowedProxyChains = getPropertyFromInitParams(filterConfig, "allowedProxyChains", null);
        final String casServerUrlPrefix = getPropertyFromInitParams(filterConfig, "casServerUrlPrefix", null);
        final String ticketValidatorClass = getPropertyFromInitParams(filterConfig, "ticketValidatorClass", null);
        final Cas20ServiceTicketValidator validator;

        if (CommonUtils.isNotBlank(allowAnyProxy) || CommonUtils.isNotBlank(allowedProxyChains)) {
            final Cas20ProxyTicketValidator v = createNewTicketValidator(ticketValidatorClass, casServerUrlPrefix,
                    Cas20ProxyTicketValidator.class);
            v.setAcceptAnyProxy(parseBoolean(allowAnyProxy));
            v.setAllowedProxyChains(CommonUtils.createProxyList(allowedProxyChains));
            validator = v;
        } else {
            validator = createNewTicketValidator(ticketValidatorClass, casServerUrlPrefix,
                    Cas20ServiceTicketValidator.class);
        }
        validator.setProxyCallbackUrl(getPropertyFromInitParams(filterConfig, "proxyCallbackUrl", null));
        validator.setProxyGrantingTicketStorage(this.proxyGrantingTicketStorage);

        final HttpURLConnectionFactory factory = new HttpsURLConnectionFactory(getHostnameVerifier(filterConfig),
                getSSLConfig(filterConfig));
        validator.setURLConnectionFactory(factory);

        validator.setProxyRetriever(new Cas20ProxyRetriever(casServerUrlPrefix, getPropertyFromInitParams(filterConfig,
                "encoding", null), factory));
        validator.setRenew(parseBoolean(getPropertyFromInitParams(filterConfig, "renew", "false")));
        validator.setEncoding(getPropertyFromInitParams(filterConfig, "encoding", null));

        final Map<String, String> additionalParameters = new HashMap<String, String>();
        final List<String> params = Arrays.asList(RESERVED_INIT_PARAMS);

        for (final Enumeration<?> e = filterConfig.getInitParameterNames(); e.hasMoreElements();) {
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
