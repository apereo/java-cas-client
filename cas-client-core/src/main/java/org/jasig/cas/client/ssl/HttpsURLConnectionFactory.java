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
package org.jasig.cas.client.ssl;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.security.KeyStore;
import java.util.Properties;
import javax.net.ssl.*;
import org.jasig.cas.client.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of the {@link HttpURLConnectionFactory} whose responsible to configure
 * the underlying <i>https</i> connection, if needed, with a given hostname and SSL socket factory based on the
 * configuration provided. 
 * 
 * @author Misagh Moayyed
 * @since 3.3
 * @see #setHostnameVerifier(HostnameVerifier)
 * @see #setSSLConfiguration(Properties)
 */
public final class HttpsURLConnectionFactory implements HttpURLConnectionFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpsURLConnectionFactory.class);

    /**
     * Hostname verifier used when making an SSL request to the CAS server.
     * Defaults to {@link HttpsURLConnection#getDefaultHostnameVerifier()}
     */
    private HostnameVerifier hostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();

    /**
     * Properties file that can contains key/trust info for Client Side Certificates
     */
    private Properties sslConfiguration = new Properties();

    public HttpsURLConnectionFactory() {
    }

    public HttpsURLConnectionFactory(final HostnameVerifier verifier, final Properties config) {
        setHostnameVerifier(verifier);
        setSSLConfiguration(config);
    }

    public final void setSSLConfiguration(final Properties config) {
        this.sslConfiguration = config;
    }

    /**
     * Set the host name verifier for the https connection received.
     * 
     * @see AnyHostnameVerifier
     * @see RegexHostnameVerifier
     * @see WhitelistHostnameVerifier
     */
    public final void setHostnameVerifier(final HostnameVerifier verifier) {
        this.hostnameVerifier = verifier;
    }

    public HttpURLConnection buildHttpURLConnection(final URLConnection url) {
        return this.configureHttpsConnectionIfNeeded(url);
    }

    /**
     * Configures the connection with specific settings for secure http connections
     * If the connection instance is not a {@link HttpsURLConnection},
     * no additional changes will be made and the connection itself is simply returned.
     *
     * @param conn the http connection
     */
    private HttpURLConnection configureHttpsConnectionIfNeeded(final URLConnection conn) {
        if (conn instanceof HttpsURLConnection) {
            final HttpsURLConnection httpsConnection = (HttpsURLConnection) conn;
            final SSLSocketFactory socketFactory = this.createSSLSocketFactory();
            if (socketFactory != null) {
                httpsConnection.setSSLSocketFactory(socketFactory);
            }

            if (this.hostnameVerifier != null) {
                httpsConnection.setHostnameVerifier(this.hostnameVerifier);
            }
        }
        return (HttpURLConnection) conn;
    }

    /**
     * Creates a {@link SSLSocketFactory} based on the configuration specified
     * <p>
     * Sample properties file:
     * <pre>
     * protocol=TLS
     * keyStoreType=JKS
     * keyStorePath=/var/secure/location/.keystore
     * keyStorePass=changeit
     * certificatePassword=aGoodPass
     * </pre>
     * @return the {@link SSLSocketFactory}
     */
    private SSLSocketFactory createSSLSocketFactory() {
        InputStream keyStoreIS = null;
        try {
            final SSLContext sslContext = SSLContext.getInstance(this.sslConfiguration.getProperty("protocol", "SSL"));

            if (this.sslConfiguration.getProperty("keyStoreType") != null) {
                final KeyStore keyStore = KeyStore.getInstance(this.sslConfiguration.getProperty("keyStoreType"));
                if (this.sslConfiguration.getProperty("keyStorePath") != null) {
                    keyStoreIS = new FileInputStream(this.sslConfiguration.getProperty("keyStorePath"));
                    if (this.sslConfiguration.getProperty("keyStorePass") != null) {
                        keyStore.load(keyStoreIS, this.sslConfiguration.getProperty("keyStorePass").toCharArray());
                        LOGGER.debug("Keystore has {} keys", keyStore.size());
                        final KeyManagerFactory keyManager = KeyManagerFactory.getInstance(this.sslConfiguration
                                .getProperty("keyManagerType", "SunX509"));
                        keyManager.init(keyStore, this.sslConfiguration.getProperty("certificatePassword")
                                .toCharArray());
                        sslContext.init(keyManager.getKeyManagers(), null, null);
                        return sslContext.getSocketFactory();
                    }
                }
            }

        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            CommonUtils.closeQuietly(keyStoreIS);
        }
        return null;
    }

}
