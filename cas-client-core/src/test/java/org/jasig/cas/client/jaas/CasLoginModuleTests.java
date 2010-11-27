/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.cas.client.jaas;

import java.security.Principal;
import java.security.acl.Group;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import static org.junit.Assert.*;

import org.jasig.cas.client.PublicTestHttpServer;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link CasLoginModule} class.
 *
 * @author Marvin S. Addison
 * @version $Revision$
 *
 */
public class CasLoginModuleTests {

    private static final PublicTestHttpServer server = PublicTestHttpServer.instance(8091);

    private static final String CONST_CAS_SERVER_URL = "http://localhost:8091/";
    
    private CasLoginModule module;
    
    private Subject subject;
    
    private Map<String,String> options;

    @AfterClass
    public static void classCleanUp() {
        server.shutdown();
    }

    @Before
    public void setUp() throws Exception {
        module = new CasLoginModule();
        subject = new Subject();
        options = new HashMap<String,String>();
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
    @Test
    public void testLoginSuccess() throws Exception {
        final String USERNAME = "username";
        final String SERVICE = "https://example.com/service";
        final String TICKET = "ST-100000-aA5Yuvrxzpv8Tau1cYQ7-srv1";
        final String RESPONSE = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>"
                + "<cas:authenticationSuccess><cas:user>"
                + USERNAME
                + "</cas:user></cas:authenticationSuccess></cas:serviceResponse>";
        server.content = RESPONSE.getBytes(server.encoding);
        
        module.initialize(
                subject,
                new ServiceAndTicketCallbackHandler(SERVICE, TICKET),
                new HashMap<String,Object>(),
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
    @Test
    public void testLoginFailure() throws Exception {
        final String SERVICE = "https://example.com/service";
        final String TICKET = "ST-200000-aA5Yuvrxzpv8Tau1cYQ7-srv1";
        final String RESPONSE = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'><cas:authenticationFailure code=\"INVALID_TICKET\">Ticket ST-200000-aA5Yuvrxzpv8Tau1cYQ7-srv1 not recognized</cas:authenticationFailure></cas:serviceResponse>";
        server.content = RESPONSE.getBytes(server.encoding);
        module.initialize(
                subject,
                new ServiceAndTicketCallbackHandler(SERVICE, TICKET),
                new HashMap<String,Object>(),
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
    @Test
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
    @Test
    public void testAssertionCaching() throws Exception {
        final String USERNAME = "username";
        final String SERVICE = "https://example.com/service";
        final String TICKET = "ST-300000-aA5Yuvrxzpv8Tau1cYQ7-srv1";
        final String RESPONSE1 = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>"
                + "<cas:authenticationSuccess><cas:user>"
                + USERNAME
                + "</cas:user></cas:authenticationSuccess></cas:serviceResponse>";
        final String RESPONSE2 = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'><cas:authenticationFailure code=\"INVALID_TICKET\">Ticket ST-300000-aA5Yuvrxzpv8Tau1cYQ7-srv1 not recognized</cas:authenticationFailure></cas:serviceResponse>";
        server.content = RESPONSE1.getBytes(server.encoding);
        
        options.put("cacheAssertions", "true");
        options.put("cacheTimeout", "1");
        module.initialize(
                subject,
                new ServiceAndTicketCallbackHandler(SERVICE, TICKET),
                new HashMap<String,Object>(),
                options);
        module.login();
        module.commit();
        assertEquals(this.subject.getPrincipals().size(), 3);
        assertEquals(TICKET, this.subject.getPrivateCredentials().iterator().next().toString());
        
        Thread.sleep(2000);
        module.logout();
        assertEquals(0, subject.getPrincipals().size());
        assertEquals(0, subject.getPrivateCredentials().size());
        server.content = RESPONSE2.getBytes(server.encoding);
        module.initialize(
                subject,
                new ServiceAndTicketCallbackHandler(SERVICE, TICKET),
                new HashMap<String,Object>(),
                options);
        module.login();
        module.commit();
        assertEquals(this.subject.getPrincipals().size(), 3);
        assertEquals(TICKET, this.subject.getPrivateCredentials().iterator().next().toString());
    }
    
    private boolean hasPrincipalName(final Subject subject, final Class<? extends Principal> principalClass, final String name) {
        final Set<? extends Principal> principals = subject.getPrincipals(principalClass);
        for (Principal p : principals) {
            if (p.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
