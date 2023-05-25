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
package org.apereo.cas.client.util;

import org.apereo.cas.client.Protocol;
import org.apereo.cas.client.proxy.ProxyGrantingTicketStorage;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Commons utilities related to the Jakarta request/response.
 *
 * @author Jerome LELEU
 * @since 4.0.3
 */
public final class WebUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebUtils.class);

    /**
     * Constant representing the ProxyGrantingTicket IOU Request Parameter.
     */
    private static final String PARAM_PROXY_GRANTING_TICKET_IOU = "pgtIou";

    /**
     * Constant representing the ProxyGrantingTicket Request Parameter.
     */
    private static final String PARAM_PROXY_GRANTING_TICKET = "pgtId";

    private static final String SERVICE_PARAMETER_NAMES;

    static {
        final Collection<String> serviceParameterSet = new HashSet<>(4);
        for (final var protocol : Protocol.values()) {
            serviceParameterSet.add(protocol.getServiceParameterName());
        }
        SERVICE_PARAMETER_NAMES = serviceParameterSet.toString()
                .replaceAll("\\[|\\]", "")
                .replaceAll("\\s", "");
    }

    public static void readAndRespondToProxyReceptorRequest(final ServletRequest request,
                                                            final ServletResponse response, final ProxyGrantingTicketStorage proxyGrantingTicketStorage)
        throws IOException {
        final var proxyGrantingTicketIou = request.getParameter(PARAM_PROXY_GRANTING_TICKET_IOU);

        final var proxyGrantingTicket = request.getParameter(PARAM_PROXY_GRANTING_TICKET);

        if (CommonUtils.isBlank(proxyGrantingTicket) || CommonUtils.isBlank(proxyGrantingTicketIou)) {
            response.getWriter().write("");
            return;
        }

        LOGGER.debug("Received proxyGrantingTicketId [{}] for proxyGrantingTicketIou [{}]", proxyGrantingTicket,
            proxyGrantingTicketIou);

        proxyGrantingTicketStorage.save(proxyGrantingTicketIou, proxyGrantingTicket);

        LOGGER.debug("Successfully saved proxyGrantingTicketId [{}] for proxyGrantingTicketIou [{}]",
            proxyGrantingTicket, proxyGrantingTicketIou);

        response.getWriter().write("<?xml version=\"1.0\"?>");
        response.getWriter().write("<casClient:proxySuccess xmlns:casClient=\"http://www.yale.edu/tp/casClient\" />");
    }

    private static String findMatchingServerName(final HttpServletRequest request, final String serverName) {
        final var serverNames = serverName.split(" ");

        if (serverNames.length == 0 || serverNames.length == 1) {
            return serverName;
        }

        final var host = request.getHeader("Host");
        final var xHost = request.getHeader("X-Forwarded-Host");

        final String comparisonHost;
        comparisonHost = (xHost != null) ? xHost : host;

        if (comparisonHost == null) {
            return serverName;
        }

        for (final var server : serverNames) {
            final var lowerCaseServer = server.toLowerCase();

            if (lowerCaseServer.contains(comparisonHost)) {
                return server;
            }
        }

        return serverNames[0];
    }

    private static boolean requestIsOnStandardPort(final ServletRequest request) {
        final var serverPort = request.getServerPort();
        return serverPort == 80 || serverPort == 443;
    }

    /**
     * Constructs a service url from the HttpServletRequest or from the given
     * serviceUrl. Prefers the serviceUrl provided if both a serviceUrl and a
     * serviceName. Compiles a list of all service parameters for supported protocols
     * and removes them all from the query string.
     *
     * @param request the HttpServletRequest
     * @param response the HttpServletResponse
     * @param service the configured service url (this will be used if not null)
     * @param serverNames the server name to  use to construct the service url if the service param is empty.  Note, prior to CAS Client 3.3, this was a single value.
     *           As of 3.3, it can be a space-separated value.  We keep it as a single value, but will convert it to an array internally to get the matching value. This keeps backward compatability with anything using this public
     *           method.
     * @param artifactParameterName the artifact parameter name to remove (i.e. ticket)
     * @param encode whether to encode the url or not (i.e. Jsession).
     * @return the service url to use.
     */
    @Deprecated
    public static String constructServiceUrl(final HttpServletRequest request, final HttpServletResponse response,
                                             final String service, final String serverNames,
                                             final String artifactParameterName, final boolean encode) {
        return constructServiceUrl(request, response, service, serverNames, SERVICE_PARAMETER_NAMES
            , artifactParameterName, encode);
    }

    /**
     * Constructs a service url from the HttpServletRequest or from the given
     * serviceUrl. Prefers the serviceUrl provided if both a serviceUrl and a
     * serviceName.
     *
     * @param request the HttpServletRequest
     * @param response the HttpServletResponse
     * @param service the configured service url (this will be used if not null)
     * @param serverNames the server name to  use to construct the service url if the service param is empty.  Note, prior to CAS Client 3.3, this was a single value.
     *           As of 3.3, it can be a space-separated value.  We keep it as a single value, but will convert it to an array internally to get the matching value. This keeps backward compatability with anything using this public
     *           method.
     * @param serviceParameterName the service parameter name to remove (i.e. service)
     * @param artifactParameterName the artifact parameter name to remove (i.e. ticket)
     * @param encode whether to encode the url or not (i.e. Jsession).
     * @return the service url to use.
     */
    public static String constructServiceUrl(final HttpServletRequest request, final HttpServletResponse response,
                                             final String service, final String serverNames, final String serviceParameterName,
                                             final String artifactParameterName, final boolean encode) {
        if (CommonUtils.isNotBlank(service)) {
            return encode ? response.encodeURL(service) : service;
        }

        final var serverName = findMatchingServerName(request, serverNames);
        final var originalRequestUrl = new URIBuilder(request.getRequestURL().toString(), encode);
        originalRequestUrl.setParameters(request.getQueryString());

        final URIBuilder builder;
        if (!serverName.startsWith("https://") && !serverName.startsWith("http://")) {
            final var scheme = request.isSecure() ? "https://" : "http://";
            builder = new URIBuilder(scheme + serverName, encode);
        } else {
            builder = new URIBuilder(serverName, encode);
        }

        if (builder.getPort() == -1 && !requestIsOnStandardPort(request)) {
            builder.setPort(request.getServerPort());
        }

        builder.setEncodedPath(builder.getEncodedPath() + request.getRequestURI());

        final var serviceParameterNames = Arrays.asList(serviceParameterName.split(","));
        if (!serviceParameterNames.isEmpty() && !originalRequestUrl.getQueryParams().isEmpty()) {
            for (final var pair : originalRequestUrl.getQueryParams()) {
                final var name = pair.name();
                if (!name.equals(artifactParameterName) && !serviceParameterNames.contains(name)) {
                    if (name.contains("&") || name.contains("=")) {
                        final var encodedParamBuilder = new URIBuilder();
                        encodedParamBuilder.setParameters(name);
                        for (final var pair2 : encodedParamBuilder.getQueryParams()) {
                            final var name2 = pair2.name();
                            if (!name2.equals(artifactParameterName) && !serviceParameterNames.contains(name2)) {
                                builder.addParameter(name2, pair2.value());
                            }
                        }
                    } else {
                        builder.addParameter(name, pair.value());
                    }
                }
            }
        }

        final var result = builder.toString();
        final var returnValue = encode ? response.encodeURL(result) : result;
        LOGGER.debug("serviceUrl generated: {}", returnValue);
        return returnValue;
    }

    /**
     * Safe method for retrieving a parameter from the request without disrupting the reader UNLESS the parameter
     * actually exists in the query string.
     * <p>
     * Note, this does not work for POST Requests for "logoutRequest".  It works for all other CAS POST requests because the
     * parameter is ALWAYS in the GET request.
     * <p>
     * If we see the "logoutRequest" parameter we MUST treat it as if calling the standard request.getParameter.
     * <p>
     *     Note, that as of 3.3.0, we've made it more generic.
     * </p>
     *
     * @param request the request to check.
     * @param parameter the parameter to look for.
     * @return the value of the parameter.
     */
    public static String safeGetParameter(final HttpServletRequest request, final String parameter,
                                          final Collection<String> parameters) {
        if ("POST".equals(request.getMethod()) && parameters.contains(parameter)) {
            LOGGER.debug("safeGetParameter called on a POST HttpServletRequest for Restricted Parameters.  Cannot complete check safely.  Reverting to standard behavior for this Parameter");
            return request.getParameter(parameter);
        }
        return request.getQueryString() == null || !request.getQueryString().contains(parameter) ? null : request
            .getParameter(parameter);
    }

    public static String safeGetParameter(final HttpServletRequest request, final String parameter) {
        return safeGetParameter(request, parameter, List.of("logoutRequest"));
    }

    /**
     * Sends the redirect message and captures the exceptions that we can't possibly do anything with.
     *
     * @param response the HttpServletResponse.  CANNOT be NULL.
     * @param url the url to redirect to.
     */
    public static void sendRedirect(final HttpServletResponse response, final String url) {
        try {
            response.sendRedirect(url);
        } catch (final IOException e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }
}
