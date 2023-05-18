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
package org.apereo.cas.client;

import org.apereo.cas.client.authentication.AttributePrincipalImpl;
import org.apereo.cas.client.authentication.SimplePrincipal;
import org.apereo.cas.client.jaas.AssertionPrincipal;
import org.apereo.cas.client.validation.AssertionImpl;

import junit.framework.TestCase;
import org.junit.Assert;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Collections;

/**
 * Confirms serialization support for classes intended for session storage or
 * other potential serialization use cases.
 *
 * @author Marvin S. Addison
 * @version $Revision$
 * @since 3.1.11
 */
public class SerializationTests extends TestCase {

    public void testSerializeDeserialize() throws Exception {
        final var subjects = getTestSubjects();
        for (final Object subject : subjects) {
            final var byteOut = new ByteArrayOutputStream();
            try (final ObjectOutput out = new ObjectOutputStream(byteOut)) {
                out.writeObject(subject);
            } catch (final Exception e) {
                Assert.fail("Serialization failed for " + subject);
            }

            final var byteIn = new ByteArrayInputStream(byteOut.toByteArray());
            try (final ObjectInput in = new ObjectInputStream(byteIn)) {
                Assert.assertEquals(subject, in.readObject());
            } catch (final Exception e) {
                Assert.fail("Deserialization failed for " + subject);
            }
        }
    }

    private static Object[] getTestSubjects() {
        final var simplePrincipal = new SimplePrincipal("simple");
        final var attributePrincipal = new AttributePrincipalImpl("attr",
            Collections.<String, Object>singletonMap("LOA", "3"));
        final var assertionPrincipal = new AssertionPrincipal("assertion", new AssertionImpl(
            attributePrincipal, Collections.<String, Object>singletonMap("authenticationMethod", "username")));

        return new Object[]{simplePrincipal, attributePrincipal, assertionPrincipal,};
    }
}
