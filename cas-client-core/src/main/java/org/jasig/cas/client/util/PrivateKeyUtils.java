package org.jasig.cas.client.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Security;
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
        PrivateKey key = readPemPrivateKey(path);
        if (key == null) {
            return readDERPrivateKey(path, algorithm);
        } else {
            return key;
        }
    }

    private static PrivateKey readPemPrivateKey(final String path) {
        LOGGER.debug("Attempting to read as PEM [{}]", path);
        final File file = new File(path);
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            isr = new FileReader(file);
            br = new BufferedReader(isr);
            final PEMParser pp = new PEMParser(br);
            final PEMKeyPair pemKeyPair = (PEMKeyPair) pp.readObject();
            final KeyPair kp = new JcaPEMKeyConverter().getKeyPair(pemKeyPair);
            return kp.getPrivate();
        } catch (final Exception e) {
            LOGGER.error("Unable to read key", e);
            return null;
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (isr != null) {
                    isr.close();
                }
            } catch (final IOException e) {}
        }
    }

    private static PrivateKey readDERPrivateKey(final String path, final String algorithm) {
        LOGGER.debug("Attempting to read key as DER [{}]", path);
        final File file = new File(path);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            long byteLength = file.length();
            byte[] bytes = new byte[(int) byteLength];
            fis.read(bytes, 0, (int) byteLength);
            final PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(bytes);
            final KeyFactory factory = KeyFactory.getInstance(algorithm);
            return factory.generatePrivate(privSpec);
        } catch (final Exception e) {
            LOGGER.error("Unable to read key", e);
            return null;
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (final IOException e) {}
        }
    }
}
