package org.apereo.cas.client.util;

import java.io.Closeable;
import java.io.IOException;

public class CloseableUtil {

    public static void closeQuietly(final Closeable resource) {
        try {
            if (resource != null) {
                resource.close();
            }
        } catch (final IOException e) {
            //ignore
        }
    }
}
