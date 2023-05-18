/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apereo.cas.client.validation.jwt;

import org.apereo.cas.client.authentication.AttributePrincipalImpl;
import org.apereo.cas.client.validation.Assertion;
import org.apereo.cas.client.validation.AssertionImpl;
import org.apereo.cas.client.validation.TicketValidationException;
import org.apereo.cas.client.validation.TicketValidator;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier;
import com.nimbusds.jose.proc.JWEDecryptionKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * This is {@link CasJWTTicketValidator} that attempts to parse the CAS service ticket
 * as a JWT.
 *
 * @author Misagh Moayyed
 */
public class CasJWTTicketValidator implements TicketValidator {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private String signingKey;

    private String encryptionKey;

    private String expectedIssuer;

    private String expectedAudience;

    private String encryptionKeyAlgorithm = "AES";

    private String signingKeyAlgorithm = "AES";

    private String requiredClaims = "sub,aud,iat,jti,exp,iss";

    private boolean base64EncryptionKey = true;

    private boolean base64SigningKey;

    private ConfigurableJWTProcessor<SecurityContext> jwtProcessor;

    @Override
    public Assertion validate(final String ticket, final String service) throws TicketValidationException {
        try {
            if (jwtProcessor == null) {
                initialize();
            }

            final var claimsSet = this.jwtProcessor.process(ticket, null);
            logger.debug("Validated claims are {}", claimsSet);

            return new AssertionImpl(
                new AttributePrincipalImpl(claimsSet.getSubject(), claimsSet.getClaims()),
                claimsSet.getIssueTime(), claimsSet.getExpirationTime(),
                claimsSet.getIssueTime(), new HashMap<>());
        } catch (final Exception e) {
            throw new TicketValidationException(e);
        }
    }

    public void initialize() {
        logger.debug("Initializing JWT processor...");
        this.jwtProcessor = new DefaultJWTProcessor<>();
        jwtProcessor.setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>(JOSEObjectType.JWT));

        final var jweKeySource = new ImmutableSecret<>(new SecretKeySpec(
            base64EncryptionKey ? Base64.getDecoder().decode(encryptionKey) : encryptionKey.getBytes(StandardCharsets.UTF_8), encryptionKeyAlgorithm));
        final var jwsKeySource = new ImmutableSecret<>(new SecretKeySpec(
            base64SigningKey ? Base64.getDecoder().decode(signingKey) : signingKey.getBytes(StandardCharsets.UTF_8), signingKeyAlgorithm));

        configureKeySelectors(jwtProcessor, jweKeySource, jwsKeySource);

        final var requiredClaimsSet = Set.of(requiredClaims.split(","));
        final var exactMatchClaims = new JWTClaimsSet.Builder()
            .issuer(expectedIssuer)
            .audience(expectedAudience)
            .build();
        final var jwtClaimsSetVerifier = new DefaultJWTClaimsVerifier<>(exactMatchClaims, requiredClaimsSet);
        jwtProcessor.setJWTClaimsSetVerifier(jwtClaimsSetVerifier);
    }

    private static void configureKeySelectors(final ConfigurableJWTProcessor<SecurityContext> jwtProcessor,
                                              final ImmutableSecret<SecurityContext> jweKeySource,
                                              final ImmutableSecret<SecurityContext> jwsKeySource) {
        final var jwsKeySelector = new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, jwsKeySource) {
            @Override
            public List<Key> selectJWSKeys(final JWSHeader jwsHeader, final SecurityContext context) {
                return List.of(jwsKeySource.getSecretKey());
            }
        };
        final var jweKeySelector = new JWEDecryptionKeySelector<>(JWEAlgorithm.DIR, EncryptionMethod.A128CBC_HS256, jweKeySource) {
            @Override
            public List<Key> selectJWEKeys(final JWEHeader jweHeader, final SecurityContext context) {
                return List.of(jweKeySource.getSecretKey());
            }
        };
        jwtProcessor.setJWSKeySelector(jwsKeySelector);
        jwtProcessor.setJWEKeySelector(jweKeySelector);
    }

    public void setBase64EncryptionKey(final boolean base64EncryptionKey) {
        this.base64EncryptionKey = base64EncryptionKey;
    }

    public void setBase64SigningKey(final boolean base64SigningKey) {
        this.base64SigningKey = base64SigningKey;
    }

    public void setRequiredClaims(final String requiredClaims) {
        this.requiredClaims = requiredClaims;
    }

    public void setEncryptionKeyAlgorithm(final String encryptionKeyAlgorithm) {
        this.encryptionKeyAlgorithm = encryptionKeyAlgorithm;
    }

    public void setSigningKeyAlgorithm(final String signingKeyAlgorithm) {
        this.signingKeyAlgorithm = signingKeyAlgorithm;
    }

    public void setExpectedAudience(final String expectedAudience) {
        this.expectedAudience = expectedAudience;
    }

    public void setExpectedIssuer(final String expectedIssuer) {
        this.expectedIssuer = expectedIssuer;
    }

    public void setSigningKey(final String signingKey) {
        this.signingKey = signingKey;
    }

    public void setEncryptionKey(final String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }
}
