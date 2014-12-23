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
