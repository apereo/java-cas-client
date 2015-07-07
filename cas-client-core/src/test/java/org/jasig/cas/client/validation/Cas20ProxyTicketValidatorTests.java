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
package org.jasig.cas.client.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.jasig.cas.client.PublicTestHttpServer;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorage;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorageImpl;
import org.jasig.cas.client.proxy.ProxyRetriever;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Test cases for the {@link Cas20ProxyTicketValidator}.
 *
 * @author Scott Battaglia
 * @version $Revision: 11737 $ $Date: 2007-10-03 09:14:02 -0400 (Tue, 03 Oct 2007) $
 * @since 3.0
 */
public final class Cas20ProxyTicketValidatorTests extends AbstractTicketValidatorTests {

    private static final PublicTestHttpServer server = PublicTestHttpServer.instance(8089);

    private Cas20ProxyTicketValidator ticketValidator;

    public Cas20ProxyTicketValidatorTests() {
        super();
    }

    /*@AfterClass
    public static void classCleanUp() {
        server.shutdown();
    } */

    @Before
    public void setUp() throws Exception {
        final List<String[]> list = new ArrayList<String[]>();
        list.add(new String[] { "proxy1", "proxy2", "proxy3" });

        this.ticketValidator = new Cas20ProxyTicketValidator(CONST_CAS_SERVER_URL_PREFIX + "8089");
        this.ticketValidator.setRenew(true);
        this.ticketValidator.setProxyCallbackUrl("test");
        this.ticketValidator.setProxyGrantingTicketStorage(getProxyGrantingTicketStorage());
        this.ticketValidator.setProxyRetriever(getProxyRetriever());
        this.ticketValidator.setAllowedProxyChains(new ProxyList(list));
    }

    private ProxyGrantingTicketStorage getProxyGrantingTicketStorage() {
        return new ProxyGrantingTicketStorageImpl();
    }

    private ProxyRetriever getProxyRetriever() {
        return new ProxyRetriever() {

            /** Unique Id For serialization. */
            private static final long serialVersionUID = 1L;

            public String getProxyTicketIdFor(String proxyGrantingTicketId, String targetService) {
                return "test";
            }
        };
    }

    @Test
    public void testProxyChainWithValidProxy() throws TicketValidationException, UnsupportedEncodingException {
        final String USERNAME = "username";
        final String RESPONSE = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'><cas:authenticationSuccess><cas:user>username</cas:user><cas:proxyGrantingTicket>PGTIOU-84678-8a9d...</cas:proxyGrantingTicket><cas:proxies><cas:proxy>proxy1</cas:proxy><cas:proxy>proxy2</cas:proxy><cas:proxy>proxy3</cas:proxy></cas:proxies></cas:authenticationSuccess></cas:serviceResponse>";
        server.content = RESPONSE.getBytes(server.encoding);

        final Assertion assertion = this.ticketValidator.validate("test", "test");
        assertEquals(USERNAME, assertion.getPrincipal().getName());
    }

    @Test
    public void testProxyChainWithInvalidProxy() throws TicketValidationException, UnsupportedEncodingException {
        final String RESPONSE = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'><cas:authenticationSuccess><cas:user>username</cas:user><cas:proxyGrantingTicket>PGTIOU-84678-8a9d...</cas:proxyGrantingTicket><cas:proxies><cas:proxy>proxy7</cas:proxy><cas:proxy>proxy2</cas:proxy><cas:proxy>proxy3</cas:proxy></cas:proxies></cas:authenticationSuccess></cas:serviceResponse>";
        server.content = RESPONSE.getBytes(server.encoding);

        try {
            this.ticketValidator.validate("test", "test");
            fail("Invalid proxy chain");
        } catch (InvalidProxyChainTicketValidationException e) {
            // expected
        }
    }

    @Test
    public void testRegexProxyChainWithValidProxy() throws TicketValidationException, UnsupportedEncodingException {
        final List<String[]> list = new ArrayList<String[]>();
        list.add(new String[] { "proxy1", "proxy2", "^proxy3/[a-z]*/" });
        this.ticketValidator.setAllowedProxyChains(new ProxyList(list));
        
        final String USERNAME = "username";
        final String RESPONSE = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'><cas:authenticationSuccess><cas:user>username</cas:user><cas:proxyGrantingTicket>PGTIOU-84678-8a9d...</cas:proxyGrantingTicket><cas:proxies><cas:proxy>proxy1</cas:proxy><cas:proxy>proxy2</cas:proxy><cas:proxy>proxy3/abc/</cas:proxy></cas:proxies></cas:authenticationSuccess></cas:serviceResponse>";
        server.content = RESPONSE.getBytes(server.encoding);
        
        final Assertion assertion = this.ticketValidator.validate("test", "test");
        assertEquals(USERNAME, assertion.getPrincipal().getName());
    }
    
    @Test
    public void testRegexProxyChainWithInvalidProxy() throws TicketValidationException, UnsupportedEncodingException {
        final List<String[]> list = new ArrayList<String[]>();
        list.add(new String[] { "proxy1", "proxy2", "^proxy3/[a-z]*/" });
        this.ticketValidator.setAllowedProxyChains(new ProxyList(list));
        
        final String RESPONSE = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'><cas:authenticationSuccess><cas:user>username</cas:user><cas:proxyGrantingTicket>PGTIOU-84678-8a9d...</cas:proxyGrantingTicket><cas:proxies><cas:proxy>proxy1</cas:proxy><cas:proxy>proxy2</cas:proxy><cas:proxy>proxy3/ABC/</cas:proxy></cas:proxies></cas:authenticationSuccess></cas:serviceResponse>";
        server.content = RESPONSE.getBytes(server.encoding);
        
        try {
            this.ticketValidator.validate("test", "test");
            fail("Invalid proxy chain");
        } catch (InvalidProxyChainTicketValidationException e) {
            // expected
        }
    }
    
    @Test
    public void testConstructionFromSpringBean() throws TicketValidationException, UnsupportedEncodingException {
        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                "classpath:cas20ProxyTicketValidator.xml");
        final Cas20ProxyTicketValidator v = (Cas20ProxyTicketValidator) context.getBean("proxyTicketValidator");

        final String USERNAME = "username";
        final String RESPONSE = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'><cas:authenticationSuccess><cas:user>username</cas:user><cas:proxyGrantingTicket>PGTIOU-84678-8a9d...</cas:proxyGrantingTicket><cas:proxies><cas:proxy>proxy1</cas:proxy><cas:proxy>proxy2</cas:proxy><cas:proxy>proxy3</cas:proxy></cas:proxies></cas:authenticationSuccess></cas:serviceResponse>";
        server.content = RESPONSE.getBytes(server.encoding);

        final Assertion assertion = v.validate("test", "test");
        assertEquals(USERNAME, assertion.getPrincipal().getName());

    }
}
