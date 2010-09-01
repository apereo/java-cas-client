package org.jasig.cas.client.tomcat;

/**
 *
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1.12
 */
public abstract class AbstractCasAuthenticator extends AbstractAuthenticator {

    private String encoding;

    private String proxyCallbackUrl;

    private boolean renew;

    protected final String getEncoding() {
        return this.encoding;
    }

    public final void setEncoding(final String encoding) {
        this.encoding = encoding;
    }

    protected final String getProxyCallbackUrl() {
        return this.proxyCallbackUrl;
    }

    public final void setProxyCallbackUrl(final String proxyCallbackUrl) {
        this.proxyCallbackUrl = proxyCallbackUrl;
    }

    protected final boolean isRenew() {
        return this.renew;
    }

    public void setRenew(final boolean renew) {
        this.renew = renew;
    }

    protected final String getArtifactParameterName() {
        return "ticket";
    }

    protected final String getServiceParameterName() {
        return "Service";
    }
}
