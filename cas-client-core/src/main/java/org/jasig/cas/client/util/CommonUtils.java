/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorage;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletRequest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.Collection;

/**
 * Common utilities so that we don't need to include Commons Lang.
 *
 * @author Scott Battaglia
 * @version $Revision: 11729 $ $Date: 2007-09-26 14:22:30 -0400 (Tue, 26 Sep 2007) $
 * @since 3.0
 */
public final class CommonUtils {

    /** Instance of Commons Logging. */
    private static final Log LOG = LogFactory.getLog(CommonUtils.class);
    
    /**
     * Constant representing the ProxyGrantingTicket IOU Request Parameter.
     */
    private static final String PARAM_PROXY_GRANTING_TICKET_IOU = "pgtIou";

    /**
     * Constant representing the ProxyGrantingTicket Request Parameter.
     */
    private static final String PARAM_PROXY_GRANTING_TICKET = "pgtId";

    private CommonUtils() {
        // nothing to do
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
     * @param c       the collecion to check.
     * @param message the message to display if the object is null.
     */
    public static void assertNotEmpty(final Collection c, final String message) {
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
    public static String constructRedirectUrl(final String casServerLoginUrl, final String serviceParameterName, final String serviceUrl, final boolean renew, final boolean gateway) {
        try {
        return casServerLoginUrl + (casServerLoginUrl.indexOf("?") != -1 ? "&" : "?") + serviceParameterName + "="
                    + URLEncoder.encode(serviceUrl, "UTF-8")
                    + (renew ? "&renew=true" : "")
                    + (gateway ? "&gateway=true" : "");
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static void readAndRespondToProxyReceptorRequest(final HttpServletRequest request, final HttpServletResponse response, final ProxyGrantingTicketStorage proxyGrantingTicketStorage) throws IOException {
        final String proxyGrantingTicketIou = request
        .getParameter(PARAM_PROXY_GRANTING_TICKET_IOU);

		final String proxyGrantingTicket = request
		        .getParameter(PARAM_PROXY_GRANTING_TICKET);

		if (CommonUtils.isBlank(proxyGrantingTicket)
		        || CommonUtils.isBlank(proxyGrantingTicketIou)) {
		    response.getWriter().write("");
		    return;
		}

		if (LOG.isDebugEnabled()) {
		    LOG.debug("Received proxyGrantingTicketId ["
		            + proxyGrantingTicket + "] for proxyGrantingTicketIou ["
		            + proxyGrantingTicketIou + "]");
		}

		proxyGrantingTicketStorage.save(proxyGrantingTicketIou,
		        proxyGrantingTicket);
		
		response.getWriter().write("<?xml version=\"1.0\"?>");
		response.getWriter().write("<casClient:proxySuccess xmlns:casClient=\"http://www.yale.edu/tp/casClient\" />");
    }
    
/**
     * Constructs a service url from the HttpServletRequest or from the given
     * serviceUrl. Prefers the serviceUrl provided if both a serviceUrl and a
     * serviceName.
     *
     * @param request  the HttpServletRequest
     * @param response the HttpServletResponse
     * @param service the configured service url (this will be used if not null)
     * @param serverName the server name to  use to constuct the service url if the service param is empty
     * @param artifactParameterName the artifact parameter name to remove (i.e. ticket)
     * @param encode whether to encode the url or not (i.e. Jsession).    
     * @return the service url to use.
     */
    public static String constructServiceUrl(final HttpServletRequest request,
                                               final HttpServletResponse response, final String service, final String serverName, final String artifactParameterName, final boolean encode) {
        if (CommonUtils.isNotBlank(service)) {
            return encode ? response.encodeURL(service) : service;
        }

        final StringBuffer buffer = new StringBuffer();

        synchronized (buffer) {
            if (!serverName.startsWith("https://") && !serverName.startsWith("http://")) {
                buffer.append(request.isSecure() ? "https://" : "http://");
            }

            buffer.append(serverName);
            buffer.append(request.getRequestURI());

            if (CommonUtils.isNotBlank(request.getQueryString())) {
                final int location = request.getQueryString().indexOf(
                        artifactParameterName + "=");

                if (location == 0) {
                    final String returnValue = encode ? response.encodeURL(buffer
                            .toString()): buffer.toString();
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("serviceUrl generated: " + returnValue);
                    }
                    return returnValue;
                }

                buffer.append("?");

                if (location == -1) {
                    buffer.append(request.getQueryString());
                } else if (location > 0) {
                    final int actualLocation = request.getQueryString()
                            .indexOf("&" + artifactParameterName + "=");

                    if (actualLocation == -1) {
                        buffer.append(request.getQueryString());
                    } else if (actualLocation > 0) {
                        buffer.append(request.getQueryString().substring(0,
                                actualLocation));
                    }
                }
            }
        }

        final String returnValue = encode ? response.encodeURL(buffer.toString()) : buffer.toString();
        if (LOG.isDebugEnabled()) {
            LOG.debug("serviceUrl generated: " + returnValue);
        }
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
     *
     * @param request the request to check.
     * @param parameter the parameter to look for.
     * @return the value of the parameter.
     */
    public static String safeGetParameter(final HttpServletRequest request, final String parameter) {
        if ("POST".equals(request.getMethod()) && "logoutRequest".equals(parameter)) {
            LOG.warn("safeGetParameter called on a POST HttpServletRequest for LogoutRequest.  Cannot complete check safely.  Reverting to standard behavior for this Parameter");
            return request.getParameter(parameter);
        }
        return request.getQueryString() == null || request.getQueryString().indexOf(parameter) == -1 ? null : request.getParameter(parameter);       
    }

    /**
     * Contacts the remote URL and returns the response.
     *
     * @param constructedUrl the url to contact.
     * @return the response.
     */
    public static String getResponseFromServer(final URL constructedUrl) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) constructedUrl.openConnection();

            final BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String line;
            final StringBuffer stringBuffer = new StringBuffer(255);

            synchronized (stringBuffer) {
                while ((line = in.readLine()) != null) {
                    stringBuffer.append(line);
                    stringBuffer.append("\n");
                }
                return stringBuffer.toString();
            }
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

    }

    /**
     * Contacts the remote URL and returns the response.
     *
     * @param url the url to contact.
     * @return the response.
     */
    public static String getResponseFromServer(final String url) {
        try {
            return getResponseFromServer(new URL(url));
        } catch (final MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
