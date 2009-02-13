/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.validation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.client.cleanup.CleanUpRegistry;
import org.jasig.cas.client.cleanup.CleanUpRegistryImpl;
import org.jasig.cas.client.cleanup.Cleanable;
import org.jasig.cas.client.proxy.Cas20ProxyRetriever;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorage;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorageImpl;
import org.jasig.cas.client.util.CommonUtils;

/**
 * Creates either a CAS20ProxyTicketValidator or a CAS20ServiceTicketValidator depending on whether any of the
 * proxy parameters are set.
 * <p>
 * This filter can also pass additional parameteres to the ticket validator.  Any init parameter not included in the
 * reserved list {@link org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter#RESERVED_INIT_PARAMS}.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 *
 */
public class Cas20ProxyReceivingTicketValidationFilter extends AbstractTicketValidationFilter {

    private static final String[] RESERVED_INIT_PARAMS = new String[] {"proxyReceptorUrl", "acceptAnyProxy", "allowedProxyChains", "casServerUrlPrefix", "proxyCallbackUrl", "renew", "exceptionOnValidationFailure", "redirectAfterValidation", "useSession", "serverName", "service", "artifactParameterName", "serviceParameterName", "encodeServiceUrl"};

    /**
     * The URL to send to the CAS server as the URL that will process proxying requests on the CAS client. 
     */
    private String proxyReceptorUrl;

    /**
     * a place to register implementations of {@link Cleanable}
     */
    private CleanUpRegistry cleanUpRegistry;
    
    /**
     * Storage location of ProxyGrantingTickets and Proxy Ticket IOUs.
     */
    private ProxyGrantingTicketStorage proxyGrantingTicketStorage;

    protected void initInternal(final FilterConfig filterConfig) throws ServletException {
        super.initInternal(filterConfig);
        setProxyReceptorUrl(getPropertyFromInitParams(filterConfig, "proxyReceptorUrl", null));
        log.trace("Setting proxyReceptorUrl parameter: " + this.proxyReceptorUrl);
    }

    public void init() {
        super.init();
        
        if (this.cleanUpRegistry == null) {
            this.cleanUpRegistry = CleanUpRegistryImpl.getInstance();
        }
        
        this.proxyGrantingTicketStorage = newProxyGrantingTicketStorage(this.cleanUpRegistry);
        
        CommonUtils.assertNotNull(this.proxyGrantingTicketStorage, "proxyGrantingTicketStorage cannot be null.");
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
        final Cas20ServiceTicketValidator validator;

        if (CommonUtils.isNotBlank(allowAnyProxy) || CommonUtils.isNotBlank(allowedProxyChains)) {
            final Cas20ProxyTicketValidator v = new Cas20ProxyTicketValidator(casServerUrlPrefix);
            v.setAcceptAnyProxy(parseBoolean(allowAnyProxy));
            v.setAllowedProxyChains(new ProxyList(constructListOfProxies(allowedProxyChains)));
            validator = v;
        } else {
            validator = new Cas20ServiceTicketValidator(casServerUrlPrefix);
        }
        validator.setProxyCallbackUrl(getPropertyFromInitParams(filterConfig, "proxyCallbackUrl", null));
        validator.setProxyGrantingTicketStorage(this.proxyGrantingTicketStorage);
        validator.setProxyRetriever(new Cas20ProxyRetriever(casServerUrlPrefix));
        validator.setRenew(parseBoolean(getPropertyFromInitParams(filterConfig, "renew", "false")));

        final Map additionalParameters = new HashMap();
        final List params = Arrays.asList(RESERVED_INIT_PARAMS);

        for (final Enumeration e = filterConfig.getInitParameterNames(); e.hasMoreElements();) {
            final String s = (String) e.nextElement();

            if (!params.contains(s)) {
                additionalParameters.put(s, filterConfig.getInitParameter(s));
            }
        }

        validator.setCustomParameters(additionalParameters);

        return validator;
    }

    protected final List constructListOfProxies(final String proxies) {
        if (CommonUtils.isBlank(proxies)) {
            return new ArrayList();
        }

        final String[] splitProxies = proxies.split("\n");
        final List items = Arrays.asList(splitProxies);
        final ProxyListEditor editor = new ProxyListEditor();
        editor.setValue(items);
        return (List) editor.getValue();
    }

    /**
     * This processes the ProxyReceptor request before the ticket validation code executes.
     */
    protected final boolean preFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;
        final String requestUri = request.getRequestURI();

        if (CommonUtils.isEmpty(this.proxyReceptorUrl) || !requestUri.endsWith(this.proxyReceptorUrl)) {
            return true;
        }

        CommonUtils.readAndRespondToProxyReceptorRequest(request, response, this.proxyGrantingTicketStorage);

        return false;
    }

    public final void setProxyReceptorUrl(final String proxyReceptorUrl) {
        this.proxyReceptorUrl = proxyReceptorUrl;
    }

    protected final void setCleanUpRegistry(final CleanUpRegistry cleanUpRegistry) {
        this.cleanUpRegistry = cleanUpRegistry;
    }
    
    private ProxyGrantingTicketStorage newProxyGrantingTicketStorage(final CleanUpRegistry cleanUpRegistry) {
        ProxyGrantingTicketStorageImpl proxyGrantingTicketStorageImpl = new ProxyGrantingTicketStorageImpl();
        cleanUpRegistry.addCleanble(proxyGrantingTicketStorageImpl);
        
        return proxyGrantingTicketStorageImpl;
    }
}
