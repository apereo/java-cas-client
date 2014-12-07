package org.jasig.cas.client.ssl;

import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

public final class HttpsURLConnectionFactoryTests {

    private HttpsURLConnectionFactory httpsURLConnectionFactory;


    @Before
    public void setUp() throws Exception {
        this.httpsURLConnectionFactory = new HttpsURLConnectionFactory();
    }


    @Test
    public void serializeAndDeserialize() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);

        oos.writeObject(this.httpsURLConnectionFactory);
        oos.close();

        final byte[] serializedHttpsUrlConnectionFactory = baos.toByteArray();

        final ByteArrayInputStream bais = new ByteArrayInputStream(serializedHttpsUrlConnectionFactory);
        final ObjectInputStream ois = new ObjectInputStream(bais);

        final HttpsURLConnectionFactory deserializedObject = (HttpsURLConnectionFactory) ois.readObject();
        assertEquals(this.httpsURLConnectionFactory, deserializedObject);
    }
}
