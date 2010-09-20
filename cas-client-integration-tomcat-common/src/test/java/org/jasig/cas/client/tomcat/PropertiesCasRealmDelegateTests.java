/*
 * Copyright 2010 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.tomcat;

import java.security.Principal;
import java.util.Arrays;
import java.util.Iterator;
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
        final List expected = Arrays.asList(new String[] {"admins", "users"});
        final List actual = Arrays.asList(realm.getRoles(p));
        assertEquals(expected.size(), actual.size());

        for (final Iterator iter = expected.iterator(); iter.hasNext();) {
            final Object value = iter.next();
            assertTrue(actual.contains(value));
        }
    }
    
    public void testHasRole() {
        assertTrue(realm.hasRole(new AttributePrincipalImpl("rosencrantz"), "admins"));
        assertTrue(realm.hasRole(new AttributePrincipalImpl("rosencrantz"), "users"));
        assertTrue(realm.hasRole(new AttributePrincipalImpl("guildenstern"), "users"));
    }
}
