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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A utility class borrowed from apache http-client to build uris.
 *
 * @author Misagh Moayyed
 * @since 3.4
 */
public final class URIBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(URIBuilder.class);
    private static final Pattern IPV6_STD_PATTERN = Pattern.compile("^[0-9a-fA-F]{1,4}(:[0-9a-fA-F]{1,4}){7}$");

    private String scheme;
    private String encodedSchemeSpecificPart;
    private String encodedAuthority;
    private String userInfo;
    private String encodedUserInfo;
    private String host;
    private int port;
    private String path;
    private String encodedPath;
    private String encodedQuery;
    private List<BasicNameValuePair> queryParams;
    private String query;
    private boolean encode;
    private String fragment;
    private String encodedFragment;

    /**
     * Constructs an empty instance.
     */
    public URIBuilder() {
        super();
        this.port = -1;
    }

    public URIBuilder(final boolean encode) {
        this();
        setEncode(encode);
    }

    /**
     * Construct an instance from the string which must be a valid URI.
     *
     * @param string a valid URI in string form
     * @throws RuntimeException if the input is not a valid URI
     */
    public URIBuilder(final String string) {
        super();
        try {
            digestURI(new URI(string));
        } catch (final URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public URIBuilder(final String string, boolean encode) {
        super();
        try {
            setEncode(encode);
            digestURI(new URI(string));
        } catch (final URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    /**
     * Construct an instance from the provided URI.
     *
     * @param uri the uri to digest
     */
    public URIBuilder(final URI uri) {
        super();
        digestURI(uri);
    }

    private List<BasicNameValuePair> parseQuery(final String query) {

        try {
            final Charset utf8 = Charset.forName("UTF-8");
            if (query != null && !query.isEmpty()) {
                final List<BasicNameValuePair> list = new ArrayList<BasicNameValuePair>();
                final String[] parametersArray = query.split("&");

                for (final String parameter : parametersArray) {
                    final int firstIndex = parameter.indexOf("=");
                    if (firstIndex != -1) {
                        final String paramName = parameter.substring(0, firstIndex);
                        final String decodedParamName = URLDecoder.decode(paramName, utf8.name());

                        final String paramVal = parameter.substring(firstIndex + 1);
                        final String decodedParamVal = URLDecoder.decode(paramVal, utf8.name());

                        list.add(new BasicNameValuePair(decodedParamName, decodedParamVal));
                    } else {
                        // Either we do not have a query parameter, or it might be encoded; take it verbaitm
                        final String[] parameterCombo = parameter.split("=");
                        if (parameterCombo.length >= 1) {
                            final String key = URLDecoder.decode(parameterCombo[0], utf8.name());
                            final String val = parameterCombo.length == 2 ? URLDecoder.decode(parameterCombo[1], utf8.name()) : "";
                            list.add(new BasicNameValuePair(key, val));
                        }
                    }
                }
                return list;
            }
        } catch (final UnsupportedEncodingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ArrayList<BasicNameValuePair>();
    }

    /**
     * Builds a {@link URI} instance.
     */
    public URI build() {
        try {
            return new URI(buildString());
        } catch (final URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isIPv6Address(final String input) {
        return IPV6_STD_PATTERN.matcher(input).matches();
    }

    private String buildString() {
        final StringBuilder sb = new StringBuilder();
        if (this.scheme != null) {
            sb.append(this.scheme).append(':');
        }
        if (this.encodedSchemeSpecificPart != null) {
            sb.append(this.encodedSchemeSpecificPart);
        } else {
            if (this.encodedAuthority != null) {
                sb.append("//").append(this.encodedAuthority);
            } else if (this.host != null) {
                sb.append("//");
                if (this.encodedUserInfo != null) {
                    sb.append(this.encodedUserInfo).append("@");
                } else if (this.userInfo != null) {
                    sb.append(encodeUserInfo(this.userInfo)).append("@");
                }
                if (isIPv6Address(this.host)) {
                    sb.append("[").append(this.host).append("]");
                } else {
                    sb.append(this.host);
                }
                if (this.port >= 0) {
                    sb.append(":").append(this.port);
                }
            }
            if (this.encodedPath != null) {
                sb.append(normalizePath(this.encodedPath));
            } else if (this.path != null) {
                sb.append(encodePath(normalizePath(this.path)));
            }
            if (this.encodedQuery != null) {
                sb.append("?").append(this.encodedQuery);
            } else if (this.queryParams != null && !this.queryParams.isEmpty()) {
                sb.append("?").append(encodeUrlForm(this.queryParams));
            } else if (this.query != null) {
                sb.append("?").append(encodeUric(this.query));
            }
        }
        if (this.encodedFragment != null) {
            sb.append("#").append(this.encodedFragment);
        } else if (this.fragment != null) {
            sb.append("#").append(encodeUric(this.fragment));
        }
        return sb.toString();
    }

    public URIBuilder digestURI(final URI uri) {
        this.scheme = uri.getScheme();
        this.encodedSchemeSpecificPart = uri.getRawSchemeSpecificPart();
        this.encodedAuthority = uri.getRawAuthority();
        this.host = uri.getHost();
        this.port = uri.getPort();
        this.encodedUserInfo = uri.getRawUserInfo();
        this.userInfo = uri.getUserInfo();
        this.encodedPath = uri.getRawPath();
        this.path = uri.getPath();
        this.encodedQuery = uri.getRawQuery();
        this.queryParams = parseQuery(uri.getRawQuery());
        this.encodedFragment = uri.getRawFragment();
        this.fragment = uri.getFragment();
        return this;
    }

    private String encodeUserInfo(final String userInfo) {
        return this.encode ? CommonUtils.urlEncode(userInfo) : userInfo;
    }

    private String encodePath(final String path) {
        return this.encode ? CommonUtils.urlEncode(path) : path;
    }

    private String encodeUrlForm(final List<BasicNameValuePair> params) {
        final StringBuilder result = new StringBuilder();
        for (final BasicNameValuePair parameter : params) {
            final String encodedName = this.encode ? CommonUtils.urlEncode(parameter.getName()) : parameter.getName();
            final String encodedValue = this.encode ? CommonUtils.urlEncode(parameter.getValue()) : parameter.getValue();

            if (result.length() > 0) {
                result.append("&");
            }
            result.append(encodedName);
            if (encodedValue != null) {
                result.append("=");
                result.append(encodedValue);
            }
        }
        return result.toString();
    }

    private String encodeUric(final String fragment) {
        return this.encode ? CommonUtils.urlEncode(fragment) : fragment;
    }

    public URIBuilder setEncode(boolean encode) {
        this.encode = encode;
        return this;
    }

    /**
     * Sets URI scheme.
     */
    public URIBuilder setScheme(final String scheme) {
        this.scheme = scheme;
        return this;
    }

    /**
     * Sets URI user info. The value is expected to be unescaped and may contain non ASCII
     * characters.
     */
    public URIBuilder setUserInfo(final String userInfo) {
        this.userInfo = userInfo;
        this.encodedSchemeSpecificPart = null;
        this.encodedAuthority = null;
        this.encodedUserInfo = null;
        return this;
    }

    /**
     * Sets URI user info as a combination of username and password. These values are expected to
     * be unescaped and may contain non ASCII characters.
     */
    public URIBuilder setUserInfo(final String username, final String password) {
        return setUserInfo(username + ':' + password);
    }

    /**
     * Sets URI host.
     */
    public URIBuilder setHost(final String host) {
        this.host = host;
        this.encodedSchemeSpecificPart = null;
        this.encodedAuthority = null;
        return this;
    }

    /**
     * Sets URI port.
     */
    public URIBuilder setPort(final int port) {
        this.port = port < 0 ? -1 : port;
        this.encodedSchemeSpecificPart = null;
        this.encodedAuthority = null;
        return this;
    }

    /**
     * Sets URI path. The value is expected to be unescaped and may contain non ASCII characters.
     */
    public URIBuilder setPath(final String path) {
        this.path = path;
        this.encodedSchemeSpecificPart = null;
        this.encodedPath = null;
        return this;
    }

    public URIBuilder setEncodedPath(final String path) {
        this.encodedPath = path;
        this.encodedSchemeSpecificPart = null;
        return this;
    }

    /**
     * Removes URI query.
     */
    public URIBuilder removeQuery() {
        this.queryParams = null;
        this.query = null;
        this.encodedQuery = null;
        this.encodedSchemeSpecificPart = null;
        return this;
    }

    /**
     * Sets URI query parameters. The parameter name / values are expected to be unescaped
     * and may contain non ASCII characters.
     * <p>
     * Please note query parameters and custom query component are mutually exclusive. This method
     * will remove custom query if present.
     * </p>
     */
    public URIBuilder setParameters(final List<BasicNameValuePair> nvps) {
        this.queryParams = new ArrayList<BasicNameValuePair>();
        this.queryParams.addAll(nvps);
        this.encodedQuery = null;
        this.encodedSchemeSpecificPart = null;
        this.query = null;
        return this;
    }

    public URIBuilder setParameters(final String queryParameters) {
        this.queryParams = new ArrayList<BasicNameValuePair>();
        this.queryParams.addAll(parseQuery(queryParameters));
        this.encodedQuery = null;
        this.encodedSchemeSpecificPart = null;
        this.query = null;
        return this;
    }


    /**
     * Adds URI query parameters. The parameter name / values are expected to be unescaped
     * and may contain non ASCII characters.
     * <p>
     * Please note query parameters and custom query component are mutually exclusive. This method
     * will remove custom query if present.
     * </p>
     */
    public URIBuilder addParameters(final List<BasicNameValuePair> nvps) {
        if (this.queryParams == null || this.queryParams.isEmpty()) {
            this.queryParams = new ArrayList<BasicNameValuePair>();
        }
        this.queryParams.addAll(nvps);
        this.encodedQuery = null;
        this.encodedSchemeSpecificPart = null;
        this.query = null;
        return this;
    }

    /**
     * Sets URI query parameters. The parameter name / values are expected to be unescaped
     * and may contain non ASCII characters.
     * <p>
     * Please note query parameters and custom query component are mutually exclusive. This method
     * will remove custom query if present.
     * </p>
     */
    public URIBuilder setParameters(final BasicNameValuePair... nvps) {
        if (this.queryParams == null) {
            this.queryParams = new ArrayList<BasicNameValuePair>();
        } else {
            this.queryParams.clear();
        }
        for (final BasicNameValuePair nvp : nvps) {
            this.queryParams.add(nvp);
        }
        this.encodedQuery = null;
        this.encodedSchemeSpecificPart = null;
        this.query = null;
        return this;
    }

    /**
     * Adds parameter to URI query. The parameter name and value are expected to be unescaped
     * and may contain non ASCII characters.
     * <p>
     * Please note query parameters and custom query component are mutually exclusive. This method
     * will remove custom query if present.
     * </p>
     */
    public URIBuilder addParameter(final String param, final String value) {
        if (this.queryParams == null) {
            this.queryParams = new ArrayList<BasicNameValuePair>();
        }
        this.queryParams.add(new BasicNameValuePair(param, value));
        this.encodedQuery = null;
        this.encodedSchemeSpecificPart = null;
        this.query = null;
        return this;
    }

    /**
     * Sets parameter of URI query overriding existing value if set. The parameter name and value
     * are expected to be unescaped and may contain non ASCII characters.
     * <p>
     * Please note query parameters and custom query component are mutually exclusive. This method
     * will remove custom query if present.
     * </p>
     */
    public URIBuilder setParameter(final String param, final String value) {
        if (this.queryParams == null) {
            this.queryParams = new ArrayList<BasicNameValuePair>();
        }
        if (!this.queryParams.isEmpty()) {
            for (final Iterator<BasicNameValuePair> it = this.queryParams.iterator(); it.hasNext(); ) {
                final BasicNameValuePair nvp = it.next();
                if (nvp.getName().equals(param)) {
                    it.remove();
                }
            }
        }
        this.queryParams.add(new BasicNameValuePair(param, value));
        this.encodedQuery = null;
        this.encodedSchemeSpecificPart = null;
        this.query = null;
        return this;
    }

    /**
     * Clears URI query parameters.
     */
    public URIBuilder clearParameters() {
        this.queryParams = null;
        this.encodedQuery = null;
        this.encodedSchemeSpecificPart = null;
        return this;
    }

    /**
     * Sets custom URI query. The value is expected to be unescaped and may contain non ASCII
     * characters.
     * <p>
     * Please note query parameters and custom query component are mutually exclusive. This method
     * will remove query parameters if present.
     * </p>
     */
    public URIBuilder setCustomQuery(final String query) {
        this.query = query;
        this.encodedQuery = null;
        this.encodedSchemeSpecificPart = null;
        this.queryParams = null;
        return this;
    }

    /**
     * Sets URI fragment. The value is expected to be unescaped and may contain non ASCII
     * characters.
     */
    public URIBuilder setFragment(final String fragment) {
        this.fragment = fragment;
        this.encodedFragment = null;
        return this;
    }

    public URIBuilder setEncodedFragment(final String fragment) {
        this.fragment = null;
        this.encodedFragment = fragment;
        return this;
    }

    public URIBuilder setEncodedQuery(final String query) {
        this.query = null;
        this.encodedFragment = query;
        return this;
    }

    public boolean isAbsolute() {
        return this.scheme != null;
    }

    public boolean isOpaque() {
        return this.path == null;
    }

    public String getScheme() {
        return this.scheme;
    }

    public String getUserInfo() {
        return this.userInfo;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public String getPath() {
        return this.path;
    }

    public List<BasicNameValuePair> getQueryParams() {
        if (this.queryParams != null) {
            return new ArrayList<BasicNameValuePair>(this.queryParams);
        }
        return new ArrayList<BasicNameValuePair>();

    }

    public String getFragment() {
        return this.fragment;
    }

    @Override
    public String toString() {
        return buildString();
    }

    private static String normalizePath(final String path) {
        String s = path;
        if (s == null) {
            return null;
        }
        int n = 0;
        for (; n < s.length(); n++) {
            if (s.charAt(n) != '/') {
                break;
            }
        }
        if (n > 1) {
            s = s.substring(n - 1);
        }
        return s;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final URIBuilder that = (URIBuilder) o;

        if (port != that.port) return false;
        if (encode != that.encode) return false;
        if (scheme != null ? !scheme.equals(that.scheme) : that.scheme != null) return false;
        if (encodedSchemeSpecificPart != null ? !encodedSchemeSpecificPart.equals(that.encodedSchemeSpecificPart) : that.encodedSchemeSpecificPart != null)
            return false;
        if (encodedAuthority != null ? !encodedAuthority.equals(that.encodedAuthority) : that.encodedAuthority != null)
            return false;
        if (userInfo != null ? !userInfo.equals(that.userInfo) : that.userInfo != null) return false;
        if (encodedUserInfo != null ? !encodedUserInfo.equals(that.encodedUserInfo) : that.encodedUserInfo != null)
            return false;
        if (host != null ? !host.equals(that.host) : that.host != null) return false;
        if (path != null ? !path.equals(that.path) : that.path != null) return false;
        if (encodedPath != null ? !encodedPath.equals(that.encodedPath) : that.encodedPath != null) return false;
        if (encodedQuery != null ? !encodedQuery.equals(that.encodedQuery) : that.encodedQuery != null) return false;
        if (queryParams != null ? !queryParams.equals(that.queryParams) : that.queryParams != null) return false;
        if (query != null ? !query.equals(that.query) : that.query != null) return false;
        if (fragment != null ? !fragment.equals(that.fragment) : that.fragment != null) return false;
        return !(encodedFragment != null ? !encodedFragment.equals(that.encodedFragment) : that.encodedFragment != null);

    }

    @Override
    public int hashCode() {
        int result = scheme != null ? scheme.hashCode() : 0;
        result = 31 * result + (encodedSchemeSpecificPart != null ? encodedSchemeSpecificPart.hashCode() : 0);
        result = 31 * result + (encodedAuthority != null ? encodedAuthority.hashCode() : 0);
        result = 31 * result + (userInfo != null ? userInfo.hashCode() : 0);
        result = 31 * result + (encodedUserInfo != null ? encodedUserInfo.hashCode() : 0);
        result = 31 * result + (host != null ? host.hashCode() : 0);
        result = 31 * result + port;
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (encodedPath != null ? encodedPath.hashCode() : 0);
        result = 31 * result + (encodedQuery != null ? encodedQuery.hashCode() : 0);
        result = 31 * result + (queryParams != null ? queryParams.hashCode() : 0);
        result = 31 * result + (query != null ? query.hashCode() : 0);
        result = 31 * result + (encode ? 1 : 0);
        result = 31 * result + (fragment != null ? fragment.hashCode() : 0);
        result = 31 * result + (encodedFragment != null ? encodedFragment.hashCode() : 0);
        return result;
    }

    public static class BasicNameValuePair implements Cloneable, Serializable {
        private static final long serialVersionUID = -6437800749411518984L;

        private final String name;
        private final String value;

        /**
         * Default Constructor taking a name and a value. The value may be null.
         *
         * @param name  The name.
         * @param value The value.
         */
        public BasicNameValuePair(final String name, final String value) {
            super();
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return this.name;
        }

        public String getValue() {
            return this.value;
        }

        @Override
        public String toString() {
            // don't call complex default formatting for a simple toString

            if (this.value == null) {
                return name;
            }
            final int len = this.name.length() + 1 + this.value.length();
            final StringBuilder buffer = new StringBuilder(len);
            buffer.append(this.name);
            buffer.append("=");
            buffer.append(this.value);
            return buffer.toString();
        }

        @Override
        public boolean equals(final Object object) {
            if (this == object) {
                return true;
            }

            if (object == null) {
                return false;
            }

            if (object instanceof BasicNameValuePair) {
                final BasicNameValuePair that = (BasicNameValuePair) object;
                return this.name.equals(that.name)
                    && this.value.equals(that.value);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return 133 * this.name.hashCode() * this.value.hashCode();
        }

        @Override
        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

    }
}
