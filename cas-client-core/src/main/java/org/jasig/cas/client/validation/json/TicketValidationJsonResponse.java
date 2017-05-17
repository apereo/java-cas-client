package org.jasig.cas.client.validation.json;

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


