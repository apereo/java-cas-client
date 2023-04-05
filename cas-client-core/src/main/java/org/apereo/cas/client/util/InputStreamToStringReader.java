package org.apereo.cas.client.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

public class InputStreamToStringReader {

    private final Charset charset;

    public InputStreamToStringReader(Charset charset) {
        this.charset = charset;
    }

    public String read(InputStream in) throws IOException {
        final Reader reader = new InputStreamReader(in, charset);
        final StringBuilder builder = new StringBuilder();
        final CharBuffer buffer = CharBuffer.allocate(2048);
        try {
            while (reader.read(buffer) > -1) {
                buffer.flip();
                builder.append(buffer);
            }
        } finally {
            CloseableUtil.closeQuietly(reader);
        }
        return builder.toString();
    }
}
