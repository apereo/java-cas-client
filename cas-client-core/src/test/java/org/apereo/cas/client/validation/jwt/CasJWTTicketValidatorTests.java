package org.apereo.cas.client.validation.jwt;

import org.junit.Test;

import static org.junit.Assert.*;

public class CasJWTTicketValidatorTests {

    private static CasJWTTicketValidator getValidator(final String url) {
        var validator = new CasJWTTicketValidator();
        validator.setEncryptionKey("GR7E6uL9djKBSH59BN8boYQ68gQgzwehIIp6s1QicPc");
        validator.setSigningKey("vTRQaUu8oDlMrsuhsgNgtk6yie2O6XwRsnDS1POstAQkA1_5TI8-mwrqo1wQ1VahGXLgjCtOb9PLOplmvFzvQA");
        validator.setExpectedIssuer("https://cas.example.org:8443/cas");
        validator.setExpectedAudience(url);
        validator.setMaxClockSkew(Integer.MAX_VALUE);
        return validator;
    }

    @Test
    public void verifyAesKeyWithSignedAndEncryptedJWT() throws Exception {
        var validator = getValidator("https://github.com/apereo/cas");
        var jwt = "eyJ6aXAiOiJERUYiLCJhbGciOiJkaXIiLCJlbmMiOiJBMTI4Q0JDLUhTMjU2IiwiY3R5IjoiSldUIiwidHlwIjoiSldUIiwia2lkIjoiNGQ4NjExN2YtZWMyZC00MTY1LThjNDYtY2QyYWI0MDdhYTA5Iiwib3JnLmFwZXJlby5jYXMuc2VydmljZXMuUmVnaXN0ZXJlZFNlcnZpY2UiOiIxIn0..SX4YsSHImUrnFzo5_F_lNw.viFHp1nFcP-LNZlx_ngVEg3H6TIZRRezO88cGe8iVjTG549L5ROkUCu7nCpuc8wiK6KmUQVIjzRLhlWZ3G0kkf0-zMiPT9UQxPlLRrtm0XM2_Okj3DUcK5tRi7TEEn67leDOx6sIKi3I2zA_80Ac84DPSsnuTd-EZwnOE8p3yxN3GVxIq-qzKgaTsl-eaER7fxePkOKION98OxsKiySWriu5UchOA25qpVr4eRq-JJCjPt2pC_DvFQVk_aPBAfsUpQttYvrzvOFN25ylLobQUHs9fGylEt8uAIr0l-Ai4rRyh46RiFEW74iyhUJpa5aPQkMACvRobjcAHVzuGduKMciF-65Ooa7MeDQM3H31hlq3VCu58Jv0AZbQRNz-Fwv7ICeUFQOzMZPzAq0sNi0akYqal-a5Q-mrlWwTABnb7amIP_1i5yXxdRiLlzSeMW3CrfmvKeIlH_ttr3ra3B6Hms23Zsw7qrmJSCFKyuwyGTiAYBJNWH5SjixBb2pLodg9eiQKkrSNHRAB-UE5cfSmm2hfl5yfLh8pLZe2BSr5Ul32UfoP2X3bW8GH_hQ3rbG0E-K5P2qRtDOC6p8yNd-3MwCD1tPKm27E1vAtGsiHlrfu_l2_i2RtzTSo24sF1EcKwfJDpNi9apReZQlaZOZ4vmmS1e7MZPfrQ83qvNGPjHx8-H9dbOWxLEfX0IuoeHwfc095o6gv3PA6rCHv5mlDRLXll31CeJPY8Xd0Xe9l8IzJZ_bF1idz2m-elr9-RXDZgWXgMNj69Vis0TbHUapEksgtLgxcjjA664goGJb87YF4fli6H5JmPSF_gbzW4f1KjVrXtEFHpHamdB3-3_HrW64oTwTLU1irE-5hp5lumk3o9Ixdsn4-Eqo_cXPu2ps8.WcV_CeloEdJ7O4cWDzBXAw";
        var assertion = validator.validate(jwt, "https://example.org");
        assertEquals("casuser", assertion.getPrincipal().getName());
        assertEquals("casuser", assertion.getPrincipal().getAttributes().get("sub"));
        assertEquals("Static Credentials", assertion.getPrincipal().getAttributes().get("authenticationMethod"));
        assertEquals("0:0:0:0:0:0:0:1", assertion.getPrincipal().getAttributes().get("clientIpAddress"));
    }

    @Test
    public void verifyAesKeyWithEncryptedAndSignedJWT() throws Exception {
        var validator = getValidator("jwtservice");
        var jwt = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCIsImtpZCI6IjU5MDg1MmZlLWE2MWEtNDE4Ni1hYTMyLTE4ZjI1M2ViMTZmOSJ9.ZXlKNmFYQWlPaUpFUlVZaUxDSmhiR2NpT2lKa2FYSWlMQ0psYm1NaU9pSkJNVEk0UTBKRExVaFRNalUySWl3aVkzUjVJam9pU2xkVUlpd2lkSGx3SWpvaVNsZFVJaXdpYTJsa0lqb2lNelpsTkdGbE5HWXRZMkV5TVMwMFpERXdMVGxrWW1JdE5XUmlNV05rTm1NNU5qYzRJbjAuLkRvVmtETV8wU1FaQUxxMEFFejE1UkEuOXg4TlpPbWoyMG8yMWpqb2FOY0ZwX0dzNF9jdHJiTlRtMDBVV1BWS1g1bnBNamJxdjZOTXJoWWhhT3E3N1E0OEpCUF9SZTVXSE9LazA4bEtfZHBuMlBIYlJJT0lZa1V0cjRBNkd3NnZBNnZvT0pud1hZS0pyZUlUeVhuZ3ptdVFjMV9wSmIzTlBpMjN5S010VGx0U2FOam5VODRzUE5fQVJNb0lObGktVGs0ZkowMk0zZzFXdkwzVFVPbHJqaVJzbzFQZXhoMkpTOHlhMUhud2RFZ3FtOEVXVEhpRGJGaXV2VldQMG1WLUJsRmx3TVNFcXR0dC1oc3JXQ3NyRTdKUnlhX0J0dkFnSnVYaklZUjV5SFdpcnI4QTQ0S2xOM21ORkhuLVlYaWViUjguOVJaRUh0czJrVmcteF8ycE56cTRiZw.C-pNsdLn4spTsM6NSvvfTIkSFJnjtCEIy4DmfAPhhnbEwV7Rl_NZ6M2IGxrMSeqOE3ckA65b1NceH6yaA_8IwQ";
        var assertion = validator.validate(jwt, "https://example.org");
        assertEquals("1f43798b-92c5-47f4-a1a9-0fcc51f185a9", assertion.getPrincipal().getName());
        assertEquals("1f43798b-92c5-47f4-a1a9-0fcc51f185a9", assertion.getPrincipal().getAttributes().get("sub"));
    }

    @Test
    public void verifyAesKeyWithEncryptedJWT() throws Exception {
        var validator = getValidator("jwtservice");
        var jwt = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCIsImtpZCI6ImQ4OTQ2MTMyLTRkZjYtNDBmZS05YTc0LWVhOTRkYTliMThjZCJ9.ZXlKNmFYQWlPaUpFUlVZaUxDSmhiR2NpT2lKa2FYSWlMQ0psYm1NaU9pSkJNVEk0UTBKRExVaFRNalUySWl3aVkzUjVJam9pU2xkVUlpd2lkSGx3SWpvaVNsZFVJaXdpYTJsa0lqb2lZamc0T0dZMVltWXRZekkyWVMwMFpEUmtMVGc0WTJZdE4yWTROV05oT0dWaE16WXdJbjAuLjY1TWdBZ1JnRXdGUnNhbmRFdGUwVXcuOHBEc1Bodnh5Q29Cc2ZIeFY3MjNzOUxvdkt0aEgyYkI4aUdsTlpEYXNpX0dQWmh1UHBsbGhNWHZrSTM5Q053Z1drWlRJQWRpOWxQSVk1YWc0RVNweWZDbEJRaUg3THdfaWNqTGhWaUVrY2RXRkx3THNQcFRaWkNUUnFKSTRmNzBQUnBBZmpFd0RKX0xzN204RERyVDRDYmFPalR2Q2JLdVAtYzFScDl0amg3cVFBUG5QcGplVGduQVppMExtaWxDXzlyYnhnZ0s1cmxYeXY5dzRQb0Z5aXR0MlZlRERmZjJGcXFLYlNnQUswZWRhdHV5ZHlqYjlFT1FvZktDdUNiZE1GRXI0TTBjOGtjN3BKU3VDZE1oYjBZUjliS3YySVY2Mks5VGU5em53MDQud0VLcTRRQjRXVlJNOUxIYnlnSW5aUQ.NiL7D5ZmBVOuG5zbgpESH-gwoWZyZwXPi8ueGdOjTYDPX14CdMitRS-827jAyC4o14q4Gdfue39yV1ahENpP4g";
        var assertion = validator.validate(jwt, "https://example.org");
        assertEquals("919d04b9-55c0-43ae-81fa-f5e3a55e6c85", assertion.getPrincipal().getName());
        assertEquals("919d04b9-55c0-43ae-81fa-f5e3a55e6c85", assertion.getPrincipal().getAttributes().get("sub"));
    }
}
