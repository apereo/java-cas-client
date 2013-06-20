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
package org.jasig.cas.client.tomcat;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import junit.framework.TestCase;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;

/**
 * Unit test for {@link PropertiesCasRealmDelegate} class.
 *
 * @author Middleware
 * @version $Revision$
 *
 */
public class PropertiesCasRealmDelegateTests extends TestCase {
    private PropertiesCasRealmDelegate realm = new PropertiesCasRealmDelegate();

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        realm.setPropertiesFilePath("src/test/resources/org/jasig/cas/client/tomcat/user-roles.properties");
        realm.readProperties();
    }

    public void testAuthenticate() {
        final Principal p = new AttributePrincipalImpl("rosencrantz");
        assertTrue(p == realm.authenticate(p));
    }

    public void testGetRoles() {
        final Principal p = new AttributePrincipalImpl("rosencrantz");
        final List<String> expected = Arrays.asList(new String[] { "admins", "users" });
        final List<String> actual = Arrays.asList(realm.getRoles(p));
        assertEquals(expected.size(), actual.size());

        for (final String item : expected) {
            assertTrue(actual.contains(item));
        }
    }

    public void testHasRole() {
        assertTrue(realm.hasRole(new AttributePrincipalImpl("rosencrantz"), "admins"));
        assertTrue(realm.hasRole(new AttributePrincipalImpl("rosencrantz"), "users"));
        assertTrue(realm.hasRole(new AttributePrincipalImpl("guildenstern"), "users"));
    }
}
