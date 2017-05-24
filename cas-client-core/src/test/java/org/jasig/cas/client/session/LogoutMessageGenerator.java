/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.client.session;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.zip.Deflater;

/**
 * Logout message generator to perform tests on Single Sign Out feature.
 * Greatly inspired by the source code in the CAS server itself.
 * 
 * @author Jerome Leleu
 * @since 3.4.0
 */
public final class LogoutMessageGenerator {

    private static final String LOGOUT_REQUEST_TEMPLATE =
            "<samlp:LogoutRequest xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" ID=\"00000001\" Version=\"2.0\" "
            + "IssueInstant=\"%s\"><saml:NameID xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\">@NOT_USED@"
            + "</saml:NameID><samlp:SessionIndex>%s</samlp:SessionIndex></samlp:LogoutRequest>";

    public static String generateBackChannelLogoutMessage(final String sessionIndex) {
        return String.format(LOGOUT_REQUEST_TEMPLATE, new Date(), sessionIndex);
    }

    public static String generateFrontChannelLogoutMessage(final String sessionIndex) {
        final String logoutMessage = generateBackChannelLogoutMessage(sessionIndex);
        final Deflater deflater = new Deflater();
        deflater.setInput(logoutMessage.getBytes(Charset.forName("ASCII")));
        deflater.finish();
        final byte[] buffer = new byte[logoutMessage.length()];
        final int resultSize = deflater.deflate(buffer);
        final byte[] output = new byte[resultSize];
        System.arraycopy(buffer, 0, output, 0, resultSize);
        return DatatypeConverter.printBase64Binary(output);
    }
}
