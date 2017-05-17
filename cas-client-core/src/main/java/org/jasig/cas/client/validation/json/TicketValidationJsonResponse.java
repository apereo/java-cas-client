package org.jasig.cas.client.validation.json;

import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorage;
import org.jasig.cas.client.proxy.ProxyRetriever;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.AssertionImpl;

import java.util.List;
import java.util.Map;

/**
 * This is {@link TicketValidationJsonResponse}.
 *
 * @author Misagh Moayyed
 */
final class TicketValidationJsonResponse {
    private CasServiceResponseAuthenticationFailure authenticationFailure;
    private CasServiceResponseAuthenticationSuccess authenticationSuccess;

    public CasServiceResponseAuthenticationFailure getAuthenticationFailure() {
        return this.authenticationFailure;
    }

    public void setAuthenticationFailure(final CasServiceResponseAuthenticationFailure authenticationFailure) {
        this.authenticationFailure = authenticationFailure;
    }

    public CasServiceResponseAuthenticationSuccess getAuthenticationSuccess() {
        return this.authenticationSuccess;
    }

    public void setAuthenticationSuccess(final CasServiceResponseAuthenticationSuccess authenticationSuccess) {
        this.authenticationSuccess = authenticationSuccess;
    }

    Assertion getAssertion(final ProxyGrantingTicketStorage proxyGrantingTicketStorage,
                           final ProxyRetriever proxyRetriever) {
        final String proxyGrantingTicketIou = getAuthenticationSuccess().getProxyGrantingTicket();
        final String proxyGrantingTicket;
        if (CommonUtils.isBlank(proxyGrantingTicketIou) || proxyGrantingTicketStorage == null) {
            proxyGrantingTicket = null;
        } else {
            proxyGrantingTicket = proxyGrantingTicketStorage.retrieve(proxyGrantingTicketIou);
        }

        final Assertion assertion;
        final Map<String, Object> attributes = getAuthenticationSuccess().getAttributes();
        final String principal = getAuthenticationSuccess().getUser();
        if (CommonUtils.isNotBlank(proxyGrantingTicket)) {
            final AttributePrincipal attributePrincipal = new AttributePrincipalImpl(principal, attributes,
                    proxyGrantingTicket, proxyRetriever);
            assertion = new AssertionImpl(attributePrincipal);
        } else {
            assertion = new AssertionImpl(new AttributePrincipalImpl(principal, attributes));
        }
        return assertion;
    }


    static class CasServiceResponseAuthenticationSuccess {
        private String user;
        private String proxyGrantingTicket;
        private List proxies;
        private Map attributes;

        public String getUser() {
            return this.user;
        }

        public void setUser(final String user) {
            this.user = user;
        }

        public String getProxyGrantingTicket() {
            return this.proxyGrantingTicket;
        }

        public void setProxyGrantingTicket(final String proxyGrantingTicket) {
            this.proxyGrantingTicket = proxyGrantingTicket;
        }

        public List getProxies() {
            return this.proxies;
        }

        public void setProxies(final List proxies) {
            this.proxies = proxies;
        }

        public Map getAttributes() {
            return this.attributes;
        }

        public void setAttributes(final Map attributes) {
            this.attributes = attributes;
        }
    }

    static class CasServiceResponseAuthenticationFailure {
        private String code;
        private String description;

        public String getCode() {
            return this.code;
        }

        public void setCode(final String code) {
            this.code = code;
        }

        public String getDescription() {
            return this.description;
        }

        public void setDescription(final String description) {
            this.description = description;
        }
    }
}


