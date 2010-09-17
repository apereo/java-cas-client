/*
 * Copyright 2010 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.tomcat;

import java.security.Principal;

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
        final String[] expected = new String[] {"admins", "users"};
        final String[] actual = realm.getRoles(p);
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], actual[i]);
        }
    }
    
    public void testHasRole() {
        assertTrue(realm.hasRole(new AttributePrincipalImpl("rosencrantz"), "admins"));
        assertTrue(realm.hasRole(new AttributePrincipalImpl("rosencrantz"), "users"));
        assertTrue(realm.hasRole(new AttributePrincipalImpl("guildenstern"), "users"));
    }
}
