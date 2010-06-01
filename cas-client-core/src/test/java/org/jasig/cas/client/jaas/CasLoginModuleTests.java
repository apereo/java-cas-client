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

import org.jasig.cas.client.PublicTestHttpServer;

import junit.framework.TestCase;

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

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        module = new CasLoginModule();
        subject = new Subject();
        final Map options = new HashMap();
        options.put("service", "https://service.example.com/webapp");
        options.put("ticketValidatorClass", "org.jasig.cas.client.validation.Cas20ServiceTicketValidator");
        options.put("casServerUrlPrefix", CONST_CAS_SERVER_URL);
        options.put("proxyCallbackUrl", "https://service.example.com/webapp/proxy");
        options.put("renew", "true");
        options.put("defaultRoles", "ADMIN");
        options.put("principalGroupName", "CallerPrincipal");
        options.put("roleGroupName", "Roles");
        module.initialize(
                subject,
                new ServiceAndTicketCallbackHandler("myService", "myTicket"),
                new HashMap(),
                options);
    }

    /**
     * Test JAAS login success.
     */
    public void testLoginSuccess() throws Exception {
        final String USERNAME = "username";
        final String RESPONSE = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>"
                + "<cas:authenticationSuccess><cas:user>"
                + USERNAME
                + "</cas:user></cas:authenticationSuccess></cas:serviceResponse>";
        PublicTestHttpServer.instance().content = RESPONSE
                .getBytes(PublicTestHttpServer.instance().encoding);
        module.login();
        module.commit();
        assertEquals(this.subject.getPrincipals().size(), 3);
        assertTrue(hasPrincipalName(this.subject, AssertionPrincipal.class, USERNAME));
        assertTrue(hasPrincipalName(this.subject, Group.class, "CallerPrincipal"));
        assertTrue(hasPrincipalName(this.subject, Group.class, "Roles"));
    }

    /**
     * Test JAAS login failure.
     */
    public void testLoginFailure() throws Exception {
        final String RESPONSE = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'><cas:authenticationFailure code=\"INVALID_TICKET\">Ticket ST-1856339-aA5Yuvrxzpv8Tau1cYQ7 not recognized</cas:authenticationFailure></cas:serviceResponse>";
        PublicTestHttpServer.instance().content = RESPONSE.getBytes(PublicTestHttpServer.instance().encoding);
        try {
            module.login();
            fail("Login did not throw LoginException as expected.");
        } catch (Exception e) {
            assertTrue(e instanceof LoginException);
        }
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
