package org.jasig.cas.client.validation;

import org.jasig.cas.client.util.XmlUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Extension to the traditional Service Ticket validation that will validate service tickets and proxy tickets.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class Cas20ProxyTicketValidator extends Cas20ServiceTicketValidator {

    private boolean acceptAnyProxy;

    /** This should be a list of an array of Strings */
    private List allowedProxyChains = new ArrayList();

    public Cas20ProxyTicketValidator(final String casServerUrlPrefix) {
        super(casServerUrlPrefix);
    }

    protected String getUrlSuffix() {
        return "proxyValidate";
    }

    protected void customParseResponse(final String response, final Assertion assertion) throws TicketValidationException {
        final List proxies = XmlUtils.getTextForElements(response, "proxy");
        final String[] proxiedList = (String[]) proxies.toArray(new String[proxies.size()]);

        // this means there was nothing in the proxy chain, which is okay
        if (proxies == null || proxies.isEmpty() || this.acceptAnyProxy) {
            return;
        }

        for (Iterator iter = this.allowedProxyChains.iterator(); iter.hasNext();) {
            if (Arrays.equals(proxiedList, (Object[]) iter.next())) {
                return;
            }
        }

        throw new InvalidProxyChainTicketValidationException("Invalid proxy chain: " + proxies.toString());
    }

    public void setAcceptAnyProxy(final boolean acceptAnyProxy) {
        this.acceptAnyProxy = acceptAnyProxy;
    }

    public void setAllowedProxyChains(final List allowedProxyChains) {
        this.allowedProxyChains = allowedProxyChains;
    }
}
