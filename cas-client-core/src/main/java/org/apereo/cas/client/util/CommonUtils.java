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

import org.apereo.cas.client.ssl.HttpURLConnectionFactory;
import org.apereo.cas.client.ssl.HttpsURLConnectionFactory;
import org.apereo.cas.client.validation.ProxyList;
import org.apereo.cas.client.validation.ProxyListEditor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;

/**
 * Common utilities so that we don't need to include Commons Lang.
 *
 * @author Scott Battaglia
 * @since 3.0
 */
public final class CommonUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonUtils.class);

    private static final HttpURLConnectionFactory DEFAULT_URL_CONNECTION_FACTORY = new HttpsURLConnectionFactory();

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
     * @param cond    the condition to assert is true.
     * @param message the message to display if the condition is not true.
     */
    public static void assertTrue(final boolean cond, final String message) {
        if (!cond) {
            throw new IllegalArgumentException(message);
        }
    }


    /**
     * Assert that the statement is true, otherwise throw an exception with the
     * provided message.
     *
     * @param cond    the condition to assert is false.
     * @param message the message to display if the condition is not false.
     */
    public static void assertFalse(final boolean cond, final String message) {
        if (cond) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Determines whether the String is null or of length 0.
     *
     * @param string the string to check
     * @return true if its null or length of 0, false otherwise.
     */
    public static boolean isEmpty(final CharSequence string) {
        return string == null || string.isEmpty();
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
        return isEmpty(string) || string.trim().isEmpty();
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
     * @param method the method used by the CAS server to send the user back to the application.
     * @return the fully constructed redirect url.
     */
    public static String constructRedirectUrl(final String casServerLoginUrl, final String serviceParameterName,
                                              final String serviceUrl, final boolean renew, final boolean gateway, final String method) {
        return casServerLoginUrl + (casServerLoginUrl.contains("?") ? "&" : "?") + serviceParameterName + "="
               + urlEncode(serviceUrl) + (renew ? "&renew=true" : "") + (gateway ? "&gateway=true" : "")
               + (method != null ? "&method=" + method : "");
    }

    /**
     * Construct redirect url to a CAS server.
     *
     * @param casServerLoginUrl    the cas server login url
     * @param serviceParameterName the service parameter name
     * @param serviceUrl           the service url
     * @param renew                the renew
     * @param gateway              the gateway
     * @return the string
     */
    public static String constructRedirectUrl(final String casServerLoginUrl, final String serviceParameterName,
                                              final String serviceUrl, final boolean renew, final boolean gateway) {
        return constructRedirectUrl(casServerLoginUrl, serviceParameterName, serviceUrl, renew, gateway, null);
    }

    /**
     * Url encode a value using UTF-8 encoding.
     *
     * @param value the value to encode.
     * @return the encoded value.
     */
    public static String urlEncode(final String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
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
        } catch (final IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Deprecated
    public static String getResponseFromServer(final URL constructedUrl, final String encoding) {
        return getResponseFromServer(constructedUrl, DEFAULT_URL_CONNECTION_FACTORY, encoding);
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

            final var builder = new StringBuilder(255);
            int byteRead;
            while ((byteRead = in.read()) != -1) {
                builder.append((char) byteRead);
            }

            return builder.toString();
        } catch (final RuntimeException e) {
            throw e;
        } catch (final SSLException e) {
            LOGGER.error("SSL error getting response from host: {} : Error Message: {}", constructedUrl.getHost(), e.getMessage(), e);
            throw new RuntimeException(e);
        } catch (final IOException e) {
            LOGGER.error("Error getting response from host: [{}] with path: [{}] and protocol: [{}] Error Message: {}",
                constructedUrl.getHost(), constructedUrl.getPath(), constructedUrl.getProtocol(), e.getMessage(), e);
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

        final var editor = new ProxyListEditor();
        editor.setAsText(proxies);
        return (ProxyList) editor.getValue();
    }

    /**
     * Unconditionally close a {@link Closeable}. Equivalent to {@link Closeable#close()}close(), except any exceptions
     * will be ignored. This is typically used in finally blocks.
     * @param resource the resource to close
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

    /**
     * <p>Converts a String to a boolean (optimised for performance).</p>
     *
     * <p>{@code 'true'}, {@code 'on'}, {@code 'y'}, {@code 't'} or {@code 'yes'}
     * (case insensitive) will return {@code true}. Otherwise,
     * {@code false} is returned.</p>
     *
     * <p>This method performs 4 times faster (JDK1.4) than
     * {@code Boolean.valueOf(String)}. However, this method accepts
     * 'on' and 'yes', 't', 'y' as true values.
     *
     * <pre>
     *   BooleanUtils.toBoolean(null)    = false
     *   BooleanUtils.toBoolean("true")  = true
     *   BooleanUtils.toBoolean("TRUE")  = true
     *   BooleanUtils.toBoolean("tRUe")  = true
     *   BooleanUtils.toBoolean("on")    = true
     *   BooleanUtils.toBoolean("yes")   = true
     *   BooleanUtils.toBoolean("false") = false
     *   BooleanUtils.toBoolean("x gti") = false
     *   BooleanUtils.toBooleanObject("y") = true
     *   BooleanUtils.toBooleanObject("n") = false
     *   BooleanUtils.toBooleanObject("t") = true
     *   BooleanUtils.toBooleanObject("f") = false
     * </pre>
     *
     * @param str  the String to check
     * @return the boolean value of the string, {@code false} if no match or the String is null
     */
    public static boolean toBoolean(final String str) {
        return toBooleanObject(str) == Boolean.TRUE;
    }

    /**
     * <p>Converts a String to a Boolean.</p>
     *
     * <p>{@code 'true'}, {@code 'on'}, {@code 'y'}, {@code 't'} or {@code 'yes'}
     * (case insensitive) will return {@code true}.
     * {@code 'false'}, {@code 'off'}, {@code 'n'}, {@code 'f'} or {@code 'no'}
     * (case insensitive) will return {@code false}.
     * Otherwise, {@code null} is returned.</p>
     *
     * <p>NOTE: This returns null and will throw a NullPointerException if autoboxed to a boolean. </p>
     *
     * <pre>
     *   // N.B. case is not significant
     *   BooleanUtils.toBooleanObject(null)    = null
     *   BooleanUtils.toBooleanObject("true")  = Boolean.TRUE
     *   BooleanUtils.toBooleanObject("T")     = Boolean.TRUE // i.e. T[RUE]
     *   BooleanUtils.toBooleanObject("false") = Boolean.FALSE
     *   BooleanUtils.toBooleanObject("f")     = Boolean.FALSE // i.e. f[alse]
     *   BooleanUtils.toBooleanObject("No")    = Boolean.FALSE
     *   BooleanUtils.toBooleanObject("n")     = Boolean.FALSE // i.e. n[o]
     *   BooleanUtils.toBooleanObject("on")    = Boolean.TRUE
     *   BooleanUtils.toBooleanObject("ON")    = Boolean.TRUE
     *   BooleanUtils.toBooleanObject("off")   = Boolean.FALSE
     *   BooleanUtils.toBooleanObject("oFf")   = Boolean.FALSE
     *   BooleanUtils.toBooleanObject("yes")   = Boolean.TRUE
     *   BooleanUtils.toBooleanObject("Y")     = Boolean.TRUE // i.e. Y[ES]
     *   BooleanUtils.toBooleanObject("blue")  = null
     *   BooleanUtils.toBooleanObject("true ") = null // trailing space (too long)
     *   BooleanUtils.toBooleanObject("ono")   = null // does not match on or no
     * </pre>
     *
     * @param str  the String to check; upper and lower case are treated as the same
     * @return the Boolean value of the string, {@code null} if no match or {@code null} input
     */
    public static Boolean toBooleanObject(final CharSequence str) {
        // Previously used equalsIgnoreCase, which was fast for interned 'true'.
        // Non interned 'true' matched 15 times slower.
        //
        // Optimisation provides same performance as before for interned 'true'.
        // Similar performance for null, 'false', and other strings not length 2/3/4.
        // 'true'/'TRUE' match 4 times slower, 'tRUE'/'True' 7 times slower.
        if (str == "true") {
            return Boolean.TRUE;
        }
        if (str == null) {
            return null;
        }
        switch (str.length()) {
            case 1: {
                final var ch0 = str.charAt(0);
                if (ch0 == 'y' || ch0 == 'Y' ||
                    ch0 == 't' || ch0 == 'T') {
                    return Boolean.TRUE;
                }
                if (ch0 == 'n' || ch0 == 'N' ||
                    ch0 == 'f' || ch0 == 'F') {
                    return Boolean.FALSE;
                }
                break;
            }
            case 2: {
                final var ch0 = str.charAt(0);
                final var ch1 = str.charAt(1);
                if ((ch0 == 'o' || ch0 == 'O') &&
                    (ch1 == 'n' || ch1 == 'N')) {
                    return Boolean.TRUE;
                }
                if ((ch0 == 'n' || ch0 == 'N') &&
                    (ch1 == 'o' || ch1 == 'O')) {
                    return Boolean.FALSE;
                }
                break;
            }
            case 3: {
                final var ch0 = str.charAt(0);
                final var ch1 = str.charAt(1);
                final var ch2 = str.charAt(2);
                if ((ch0 == 'y' || ch0 == 'Y') &&
                    (ch1 == 'e' || ch1 == 'E') &&
                    (ch2 == 's' || ch2 == 'S')) {
                    return Boolean.TRUE;
                }
                if ((ch0 == 'o' || ch0 == 'O') &&
                    (ch1 == 'f' || ch1 == 'F') &&
                    (ch2 == 'f' || ch2 == 'F')) {
                    return Boolean.FALSE;
                }
                break;
            }
            case 4: {
                final var ch0 = str.charAt(0);
                final var ch1 = str.charAt(1);
                final var ch2 = str.charAt(2);
                final var ch3 = str.charAt(3);
                if ((ch0 == 't' || ch0 == 'T') &&
                    (ch1 == 'r' || ch1 == 'R') &&
                    (ch2 == 'u' || ch2 == 'U') &&
                    (ch3 == 'e' || ch3 == 'E')) {
                    return Boolean.TRUE;
                }
                break;
            }
            case 5: {
                final var ch0 = str.charAt(0);
                final var ch1 = str.charAt(1);
                final var ch2 = str.charAt(2);
                final var ch3 = str.charAt(3);
                final var ch4 = str.charAt(4);
                if ((ch0 == 'f' || ch0 == 'F') &&
                    (ch1 == 'a' || ch1 == 'A') &&
                    (ch2 == 'l' || ch2 == 'L') &&
                    (ch3 == 's' || ch3 == 'S') &&
                    (ch4 == 'e' || ch4 == 'E')) {
                    return Boolean.FALSE;
                }
                break;
            }
            default:
                break;
        }

        return null;
    }

    /**
     * <p>Convert a <code>String</code> to a <code>long</code>, returning a
     * default value if the conversion fails.</p>
     *
     * <p>If the string is <code>null</code>, the default value is returned.</p>
     *
     * <pre>
     *   NumberUtils.toLong(null, 1L) = 1L
     *   NumberUtils.toLong("", 1L)   = 1L
     *   NumberUtils.toLong("1", 0L)  = 1L
     * </pre>
     *
     * @param str  the string to convert, may be null
     * @param defaultValue  the default value
     * @return the long represented by the string, or the default if conversion fails
     */
    public static long toLong(final String str, final long defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(str);
        } catch (final NumberFormatException nfe) {
            return defaultValue;
        }
    }

    /**
     * <p>Convert a <code>String</code> to an <code>int</code>, returning a
     * default value if the conversion fails.</p>
     *
     * <p>If the string is <code>null</code>, the default value is returned.</p>
     *
     * <pre>
     *   NumberUtils.toInt(null, 1) = 1
     *   NumberUtils.toInt("", 1)   = 1
     *   NumberUtils.toInt("1", 0)  = 1
     * </pre>
     *
     * @param str  the string to convert, may be null
     * @param defaultValue  the default value
     * @return the int represented by the string, or the default if conversion fails
     */
    public static int toInt(final String str, final int defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(str);
        } catch (final NumberFormatException nfe) {
            return defaultValue;
        }
    }

    /**
     * Returns the string as-is, unless it's <code>null</code>;
     * in this case an empty string is returned.
     *
     * @param string a possibly <code>null</code> string
     * @return a non-<code>null</code> string
     */
    public static String nullToEmpty(final String string) {
        return string == null ? "" : string;
    }

    /**
     * Adds a trailing slash to the given uri, if it doesn't already have one.
     *
     * @param uri a string that may or may not end with a slash
     * @return the same string, except with a slash suffix (if necessary).
     */
    public static String addTrailingSlash(final String uri) {
        return uri.endsWith("/") ? uri : uri + "/";
    }
}
