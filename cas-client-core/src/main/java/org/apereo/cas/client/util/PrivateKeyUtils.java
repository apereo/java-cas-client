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
package org.apereo.cas.client.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Security;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * Utility class to parse private keys.
 *
 * @author Jerome LELEU
 * @since 3.6.0
 */
public class PrivateKeyUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrivateKeyUtils.class);

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static PrivateKey createKey(final String path, final String algorithm) {
        final var key = readPemPrivateKey(path);
        if (key == null) {
            return readDERPrivateKey(path, algorithm);
        } else {
            return key;
        }
    }

    private static PrivateKey readPemPrivateKey(final String path) {
        LOGGER.debug("Attempting to read as PEM [{}]", path);
        final var file = new File(path);
        try (final InputStreamReader isr = new FileReader(file); final var br = new BufferedReader(isr)) {
            final var pp = new PEMParser(br);
            final var pemKeyPair = (PEMKeyPair) pp.readObject();
            final var kp = new JcaPEMKeyConverter().getKeyPair(pemKeyPair);
            return kp.getPrivate();
        } catch (final Exception e) {
            LOGGER.error("Unable to read key", e);
            return null;
        }
    }

    private static PrivateKey readDERPrivateKey(final String path, final String algorithm) {
        LOGGER.debug("Attempting to read key as DER [{}]", path);
        final var file = new File(path);
        try (final var fis = new FileInputStream(file)) {
            final var byteLength = file.length();
            final var bytes = new byte[(int) byteLength];
            fis.read(bytes, 0, (int) byteLength);
            final KeySpec privSpec = new PKCS8EncodedKeySpec(bytes);
            final var factory = KeyFactory.getInstance(algorithm);
            return factory.generatePrivate(privSpec);
        } catch (final Exception e) {
            LOGGER.error("Unable to read key", e);
            return null;
        }
    }
}
