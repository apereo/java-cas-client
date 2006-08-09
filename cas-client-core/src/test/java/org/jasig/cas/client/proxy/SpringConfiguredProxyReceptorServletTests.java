/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.client.proxy;

import junit.framework.TestCase;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

import javax.servlet.ServletConfig;

public final class SpringConfiguredProxyReceptorServletTests extends TestCase {

    private XmlWebApplicationContext webApplicationContext;

    public void testNoProxyGrantingTicketStorage() {
        AbstractProxyReceptorServlet servlet = new SpringConfiguredProxyReceptorServlet();
        try {
            servlet
                    .init(getServletConfig("classpath:badProxyGrantingTicketStorageConfig.xml"));
            fail("Exception expected.");
        } catch (final Exception e) {
            // expected
        }
    }

    public void testTwoProxyGrantingTicketStorage() {
        AbstractProxyReceptorServlet servlet = new SpringConfiguredProxyReceptorServlet();
        try {
            servlet
                    .init(getServletConfig("classpath:twoProxyGrantingTicketStorageConfig.xml"));
            fail("Exception expected.");
        } catch (final Exception e) {
            // expected
        }
    }

    public void testOneProxyGrantingTicketStorage() {
        AbstractProxyReceptorServlet servlet = new SpringConfiguredProxyReceptorServlet();
        try {
            servlet
                    .init(getServletConfig("classpath:oneProxyGrantingTicketStorageConfig.xml"));
        } catch (final Exception e) {
            fail("Unexpected excception.");
        }
    }

    public void testNoPgtOrPgtIouPassed() throws Exception {
        final AbstractProxyReceptorServlet servlet = new SpringConfiguredProxyReceptorServlet();
        servlet
                .init(getServletConfig("classpath:proxyGrantingTicketStorageConfig.xml"));

        final MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.doGet(new MockHttpServletRequest(), response);

        assertEquals("", response.getContentAsString());
    }

    public void testPgtPassed() throws Exception {
        final AbstractProxyReceptorServlet servlet = new SpringConfiguredProxyReceptorServlet();
        servlet
                .init(getServletConfig("classpath:proxyGrantingTicketStorageConfig.xml"));

        final ProxyGrantingTicketStorage storage = (ProxyGrantingTicketStorage) this.webApplicationContext
                .getBean("proxyGrantingTicketStorage");

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("pgtIou", "test");
        request.setParameter("pgtId", "testpgtId");
        final MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.doGet(request, response);

        assertTrue(!"".equals(response.getContentAsString()));
        assertEquals("testpgtId", storage.retrieve("test"));
    }

    private ServletConfig getServletConfig(final String contextLocation) {
        this.webApplicationContext = new XmlWebApplicationContext();
        this.webApplicationContext
                .setConfigLocations(new String[]{contextLocation});
        this.webApplicationContext.refresh();

        MockServletContext context = new MockServletContext();
        context.setAttribute(
                WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE,
                this.webApplicationContext);

        return new MockServletConfig(context);
    }
}
