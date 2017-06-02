package org.jasig.cas.client.validation.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    private final CasServiceResponseAuthentication serviceResponse;

    @JsonCreator
    public TicketValidationJsonResponse(@JsonProperty("serviceResponse")
                                        final CasServiceResponseAuthentication serviceResponse) {
        this.serviceResponse = serviceResponse;
    }

    public CasServiceResponseAuthentication getServiceResponse() {
        return serviceResponse;
    }

    Assertion getAssertion(final ProxyGrantingTicketStorage proxyGrantingTicketStorage,
                           final ProxyRetriever proxyRetriever) {
        final String proxyGrantingTicketIou = getServiceResponse().getAuthenticationSuccess().getProxyGrantingTicket();
        final String proxyGrantingTicket;
        if (CommonUtils.isBlank(proxyGrantingTicketIou) || proxyGrantingTicketStorage == null) {
            proxyGrantingTicket = null;
        } else {
            proxyGrantingTicket = proxyGrantingTicketStorage.retrieve(proxyGrantingTicketIou);
        }

        final Assertion assertion;
        final Map<String, Object> attributes = getServiceResponse().getAuthenticationSuccess().getAttributes();
        final String principal = getServiceResponse().getAuthenticationSuccess().getUser();
        if (CommonUtils.isNotBlank(proxyGrantingTicket)) {
            final AttributePrincipal attributePrincipal = new AttributePrincipalImpl(principal, attributes,
                    proxyGrantingTicket, proxyRetriever);
            assertion = new AssertionImpl(attributePrincipal);
        } else {
            assertion = new AssertionImpl(new AttributePrincipalImpl(principal, attributes));
        }
        return assertion;
    }

    static class CasServiceResponseAuthentication {
        private final CasServiceResponseAuthenticationFailure authenticationFailure;
        private final CasServiceResponseAuthenticationSuccess authenticationSuccess;

        @JsonCreator
        public CasServiceResponseAuthentication(@JsonProperty("authenticationFailure")
                                                final CasServiceResponseAuthenticationFailure authenticationFailure,
                                                @JsonProperty("authenticationSuccess")
                                                final CasServiceResponseAuthenticationSuccess authenticationSuccess) {
            this.authenticationFailure = authenticationFailure;
            this.authenticationSuccess = authenticationSuccess;
        }

        public CasServiceResponseAuthenticationFailure getAuthenticationFailure() {
            return this.authenticationFailure;
        }

        public CasServiceResponseAuthenticationSuccess getAuthenticationSuccess() {
            return this.authenticationSuccess;
        }
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


