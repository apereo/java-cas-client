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
package org.jasig.cas.client.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorage;
import org.jasig.cas.client.ssl.HttpURLConnectionFactory;
import org.jasig.cas.client.ssl.HttpsURLConnectionFactory;
import org.jasig.cas.client.validation.ProxyList;
import org.jasig.cas.client.validation.ProxyListEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common utilities so that we don't need to include Commons Lang.
 *
 * @author Scott Battaglia
 * @version $Revision: 11729 $ $Date: 2007-09-26 14:22:30 -0400 (Tue, 26 Sep 2007) $
 * @since 3.0
 */
public final class CommonUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonUtils.class);

    /**
     * Constant representing the ProxyGrantingTicket IOU Request Parameter.
     */
    private static final String PARAM_PROXY_GRANTING_TICKET_IOU = "pgtIou";

    /**
     * Constant representing the ProxyGrantingTicket Request Parameter.
     */
    private static final String PARAM_PROXY_GRANTING_TICKET = "pgtId";

    private static final HttpURLConnectionFactory DEFAULT_URL_CONNECTION_FACTORY = new HttpsURLConnectionFactory();

    private CommonUtils() {
        // nothing to do
    }

    public static String formatForUtcTime(final Date date) {
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(date);
    }

    /**
     * Check whether the object is null or not. If it is, throw an exception and
     * display the message.
     *
     * @param object  the object to check.
     * @param message the message to display if the object is null.
     */
    public static void assertNotNull(final Object object, final String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Check whether the collection is null or empty. If it is, throw an
     * exception and display the message.
     *
     * @param c       the collection to check.
     * @param message the message to display if the object is null.
     */
    public static void assertNotEmpty(final Collection<?> c, final String message) {
        assertNotNull(c, message);
        if (c.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that the statement is true, otherwise throw an exception with the
     * provided message.
     *
     * @param cond    the codition to assert is true.
     * @param message the message to display if the condition is not true.
     */
    public static void assertTrue(final boolean cond, final String message) {
        if (!cond) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Determines whether the String is null or of length 0.
     *
     * @param string the string to check
     * @return true if its null or length of 0, false otherwise.
     */
    public static boolean isEmpty(final String string) {
        return string == null || string.length() == 0;
    }

    /**
     * Determines if the String is not empty. A string is not empty if it is not
     * null and has a length > 0.
     *
     * @param string the string to check
     * @return true if it is not empty, false otherwise.
     */
    public static boolean isNotEmpty(final String string) {
        return !isEmpty(string);
    }

    /**
     * Determines if a String is blank or not. A String is blank if its empty or
     * if it only contains spaces.
     *
     * @param string the string to check
     * @return true if its blank, false otherwise.
     */
    public static boolean isBlank(final String string) {
        return isEmpty(string) || string.trim().length() == 0;
    }

    /**
     * Determines if a string is not blank. A string is not blank if it contains
     * at least one non-whitespace character.
     *
     * @param string the string to check.
     * @return true if its not blank, false otherwise.
     */
    public static boolean isNotBlank(final String string) {
        return !isBlank(string);
    }

    /**
     * Constructs the URL to use to redirect to the CAS server.
     *
     * @param casServerLoginUrl the CAS Server login url.
     * @param serviceParameterName the name of the parameter that defines the service.
     * @param serviceUrl the actual service's url.
     * @param renew whether we should send renew or not.
     * @param gateway where we should send gateway or not.
     * @return the fully constructed redirect url.
     */
    public static String constructRedirectUrl(final String casServerLoginUrl, final String serviceParameterName,
            final String serviceUrl, final boolean renew, final boolean gateway) {
        return casServerLoginUrl + (casServerLoginUrl.contains("?") ? "&" : "?") + serviceParameterName + "="
                + urlEncode(serviceUrl) + (renew ? "&renew=true" : "") + (gateway ? "&gateway=true" : "");
    }

    /**
     * Url encode a value using UTF-8 encoding.
     * 
     * @param value the value to encode.
     * @return the encoded value.
     */
    public static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static void readAndRespondToProxyReceptorRequest(final HttpServletRequest request,
            final HttpServletResponse response, final ProxyGrantingTicketStorage proxyGrantingTicketStorage)
            throws IOException {
        final String proxyGrantingTicketIou = request.getParameter(PARAM_PROXY_GRANTING_TICKET_IOU);

        final String proxyGrantingTicket = request.getParameter(PARAM_PROXY_GRANTING_TICKET);

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

    protected static String findMatchingServerName(final HttpServletRequest request, final String serverName) {
        final String[] serverNames = serverName.split(" ");

        if (serverNames == null || serverNames.length == 0 || serverNames.length == 1) {
            return serverName;
        }

        final String host = request.getHeader("Host");
        final String xHost = request.getHeader("X-Forwarded-Host");

        final String comparisonHost;
        if (xHost != null && host == "localhost") {
            comparisonHost = xHost;
        } else {
            comparisonHost = host;
        }

        if (comparisonHost == null) {
            return serverName;
        }

        for (final String server : serverNames) {
            final String lowerCaseServer = server.toLowerCase();

            if (lowerCaseServer.contains(comparisonHost)) {
                return server;
            }
        }

        return serverNames[0];
    }

    private static boolean serverNameContainsPort(final boolean containsScheme, final String serverName) {
        if (!containsScheme && serverName.contains(":")) {
            return true;
        }

        final int schemeIndex = serverName.indexOf(":");
        final int portIndex = serverName.lastIndexOf(":");
        return schemeIndex != portIndex;
    }

    private static boolean requestIsOnStandardPort(final HttpServletRequest request) {
        final int serverPort = request.getServerPort();
        return serverPort == 80 || serverPort == 443;
    }

    /**
         * Constructs a service url from the HttpServletRequest or from the given
         * serviceUrl. Prefers the serviceUrl provided if both a serviceUrl and a
         * serviceName.
         *
         * @param request  the HttpServletRequest
         * @param response the HttpServletResponse
         * @param service the configured service url (this will be used if not null)
         * @param serverNames the server name to  use to constuct the service url if the service param is empty.  Note, prior to CAS Client 3.3, this was a single value.
         *           As of 3.3, it can be a space-separated value.  We keep it as a single value, but will convert it to an array internally to get the matching value. This keeps backward compatability with anything using this public
         *           method.
         * @param artifactParameterName the artifact parameter name to remove (i.e. ticket)
         * @param encode whether to encode the url or not (i.e. Jsession).    
         * @return the service url to use.
         */
    public static String constructServiceUrl(final HttpServletRequest request, final HttpServletResponse response,
            final String service, final String serverNames, final String artifactParameterName, final boolean encode) {
        if (CommonUtils.isNotBlank(service)) {
            return encode ? response.encodeURL(service) : service;
        }

        final StringBuilder buffer = new StringBuilder();

        final String serverName = findMatchingServerName(request, serverNames);

        boolean containsScheme = true;
        if (!serverName.startsWith("https://") && !serverName.startsWith("http://")) {
            buffer.append(request.isSecure() ? "https://" : "http://");
            containsScheme = false;
        }

        buffer.append(serverName);

        if (!serverNameContainsPort(containsScheme, serverName) && !requestIsOnStandardPort(request)) {
            buffer.append(":");
            buffer.append(request.getServerPort());
        }

        buffer.append(request.getRequestURI());

        if (CommonUtils.isNotBlank(request.getQueryString())) {
            final int location = request.getQueryString().indexOf(artifactParameterName + "=");

            if (location == 0) {
                final String returnValue = encode ? response.encodeURL(buffer.toString()) : buffer.toString();
                LOGGER.debug("serviceUrl generated: {}", returnValue);
                return returnValue;
            }

            buffer.append("?");

            if (location == -1) {
                buffer.append(request.getQueryString());
            } else if (location > 0) {
                final int actualLocation = request.getQueryString().indexOf("&" + artifactParameterName + "=");

                if (actualLocation == -1) {
                    buffer.append(request.getQueryString());
                } else if (actualLocation > 0) {
                    buffer.append(request.getQueryString().substring(0, actualLocation));
                }
            }
        }

        final String returnValue = encode ? response.encodeURL(buffer.toString()) : buffer.toString();
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
            final List<String> parameters) {
        if ("POST".equals(request.getMethod()) && parameters.contains(parameter)) {
            LOGGER.debug("safeGetParameter called on a POST HttpServletRequest for Restricted Parameters.  Cannot complete check safely.  Reverting to standard behavior for this Parameter");
            return request.getParameter(parameter);
        }
        return request.getQueryString() == null || !request.getQueryString().contains(parameter) ? null : request
                .getParameter(parameter);
    }

    public static String safeGetParameter(final HttpServletRequest request, final String parameter) {
        return safeGetParameter(request, parameter, Arrays.asList("logoutRequest"));
    }


    /**
     * Contacts the remote URL and returns the response.
     *
     * @param constructedUrl the url to contact.
     * @param encoding the encoding to use.
     * @return the response.
     */
    @Deprecated
    public static String getResponseFromServer(final String constructedUrl, final String encoding) {
	try {
        	return getResponseFromServer(new URL(constructedUrl), DEFAULT_URL_CONNECTION_FACTORY, encoding);
	} catch (final Exception e) {
		throw new RuntimeException(e);
	}
    }

    /**
     * Contacts the remote URL and returns the response.
     *
     * @param constructedUrl the url to contact.
     * @param factory connection factory to prepare the URL connection instance
     * @param encoding the encoding to use.
     * @return the response.
     */
    public static String getResponseFromServer(final URL constructedUrl, final HttpURLConnectionFactory factory,
            final String encoding) {

        HttpURLConnection conn = null;
        InputStreamReader in = null;
        try {
            conn = factory.buildHttpURLConnection(constructedUrl.openConnection());

            if (CommonUtils.isEmpty(encoding)) {
                in = new InputStreamReader(conn.getInputStream());
            } else {
                in = new InputStreamReader(conn.getInputStream(), encoding);
            }

            final StringBuilder builder = new StringBuilder(255);
            int byteRead;
            while ((byteRead = in.read()) != -1) {
                builder.append((char) byteRead);
            }

            return builder.toString();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            closeQuietly(in);
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public static ProxyList createProxyList(final String proxies) {
        if (CommonUtils.isBlank(proxies)) {
            return new ProxyList();
        }

        final ProxyListEditor editor = new ProxyListEditor();
        editor.setAsText(proxies);
        return (ProxyList) editor.getValue();
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
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }

    }

    /**
     * Unconditionally close a {@link Closeable}. Equivalent to {@link java.io.Closeable#close()}close(), except any exceptions 
     * will be ignored. This is typically used in finally blocks.
     * @param resource
     */
    public static void closeQuietly(final Closeable resource) {
        try {
            if (resource != null) {
                resource.close();
            }
        } catch (final IOException e) {
            //ignore
        }
    }
}
