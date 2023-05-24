package org.apereo.cas.client.validation.jwt;

import org.junit.Test;

import static org.junit.Assert.*;

public class CasJWTTicketValidatorTests {

    @Test
    public void verifyAesKeyWithSignedAndEncryptedJWT() throws Exception {
        var validator = new CasJWTTicketValidator();
        validator.setEncryptionKey("GR7E6uL9djKBSH59BN8boYQ68gQgzwehIIp6s1QicPc");
        validator.setSigningKey("vTRQaUu8oDlMrsuhsgNgtk6yie2O6XwRsnDS1POstAQkA1_5TI8-mwrqo1wQ1VahGXLgjCtOb9PLOplmvFzvQA");
        validator.setExpectedIssuer("https://cas.example.org:8443/cas");
        validator.setExpectedAudience("https://github.com/apereo/cas");
        validator.setMaxClockSkew(Integer.MAX_VALUE);
        
        var jwt =
            "eyJ6aXAiOiJERUYiLCJhbGciOiJkaXIiLCJlbmMiOiJBMTI4Q0JDLUhTMjU2IiwiY3R5IjoiSldUIiwidHlwIjoiSldUIiwia2lkIjoiNGQ4NjExN2YtZWMyZC00MTY1LThjNDYtY2QyYWI0MDdhYTA5Iiwib3JnLmFwZXJlby5jYXMuc2VydmljZXMuUmVnaXN0ZXJlZFNlcnZpY2UiOiIxIn0..SX4YsSHImUrnFzo5_F_lNw.viFHp1nFcP-LNZlx_ngVEg3H6TIZRRezO88cGe8iVjTG549L5ROkUCu7nCpuc8wiK6KmUQVIjzRLhlWZ3G0kkf0-zMiPT9UQxPlLRrtm0XM2_Okj3DUcK5tRi7TEEn67leDOx6sIKi3I2zA_80Ac84DPSsnuTd-EZwnOE8p3yxN3GVxIq-qzKgaTsl-eaER7fxePkOKION98OxsKiySWriu5UchOA25qpVr4eRq-JJCjPt2pC_DvFQVk_aPBAfsUpQttYvrzvOFN25ylLobQUHs9fGylEt8uAIr0l-Ai4rRyh46RiFEW74iyhUJpa5aPQkMACvRobjcAHVzuGduKMciF-65Ooa7MeDQM3H31hlq3VCu58Jv0AZbQRNz-Fwv7ICeUFQOzMZPzAq0sNi0akYqal-a5Q-mrlWwTABnb7amIP_1i5yXxdRiLlzSeMW3CrfmvKeIlH_ttr3ra3B6Hms23Zsw7qrmJSCFKyuwyGTiAYBJNWH5SjixBb2pLodg9eiQKkrSNHRAB-UE5cfSmm2hfl5yfLh8pLZe2BSr5Ul32UfoP2X3bW8GH_hQ3rbG0E-K5P2qRtDOC6p8yNd-3MwCD1tPKm27E1vAtGsiHlrfu_l2_i2RtzTSo24sF1EcKwfJDpNi9apReZQlaZOZ4vmmS1e7MZPfrQ83qvNGPjHx8-H9dbOWxLEfX0IuoeHwfc095o6gv3PA6rCHv5mlDRLXll31CeJPY8Xd0Xe9l8IzJZ_bF1idz2m-elr9-RXDZgWXgMNj69Vis0TbHUapEksgtLgxcjjA664goGJb87YF4fli6H5JmPSF_gbzW4f1KjVrXtEFHpHamdB3-3_HrW64oTwTLU1irE-5hp5lumk3o9Ixdsn4-Eqo_cXPu2ps8.WcV_CeloEdJ7O4cWDzBXAw";
        var assertion = validator.validate(jwt, "https://example.org");
        assertEquals("casuser", assertion.getPrincipal().getName());
        assertEquals("casuser", assertion.getPrincipal().getAttributes().get("sub"));
        assertEquals("Static Credentials", assertion.getPrincipal().getAttributes().get("authenticationMethod"));
        assertEquals("0:0:0:0:0:0:0:1", assertion.getPrincipal().getAttributes().get("clientIpAddress"));
    }
}
