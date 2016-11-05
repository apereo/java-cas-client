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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.net.ssl.HostnameVerifier;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.client.Protocol;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.jasig.cas.client.configuration.ConfigurationKeys;
import org.jasig.cas.client.ssl.HttpsURLConnectionFactory;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.util.ReflectUtils;
import org.jasig.cas.client.util.XmlUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

/**
 * The filter that handles all the work of validating ticket requests.
 * <p>
 * This filter can be configured with the following values:
 * <ul>
 * <li><code>redirectAfterValidation</code> - redirect the CAS client to the same URL without the ticket.
 * (default: true, Will be forced to false when {@link #useSession} is false.)</li>
 * <li><code>exceptionOnValidationFailure</code> - throw an exception if the validation fails.  Otherwise, continue
 *  processing. (default: true)</li>
 * <li><code>useSession</code> - store any of the useful information in a session attribute. (default: true)</li>
 * <li><code>hostnameVerifier</code> - name of class implementing a {@link HostnameVerifier}.</li>
 * <li><code>hostnameVerifierConfig</code> - name of configuration class (constructor argument of verifier).</li>
 * </ul>
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public abstract class AbstractTicketValidationFilter extends AbstractCasFilter {

    /** The TicketValidator we will use to validate tickets. */
    private TicketValidator ticketValidator;

    /**
     * Specify whether the filter should redirect the user agent after a
     * successful validation to remove the ticket parameter from the query
     * string.
     */
    private boolean redirectAfterValidation = true;

    /** Determines whether an exception is thrown when there is a ticket validation failure. */
    private boolean exceptionOnValidationFailure = false;

    /**
     * Specify whether the Assertion should be stored in a session
     * attribute {@link AbstractCasFilter#CONST_CAS_ASSERTION}.
     */
    private boolean useSession = true;

    protected AbstractTicketValidationFilter(final Protocol protocol) {
        super(protocol);
    }

    /**
     * Template method to return the appropriate validator.
     *
     * @param filterConfig the FilterConfiguration that may be needed to construct a validator.
     * @return the ticket validator.
     */
    protected TicketValidator getTicketValidator(final FilterConfig filterConfig) {
        return this.ticketValidator;
    }

    /**
     * Gets the ssl config to use for HTTPS connections
     * if one is configured for this filter.
     * @return Properties that can contains key/trust info for Client Side Certificates
     */
    protected Properties getSSLConfig() {
        final Properties properties = new Properties();
        final String fileName = getString(ConfigurationKeys.SSL_CONFIG_FILE);

        if (fileName != null) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(fileName);
                properties.load(fis);
                logger.trace("Loaded {} entries from {}", properties.size(), fileName);
            } catch (final IOException ioe) {
                logger.error(ioe.getMessage(), ioe);
            } finally {
                CommonUtils.closeQuietly(fis);
            }
        }
        return properties;
    }

    /**
     * Gets the configured {@link HostnameVerifier} to use for HTTPS connections
     * if one is configured for this filter.
     * @return Instance of specified host name verifier or null if none specified.
     */
    protected HostnameVerifier getHostnameVerifier() {
        final Class<? extends HostnameVerifier> className = getClass(ConfigurationKeys.HOSTNAME_VERIFIER);
        final String config = getString(ConfigurationKeys.HOSTNAME_VERIFIER_CONFIG);
        if (className != null) {
            if (config != null) {
                return ReflectUtils.newInstance(className, config);
            } else {
                return ReflectUtils.newInstance(className);
            }
        }
        return null;
    }

    protected void initInternal(final FilterConfig filterConfig) throws ServletException {
        setExceptionOnValidationFailure(getBoolean(ConfigurationKeys.EXCEPTION_ON_VALIDATION_FAILURE));
        setRedirectAfterValidation(getBoolean(ConfigurationKeys.REDIRECT_AFTER_VALIDATION));
        setUseSession(getBoolean(ConfigurationKeys.USE_SESSION));

        if (!this.useSession && this.redirectAfterValidation) {
            logger.warn("redirectAfterValidation parameter may not be true when useSession parameter is false. Resetting it to false in order to prevent infinite redirects.");
            setRedirectAfterValidation(false);
        }

        setTicketValidator(getTicketValidator(filterConfig));
        super.initInternal(filterConfig);
    }

    public void init() {
        super.init();
        CommonUtils.assertNotNull(this.ticketValidator, "ticketValidator cannot be null.");
    }

    /**
     * Pre-process the request before the normal filter process starts.  This could be useful for pre-empting code.
     *
     * @param servletRequest The servlet request.
     * @param servletResponse The servlet response.
     * @param filterChain the filter chain.
     * @return true if processing should continue, false otherwise.
     * @throws IOException if there is an I/O problem
     * @throws ServletException if there is a servlet problem.
     */
    protected boolean preFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
            final FilterChain filterChain) throws IOException, ServletException {
        return true;
    }

    /**
     * Template method that gets executed if ticket validation succeeds.  Override if you want additional behavior to occur
     * if ticket validation succeeds.  This method is called after all ValidationFilter processing required for a successful authentication
     * occurs.
     *
     * @param request the HttpServletRequest.
     * @param response the HttpServletResponse.
     * @param assertion the successful Assertion from the server.
     */
    protected void onSuccessfulValidation(final HttpServletRequest request, final HttpServletResponse response,
            final Assertion assertion) {
        // nothing to do here.
    }

    /**
     * Template method that gets executed if validation fails.  This method is called right after the exception is caught from the ticket validator
     * but before any of the processing of the exception occurs.
     *
     * @param request the HttpServletRequest.
     * @param response the HttpServletResponse.
     */
    protected void onFailedValidation(final HttpServletRequest request, final HttpServletResponse response) {
        // nothing to do here.
    }

    public final void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
            final FilterChain filterChain) throws IOException, ServletException {

        if (!preFilter(servletRequest, servletResponse, filterChain)) {
            return;
        }

        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;
        final String ticket = retrieveTicketFromRequest(request);

        if (CommonUtils.isNotBlank(ticket)) {
            logger.debug("Attempting to validate ticket: {}", ticket);

            try {
                final Assertion assertion = this.ticketValidator.validate(ticket,
                        constructServiceUrl(request, response));

                logger.debug("Successfully authenticated user: {}", assertion.getPrincipal().getName());

                request.setAttribute(CONST_CAS_ASSERTION, assertion);

                if (this.useSession) {
                    request.getSession().setAttribute(CONST_CAS_ASSERTION, assertion);
                }
                onSuccessfulValidation(request, response, assertion);

                if (this.redirectAfterValidation) {
                    logger.debug("Redirecting after successful ticket validation.");
                    response.sendRedirect(constructServiceUrl(request, response));
                    return;
                }
            } catch (final TicketValidationException e) {
                logger.debug(e.getMessage(), e);

                onFailedValidation(request, response);

                if (this.exceptionOnValidationFailure) {
                    throw new ServletException(e);
                }

                response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());

                return;
            }
        }
        
        // Check if there is an access token in the request header.
        if (!(checkAccessToken(request, response))) {
        	return;
        }

        filterChain.doFilter(request, response);
    
    }
    
    /**
     * Check if there is an access token in the request header, and if is do the same as the ticket does.
     * 
     * @param request the object of HttpServletRequest.
     * @param response the object of HttpServletResponse.
     * @return whether it needs to go the the next filter or not.
     * @throws IOException
     * @throws ServletException
     */
    private final boolean checkAccessToken(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
		final String authHeader = request.getHeader("Authorization");
		if (!CommonUtils.isBlank(authHeader) && authHeader.toLowerCase().startsWith("Bearer".toLowerCase() + ' ')) {
			final String accessToken = authHeader.substring("Bearer".length() + 1);
			logger.debug("{}: {}", "access token", accessToken);

			try {
				logger.debug("Retrieving response from server.");
				String serverName = request.getServerName();
				int pos = serverName.indexOf(".");
				if (pos > 0 && pos < serverName.length() - 1) {
					serverName = String.format("accounts.%s", serverName.substring(pos + 1));
				}
				logger.debug("Server name {}", serverName);
				StringBuilder builder = new StringBuilder();
				logger.debug("Request scheme {}", request.getScheme());
				// The request scheme should be HTTP since it's used internally.
				builder
					.append(request.getScheme()).append("://")
					.append(serverName)
					.append(":").append(request.getServerPort());
				
				// Create a variable to use it when requesting PT below.
				final String serverNameWithPort = builder.toString();
				logger.debug("Server name with port {}", serverNameWithPort);
				builder
					.append("/auth/oauth2.0/profile?access_token=")
					.append(accessToken);
				logger.debug("CAS Profile URL {}", builder.toString());
				String serverResponse = CommonUtils.getResponseFromServer(new URL(builder.toString()),
						new HttpsURLConnectionFactory(), getString(ConfigurationKeys.ENCODING));
				
				logger.debug("CAS Profile Server response: {}", serverResponse);

				final JsonParser parser = new JsonParser();
				final JsonReader reader = new JsonReader(new StringReader(serverResponse.trim()));
				reader.setLenient(true);
				final JsonObject responseFromServer = (JsonObject) parser.parse(reader);
				Iterator<Entry<String, JsonElement>> itr = responseFromServer.entrySet().iterator();

				// getting an assertion
				final Assertion assertion;
				itr = responseFromServer.entrySet().iterator();
				String principal = null;
				final Map<String, Object> attributes = new HashMap<String, Object>();
				String tgtId = null;
				while (itr.hasNext()) {
					Entry<String, JsonElement> entry = itr.next();
					if (entry.getKey().equalsIgnoreCase("error")) {
						throw new TicketValidationException(entry.getValue().getAsString());
					}
					if (entry.getKey().equalsIgnoreCase("id")) {
						principal = entry.getValue().getAsString();
					} else if (entry.getKey().equalsIgnoreCase("attributes")) {
						final JsonArray attributesArray = entry.getValue().getAsJsonArray();
						final Iterator<JsonElement> attrItr = attributesArray.iterator();
						while (attrItr.hasNext()) {
							JsonObject jo = (JsonObject) attrItr.next();
							final Iterator<Entry<String, JsonElement>> joItr = jo.entrySet().iterator();
							while (joItr.hasNext()) {
								Entry<String, JsonElement> attr = joItr.next();
								attributes.put(attr.getKey(), attr.getValue().getAsString());
							}
						}
					} else if (entry.getKey().equalsIgnoreCase("tgtId")) {
						tgtId = entry.getValue().getAsString();
					}
				}
				
				logger.debug("Principal {}", principal);
				
				// Use ticket granting ticket to retrieve a service ticket.
				CommonUtils.assertNotNull(tgtId, "Ticket granting ticket can't be null");
				logger.debug("TGT ID {}", tgtId);
				String requestUri = String.format("/auth/v1/tickets/%s", tgtId);
				builder = new StringBuilder();
				builder
					.append(serverNameWithPort)
					.append(requestUri);
				final Map<String, String> postParams = new HashMap<String, String>();
				postParams.put("service", request.getRequestURL().toString());
				logger.debug("TGT request URL {}", requestUri);
				final String serviceTicket = CommonUtils.getResponseFromServer(new URL(builder.toString()),
						new HttpsURLConnectionFactory(), getString(ConfigurationKeys.ENCODING), null, postParams);
				CommonUtils.assertNotNull(serviceTicket, "Service ticket can't be null");
				logger.debug("Service ticket {}", serviceTicket);
				builder = new StringBuilder();
				requestUri = String
						.format("/auth/p3/proxyValidate?ticket=%s&service=%s&pgtUrl=%s", serviceTicket, request.getRequestURL(), 
								getString(ConfigurationKeys.PROXY_CALLBACK_URL));
				builder
					.append(serverNameWithPort)
					.append(requestUri);
				
				// Create a map for header information and put an extra command to remove the service ticket after PT is created.
				final Map<String, String> headers = new HashMap<String, String>();
				headers.put("x-command", "rm-st");
				logger.debug("Proxy validate URL {}", builder.toString());
				final String validateResponse = CommonUtils.getResponseFromServer(new URL(builder.toString()),
						new HttpsURLConnectionFactory(), getString(ConfigurationKeys.ENCODING), headers);
				logger.debug("Proxy validate response {}", validateResponse);
				final String proxyGrantingTicketIou = XmlUtils.getTextForElement(validateResponse, "proxyGrantingTicket");
				logger.debug("Proxy Granting Ticket IOU {}", proxyGrantingTicketIou);
				final String proxyGrantingTicket;
				
				// Cast this class to use the method implemented in the parent class.
				final Cas30ProxyTicketValidator validator = (Cas30ProxyTicketValidator) this.ticketValidator;
		        if (CommonUtils.isBlank(proxyGrantingTicketIou) || validator.getProxyGrantingTicketStorage() == null) {
		            proxyGrantingTicket = null;
		        } else {
		            proxyGrantingTicket = validator.getProxyGrantingTicketStorage().retrieve(proxyGrantingTicketIou);
		        }
		        
		        CommonUtils.assertNotNull(proxyGrantingTicket, "Proxy Granting Ticket cannot be null");
		        logger.debug("Proxy Granting Ticket {}", proxyGrantingTicket);
		        
				if (principal == null || CommonUtils.isBlank(principal)) {
					throw new TicketValidationException("No principal was found in the response from the CAS server.");
				}
				assertion = new AssertionImpl(new AttributePrincipalImpl(principal, attributes,
			            proxyGrantingTicket, validator.getCas20ProxyRetriever()));
				request.setAttribute(CONST_CAS_ASSERTION, assertion);

				if (this.useSession) {
					request.getSession().setAttribute(CONST_CAS_ASSERTION, assertion);
				}

				if (this.redirectAfterValidation) {
					logger.debug("Redirecting after successful ticket validation.");
					response.sendRedirect(constructServiceUrl(request, response));
					return false;
				}
			} catch (final TicketValidationException e) {
				logger.debug(e.getMessage(), e);

				onFailedValidation(request, response);

				if (this.exceptionOnValidationFailure) {
					throw new ServletException(e);
				}

				response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());

				return false;
			}

		}
		return true;
    }

    public final void setTicketValidator(final TicketValidator ticketValidator) {
        this.ticketValidator = ticketValidator;
    }

    public final void setRedirectAfterValidation(final boolean redirectAfterValidation) {
        this.redirectAfterValidation = redirectAfterValidation;
    }

    public final void setExceptionOnValidationFailure(final boolean exceptionOnValidationFailure) {
        this.exceptionOnValidationFailure = exceptionOnValidationFailure;
    }

    public final void setUseSession(final boolean useSession) {
        this.useSession = useSession;
    }
}