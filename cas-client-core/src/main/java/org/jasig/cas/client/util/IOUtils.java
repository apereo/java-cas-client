package org.jasig.cas.client.util;

import java.io.*;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * IO utility class.
 *
 * @author Marvin S. Addison
 * @since 3.3.4
 */
public final class IOUtils {

    /** UTF-8 character set. */
    public static final Charset UTF8 = Charset.forName("UTF-8");


    private IOUtils() { /** Utility class pattern. */ }

    /**
     * Reads all data from the given stream as UTF-8 character data and closes it on completion or errors.
     *
     * @param in Input stream containing character data.
     *
     * @return String of all data in stream.
     *
     * @throws IOException On IO errors.
     */
    public static String readString(final InputStream in) throws IOException {
        return readString(in, UTF8);
    }

    /**
     * Reads all data from the given stream as character data in the given character set and closes it on completion
     * or errors.
     *
     * @param in Input stream containing character data.
     * @param charset Character set of data in stream.
     *
     * @return String of all data in stream.
     *
     * @throws IOException On IO errors.
     */
    public static String readString(final InputStream in, final Charset charset) throws IOException {
        final Reader reader = new InputStreamReader(in, charset);
        final StringBuilder builder = new StringBuilder();
        final CharBuffer buffer = CharBuffer.allocate(2048);
        try {
            while (reader.read(buffer) > -1) {
                buffer.flip();
                builder.append(buffer);
            }
        } finally {
            closeQuietly(reader);
        }
        return builder.toString();
    }

    /**
     * Unconditionally close a {@link Closeable} resource. Errors on close are ignored.
     *
     * @param resource Resource to close.
     */
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
