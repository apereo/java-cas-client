package org.jasig.cas.client.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLConnection;
import java.security.KeyStore;
import java.util.Properties;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of the {@link URLConnectionFactory} whose responsible to configure
 * the underlying <i>https</i> connection, if needed, with a given hostname and SSL socket factory based on the
 * configuration provided. 
 * 
 * @author Misagh Moayyed
 * @since 3.3
 * @see #setHostnameVerifier(HostnameVerifier)
 * @see #setSSLConfiguration(Properties)
 */
public final class HttpsURLConnectionFactory implements URLConnectionFactory {

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
    
    public HttpsURLConnectionFactory() {}
    
    public HttpsURLConnectionFactory(final HostnameVerifier verifier, final Properties config) {
        setHostnameVerifier(verifier);
        setSSLConfiguration(config);
    }
        
    public final void setSSLConfiguration(final Properties config) {
        this.sslConfiguration = config;
    }
    
    public final void setHostnameVerifier(final HostnameVerifier verifier) {
        this.hostnameVerifier = verifier;
    }

    public URLConnection getURLConnection(final URLConnection url) {
        return this.configureHttpsConnectionIfNeeded(url);
    }
    
    /**
     * Configures the connection with specific settings for secure http connections
     * If the connection instance is not a {@link HttpsURLConnection},
     * no additional changes will be made and the connection itself is simply returned.
     *
     * @param conn the http connection
     */
    private URLConnection configureHttpsConnectionIfNeeded(final URLConnection conn) {
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
        return conn;
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
     * @param sslConfig {@link Properties} 
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
                        final KeyManagerFactory keyManager = KeyManagerFactory.getInstance(this.sslConfiguration.getProperty("keyManagerType", "SunX509"));
                        keyManager.init(keyStore, this.sslConfiguration.getProperty("certificatePassword").toCharArray());
                        sslContext.init(keyManager.getKeyManagers(), null, null);
                    }
                }
            }

            return sslContext.getSocketFactory();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            CommonUtils.closeQuietly(keyStoreIS);
        }
        return null;
    }

}
