/*
 * Copyright 2010 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.jaas;

import java.security.Principal;
import java.security.acl.Group;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import junit.framework.TestCase;

import org.jasig.cas.client.PublicTestHttpServer;

/**
 * Unit test for {@link CasLoginModule} class.
 *
 * @author Marvin S. Addison
 * @version $Revision$
 *
 */
public class CasLoginModuleTests extends TestCase {
    private static final String CONST_CAS_SERVER_URL = "http://localhost:8085/";
    
    private CasLoginModule module;
    
    private Subject subject;
    
    private Map options;

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        module = new CasLoginModule();
        subject = new Subject();
        options = new HashMap();
        options.put("service", "https://service.example.com/webapp");
        options.put("ticketValidatorClass", "org.jasig.cas.client.validation.Cas20ServiceTicketValidator");
        options.put("casServerUrlPrefix", CONST_CAS_SERVER_URL);
        options.put("proxyCallbackUrl", "https://service.example.com/webapp/proxy");
        options.put("renew", "true");
        options.put("defaultRoles", "ADMIN");
        options.put("principalGroupName", "CallerPrincipal");
        options.put("roleGroupName", "Roles");
    }

    /**
     * Test JAAS login success.
     * @throws Exception On errors.
     */
    public void testLoginSuccess() throws Exception {
        final String USERNAME = "username";
        final String SERVICE = "https://example.com/service";
        final String TICKET = "ST-100000-aA5Yuvrxzpv8Tau1cYQ7-srv1";
        final String RESPONSE = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>"
                + "<cas:authenticationSuccess><cas:user>"
                + USERNAME
                + "</cas:user></cas:authenticationSuccess></cas:serviceResponse>";
        PublicTestHttpServer.instance().content = RESPONSE.getBytes(PublicTestHttpServer.instance().encoding);
        
        module.initialize(
                subject,
                new ServiceAndTicketCallbackHandler(SERVICE, TICKET),
                new HashMap(),
                options);
        module.login();
        module.commit();
        assertEquals(this.subject.getPrincipals().size(), 3);
        assertEquals(TICKET, this.subject.getPrivateCredentials().iterator().next().toString());
        assertTrue(hasPrincipalName(this.subject, AssertionPrincipal.class, USERNAME));
        assertTrue(hasPrincipalName(this.subject, Group.class, "CallerPrincipal"));
        assertTrue(hasPrincipalName(this.subject, Group.class, "Roles"));
    }

    /**
     * Test JAAS login failure.
     * @throws Exception On errors.
     */
    public void testLoginFailure() throws Exception {
        final String SERVICE = "https://example.com/service";
        final String TICKET = "ST-200000-aA5Yuvrxzpv8Tau1cYQ7-srv1";
        final String RESPONSE = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'><cas:authenticationFailure code=\"INVALID_TICKET\">Ticket ST-200000-aA5Yuvrxzpv8Tau1cYQ7-srv1 not recognized</cas:authenticationFailure></cas:serviceResponse>";
        PublicTestHttpServer.instance().content = RESPONSE.getBytes(PublicTestHttpServer.instance().encoding);
        module.initialize(
                subject,
                new ServiceAndTicketCallbackHandler(SERVICE, TICKET),
                new HashMap(),
                options);
        try {
            module.login();
            fail("Login did not throw LoginException as expected.");
        } catch (Exception e) {
            assertTrue(e instanceof LoginException);
        }
        module.commit();
        assertNull(module.ticket);
        assertNull(module.assertion);
    }

    /**
     * Test JAAS logout after successful login to ensure subject cleanup.
     * @throws Exception On errors.
     */
    public void testLogout() throws Exception {
        testLoginSuccess();
        module.logout();
        assertEquals(0, subject.getPrincipals().size());
        assertEquals(0, subject.getPrivateCredentials().size());
    }

    /**
     * Test assertion cache allows successive logins with same ticket to succeed.
     * @throws Exception On errors.
     */    
    public void testAssertionCaching() throws Exception {
        final String USERNAME = "username";
        final String SERVICE = "https://example.com/service";
        final String TICKET = "ST-300000-aA5Yuvrxzpv8Tau1cYQ7-srv1";
        final String RESPONSE1 = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>"
                + "<cas:authenticationSuccess><cas:user>"
                + USERNAME
                + "</cas:user></cas:authenticationSuccess></cas:serviceResponse>";
        final String RESPONSE2 = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'><cas:authenticationFailure code=\"INVALID_TICKET\">Ticket ST-300000-aA5Yuvrxzpv8Tau1cYQ7-srv1 not recognized</cas:authenticationFailure></cas:serviceResponse>";
        PublicTestHttpServer.instance().content = RESPONSE1.getBytes(PublicTestHttpServer.instance().encoding);
        
        options.put("cacheAssertions", "true");
        options.put("cacheTimeout", "1");
        module.initialize(
                subject,
                new ServiceAndTicketCallbackHandler(SERVICE, TICKET),
                new HashMap(),
                options);
        module.login();
        module.commit();
        assertEquals(this.subject.getPrincipals().size(), 3);
        assertEquals(TICKET, this.subject.getPrivateCredentials().iterator().next().toString());
        
        Thread.sleep(2000);
        module.logout();
        assertEquals(0, subject.getPrincipals().size());
        assertEquals(0, subject.getPrivateCredentials().size());
        PublicTestHttpServer.instance().content = RESPONSE2.getBytes(PublicTestHttpServer.instance().encoding);
        module.initialize(
                subject,
                new ServiceAndTicketCallbackHandler(SERVICE, TICKET),
                new HashMap(),
                options);
        module.login();
        module.commit();
        assertEquals(this.subject.getPrincipals().size(), 3);
        assertEquals(TICKET, this.subject.getPrivateCredentials().iterator().next().toString());
    }
    
    private boolean hasPrincipalName(final Subject subject, final Class principalClass, final String name) {
        final Set principals = subject.getPrincipals(principalClass);
        final Iterator iter = principals.iterator();
        while (iter.hasNext()) {
            final Principal p = (Principal) iter.next();
            if (p.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
