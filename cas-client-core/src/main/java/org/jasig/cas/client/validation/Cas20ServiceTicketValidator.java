package org.jasig.cas.client.validation;

import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.jasig.cas.client.proxy.Cas20ProxyRetriever;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorage;
import org.jasig.cas.client.proxy.ProxyRetriever;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.util.XmlUtils;

import java.util.Map;

/**
 * Implementation of the TicketValidator that will validate Service Tickets in compliance with the CAS 2.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public class Cas20ServiceTicketValidator extends AbstractCasProtocolUrlBasedTicketValidator {

    /** The CAS 2.0 protocol proxy callback url. */
    private String proxyCallbackUrl;

    /** The storage location of the proxy granting tickets. */
    private ProxyGrantingTicketStorage proxyGrantingTicketStorage;

    /** Implementation of the proxy retriever. */
    private ProxyRetriever proxyRetriever;

    /**
     * Constructs an instance of the CAS 2.0 Service Ticket Validator with the supplied
     * CAS server url prefix.
     *
     * @param casServerUrlPrefix the CAS Server URL prefix.
     */
    public Cas20ServiceTicketValidator(final String casServerUrlPrefix) {
        super(casServerUrlPrefix);
        this.proxyRetriever = new Cas20ProxyRetriever(casServerUrlPrefix);
    }

    /**
     * Adds the pgtUrl to the list of parameters to pass to the CAS server.
     *
     * @param urlParameters the Map containing the existing parameters to send to the server.
     */
    protected final void populateUrlAttributeMap(final Map urlParameters) {
        urlParameters.put("pgtUrl", encodeUrl(this.proxyCallbackUrl));
    }

    protected String getUrlSuffix() {
        return "serviceValidate";
    }

    protected final Assertion parseResponseFromServer(final String response) throws TicketValidationException {
        final String error = XmlUtils.getTextForElement(response,
                "authenticationFailure");

        if (CommonUtils.isNotBlank(error)) {
            throw new TicketValidationException(error);
        }

        final String principal = XmlUtils.getTextForElement(response, "user");
        final String proxyGrantingTicketIou = XmlUtils.getTextForElement(
                response, "proxyGrantingTicket");
        final String proxyGrantingTicket = this.proxyGrantingTicketStorage.retrieve(proxyGrantingTicketIou);

        if (CommonUtils.isEmpty(principal)) {
            throw new TicketValidationException("No principal was found in the response from the CAS server.");
        }

        final Assertion assertion;
        if (CommonUtils.isNotBlank(proxyGrantingTicket)) {
            final AttributePrincipal attributePrincipal = new AttributePrincipalImpl(principal, proxyGrantingTicket, this.proxyRetriever);
            assertion = new AssertionImpl(attributePrincipal);
        } else {
            assertion = new AssertionImpl(principal);
        }

        customParseResponse(response, assertion);

        return assertion;
    }

    /**
     * Template method if additional custom parsing (such as Proxying) needs to be done.
     *
     * @param response the original response from the CAS server.
     * @param assertion the partially constructed assertion.
     * @throws TicketValidationException if there is a problem constructing the Assertion.
     */
    protected void customParseResponse(final String response, final Assertion assertion) throws TicketValidationException {
        // nothing to do
    }

    public final void setProxyCallbackUrl(final String proxyCallbackUrl) {
        this.proxyCallbackUrl = proxyCallbackUrl;
    }

    public final void setProxyGrantingTicketStorage(final ProxyGrantingTicketStorage proxyGrantingTicketStorage) {
        this.proxyGrantingTicketStorage = proxyGrantingTicketStorage;
    }

    public final void setProxyRetriever(final ProxyRetriever proxyRetriever) {
        this.proxyRetriever = proxyRetriever;
    }    
}
