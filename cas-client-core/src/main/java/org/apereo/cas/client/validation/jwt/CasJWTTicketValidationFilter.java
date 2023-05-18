package org.apereo.cas.client.validation.jwt;

import org.apereo.cas.client.Protocol;
import org.apereo.cas.client.configuration.ConfigurationKey;
import org.apereo.cas.client.validation.AbstractTicketValidationFilter;
import org.apereo.cas.client.validation.TicketValidator;

import jakarta.servlet.FilterConfig;

public class CasJWTTicketValidationFilter extends AbstractTicketValidationFilter {
    protected CasJWTTicketValidationFilter() {
        super(Protocol.CAS3);
    }

    @Override
    protected TicketValidator getTicketValidator(final FilterConfig filterConfig) {
        final var validator = new CasJWTTicketValidator();
        validator.setEncryptionKey(getString(new ConfigurationKey<>("encryptionKey")));
        validator.setSigningKey(getString(new ConfigurationKey<>("signingKey")));
        validator.setExpectedIssuer(getString(new ConfigurationKey<>("expectedIssuer")));
        validator.setExpectedAudience(getString(new ConfigurationKey<>("expectedAudience")));
        validator.setEncryptionKeyAlgorithm(getString(new ConfigurationKey<>("encryptionKeyAlgorithm", "AES")));
        validator.setSigningKeyAlgorithm(getString(new ConfigurationKey<>("signingKeyAlgorithm", "AES")));
        validator.setRequiredClaims(getString(new ConfigurationKey<>("requiredClaims", "sub,aud,iat,jti,exp,iss")));
        validator.setBase64EncryptionKey(getBoolean(new ConfigurationKey<>("base64EncryptionKey", true)));
        validator.setBase64SigningKey(getBoolean(new ConfigurationKey<>("base64SigningKey", true)));
        validator.initialize();
        return validator;
    }
}
