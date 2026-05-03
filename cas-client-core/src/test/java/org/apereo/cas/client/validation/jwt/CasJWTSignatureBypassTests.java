package org.apereo.cas.client.validation.jwt;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import org.apereo.cas.client.validation.TicketValidationException;
import org.junit.Test;

import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests demonstrating signature verification bypass in CasJWTProcessor
 * when processing JWE tokens with unsigned payloads.
 *
 * @see CasJWTTicketValidator
 */
public class CasJWTSignatureBypassTests {

    private static final String ENCRYPTION_KEY = "GR7E6uL9djKBSH59BN8boYQ68gQgzwehIIp6s1QicPc";
    private static final String SIGNING_KEY = "vTRQaUu8oDlMrsuhsgNgtk6yie2O6XwRsnDS1POstAQkA1_5TI8-mwrqo1wQ1VahGXLgjCtOb9PLOplmvFzvQA";
    private static final String ISSUER = "https://cas.example.org:8443/cas";
    private static final String AUDIENCE = "https://github.com/apereo/cas";

    private static CasJWTTicketValidator getValidator() {
        var validator = new CasJWTTicketValidator();
        validator.setEncryptionKey(ENCRYPTION_KEY);
        validator.setSigningKey(SIGNING_KEY);
        validator.setExpectedIssuer(ISSUER);
        validator.setExpectedAudience(AUDIENCE);
        validator.setMaxClockSkew(Integer.MAX_VALUE);
        return validator;
    }

    private static JWTClaimsSet forgedClaims() {
        return new JWTClaimsSet.Builder()
                .subject("attacker-forged-admin")
                .issuer(ISSUER)
                .audience(AUDIENCE)
                .jwtID(UUID.randomUUID().toString())
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + 3600000))
                .claim("authenticationMethod", "Forged")
                .claim("role", "ROLE_ADMIN")
                .build();
    }

    private static byte[] getEncryptionKeyBytes() {
        return Base64.getDecoder().decode(ENCRYPTION_KEY);
    }

    /**
     * Vector 1: JWE with cty="JWT" containing raw JSON claims (no nested JWS).
     * <p>
     * toSignedJWT() returns null because the payload is raw JSON, not a signed JWT.
     * The current code checks if payload is valid JSON and falls through to return
     * the unsigned claims. This SHOULD throw BadJWTException.
     */
    @Test
    public void rejectJweWithCtyJwtButNoNestedSignature() throws Exception {
        var keyBytes = getEncryptionKeyBytes();

        var header = new JWEHeader.Builder(JWEAlgorithm.DIR, EncryptionMethod.A128CBC_HS256)
                .contentType("JWT")
                .type(JOSEObjectType.JWT)
                .build();

        var encJwt = new EncryptedJWT(header, forgedClaims());
        encJwt.encrypt(new DirectEncrypter(keyBytes));
        var forgedToken = encJwt.serialize();

        var validator = getValidator();
        try {
            var assertion = validator.validate(forgedToken, "https://example.org");
            fail("JWE with cty=JWT but no nested signed JWT should be rejected. "
                 + "Got subject: " + assertion.getPrincipal().getName());
        } catch (TicketValidationException e) {
            // Expected: signature verification bypass should be blocked
            assertTrue("Should reject unsigned nested payload",
                    e.getMessage().contains("not a nested signed JWT")
                    || e.getCause().getMessage().contains("not a nested signed JWT"));
        }
    }

    /**
     * Vector 1 with attacker-controlled claims: demonstrates full identity forgery.
     * <p>
     * The forged claims include a subject, issuer, and audience that match the
     * validator's expected values. The claims verifier passes because the values
     * are correct — the only missing piece is the cryptographic signature, which
     * is never checked.
     */
    @Test
    public void rejectForgedIdentityInJweWithCtyJwt() throws Exception {
        var keyBytes = getEncryptionKeyBytes();

        var attackerClaims = new JWTClaimsSet.Builder()
                .subject("admin")
                .issuer(ISSUER)
                .audience(AUDIENCE)
                .jwtID(UUID.randomUUID().toString())
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + 3600000))
                .claim("role", "ROLE_ADMIN")
                .claim("clientIpAddress", "10.0.0.1")
                .build();

        var header = new JWEHeader.Builder(JWEAlgorithm.DIR, EncryptionMethod.A128CBC_HS256)
                .contentType("JWT")
                .type(JOSEObjectType.JWT)
                .build();

        var encJwt = new EncryptedJWT(header, attackerClaims);
        encJwt.encrypt(new DirectEncrypter(keyBytes));
        var forgedToken = encJwt.serialize();

        var validator = getValidator();
        try {
            var assertion = validator.validate(forgedToken, "https://example.org");
            fail("Forged identity in unsigned JWE should be rejected. "
                 + "Attacker authenticated as: " + assertion.getPrincipal().getName()
                 + " with role: " + assertion.getPrincipal().getAttributes().get("role"));
        } catch (TicketValidationException e) {
            // Expected: forged claims without signature should be blocked
        }
    }

    /**
     * Sanity check: the existing signed-then-encrypted token from the test suite
     * should continue to validate successfully after the fix.
     */
    @Test
    public void existingSignedAndEncryptedJwtStillValidates() throws Exception {
        var validator = getValidator();
        var jwt = "eyJ6aXAiOiJERUYiLCJhbGciOiJkaXIiLCJlbmMiOiJBMTI4Q0JDLUhTMjU2IiwiY3R5IjoiSldUIiwidHlwIjoiSldUIiwia2lkIjoiNGQ4NjExN2YtZWMyZC00MTY1LThjNDYtY2QyYWI0MDdhYTA5Iiwib3JnLmFwZXJlby5jYXMuc2VydmljZXMuUmVnaXN0ZXJlZFNlcnZpY2UiOiIxIn0..SX4YsSHImUrnFzo5_F_lNw.viFHp1nFcP-LNZlx_ngVEg3H6TIZRRezO88cGe8iVjTG549L5ROkUCu7nCpuc8wiK6KmUQVIjzRLhlWZ3G0kkf0-zMiPT9UQxPlLRrtm0XM2_Okj3DUcK5tRi7TEEn67leDOx6sIKi3I2zA_80Ac84DPSsnuTd-EZwnOE8p3yxN3GVxIq-qzKgaTsl-eaER7fxePkOKION98OxsKiySWriu5UchOA25qpVr4eRq-JJCjPt2pC_DvFQVk_aPBAfsUpQttYvrzvOFN25ylLobQUHs9fGylEt8uAIr0l-Ai4rRyh46RiFEW74iyhUJpa5aPQkMACvRobjcAHVzuGduKMciF-65Ooa7MeDQM3H31hlq3VCu58Jv0AZbQRNz-Fwv7ICeUFQOzMZPzAq0sNi0akYqal-a5Q-mrlWwTABnb7amIP_1i5yXxdRiLlzSeMW3CrfmvKeIlH_ttr3ra3B6Hms23Zsw7qrmJSCFKyuwyGTiAYBJNWH5SjixBb2pLodg9eiQKkrSNHRAB-UE5cfSmm2hfl5yfLh8pLZe2BSr5Ul32UfoP2X3bW8GH_hQ3rbG0E-K5P2qRtDOC6p8yNd-3MwCD1tPKm27E1vAtGsiHlrfu_l2_i2RtzTSo24sF1EcKwfJDpNi9apReZQlaZOZ4vmmS1e7MZPfrQ83qvNGPjHx8-H9dbOWxLEfX0IuoeHwfc095o6gv3PA6rCHv5mlDRLXll31CeJPY8Xd0Xe9l8IzJZ_bF1idz2m-elr9-RXDZgWXgMNj69Vis0TbHUapEksgtLgxcjjA664goGJb87YF4fli6H5JmPSF_gbzW4f1KjVrXtEFHpHamdB3-3_HrW64oTwTLU1irE-5hp5lumk3o9Ixdsn4-Eqo_cXPu2ps8.WcV_CeloEdJ7O4cWDzBXAw";
        var assertion = validator.validate(jwt, "https://example.org");
        assertNotNull("Existing signed+encrypted JWT should still validate", assertion);
        assertEquals("casuser", assertion.getPrincipal().getName());
    }
}
