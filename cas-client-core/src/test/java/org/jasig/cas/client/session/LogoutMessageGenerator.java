package org.jasig.cas.client.session;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.zip.Deflater;

import org.apache.commons.codec.binary.Base64;

/**
 * Logout message generator to perform tests on Single Sign Out feature.
 * Greatly inspired by the source code in the CAS server itself.
 * 
 * @author Jerome Leleu
 * @since 3.3.1
 */
public final class LogoutMessageGenerator {

    private static final String LOGOUT_REQUEST_TEMPLATE =
            "<samlp:LogoutRequest xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" ID=\"00000001\" Version=\"2.0\" "
            + "IssueInstant=\"%s\"><saml:NameID xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\">@NOT_USED@"
            + "</saml:NameID><samlp:SessionIndex>%s</samlp:SessionIndex></samlp:LogoutRequest>";

    public static String generateBackChannelLogoutMessage(String sessionIndex) {
        return String.format(LOGOUT_REQUEST_TEMPLATE, new Date(), sessionIndex);
    }

    public static String generateFrontChannelLogoutMessage(String sessionIndex) {
        final String logoutMessage = generateBackChannelLogoutMessage(sessionIndex);
        final Deflater deflater = new Deflater();
        deflater.setInput(logoutMessage.getBytes(Charset.forName("ASCII")));
        deflater.finish();
        final byte[] buffer = new byte[logoutMessage.length()];
        final int resultSize = deflater.deflate(buffer);
        final byte[] output = new byte[resultSize];
        System.arraycopy(buffer, 0, output, 0, resultSize);
        return Base64.encodeBase64String(output);
    }
}
