package org.jasig.cas.client.validation;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.client.util.CommonUtils;

import java.net.URLEncoder;

/**
 * Abstract class for validating tickets that defines a workflow that all ticket
 * validation should follow.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public abstract class AbstractUrlBasedTicketValidator implements
        TicketValidator {

    /**
     * Instance of Commons Logging.
     */
    protected final Log log = LogFactory.getLog(this.getClass());

    /**
     * Url to CAS server.
     */
    private final String casServerUrl;

    /**
     * Whether this client is looking for an authentication from renew.
     */
    private final boolean renew;

    /**
     * Instance of HttpClient for connecting to server.
     */
    private final HttpClient httpClient;

    public final Assertion validate(final String ticketId, final Service service)
            throws ValidationException {
        final String url = constructURL(ticketId, service);
        final String response = getResponseFromURL(url);

        return parseResponse(response);
    }

    protected abstract String constructURL(final String ticketId,
                                           final Service service);

    protected abstract Assertion parseResponse(final String response)
            throws ValidationException;

    private String getResponseFromURL(final String url)
            throws ValidationException {
        final GetMethod method = new GetMethod(url);

        try {
            this.httpClient.executeMethod(method);
            return method.getResponseBodyAsString();
        } catch (Exception e) {
            log.error(e, e);
            throw new ValidationException(
                    "Unable to retrieve response from CAS Server.", e);
        } finally {
            method.releaseConnection();
        }
    }

    protected AbstractUrlBasedTicketValidator(final String casServerUrl, final boolean renew, final HttpClient httpClient) {
        CommonUtils.assertNotNull(casServerUrl,
                "the validationUrl cannot be null");
        CommonUtils
                .assertNotNull(httpClient, "httpClient cannot be null.");
        this.casServerUrl = casServerUrl;
        this.renew = renew;
        this.httpClient = httpClient;
    }

    /**
     * Helper method to encode the service url.
     *
     * @param service the service url to encode.
     * @return the encoded service url.
     */
    protected final String getEncodedService(final Service service) {
        try {
            return URLEncoder.encode(service.getId(), "UTF-8");
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected final String getCasServerUrl() {
        return this.casServerUrl;
    }

    protected final boolean isRenew() {
        return this.renew;
    }
}
