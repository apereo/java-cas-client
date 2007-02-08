/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.validation;

import junit.framework.TestCase;
import org.jasig.cas.web.view.Saml10SuccessResponseView;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.ImmutableAuthentication;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.authentication.principal.SimpleService;
import org.jasig.cas.validation.ImmutableAssertionImpl;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.apache.commons.httpclient.HttpClient;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Test cases for the {@link org.jasig.cas.client.validation.Saml10TicketValidator}.
 *
 * @author Scott Battaglia
 * @version $Revision: 11737 $ $Date: 2006-10-03 09:14:02 -0400 (Tue, 03 Oct 2006) $
 * @since 3.1
 */
public class SamlTicketValidatorTests extends TestCase {

    final Saml10TicketValidator validator = new Saml10TicketValidator("https://cas.rutgers.edu", new HttpClient());
     /*
    public void testValidationWithTicketIdWithPlus() throws Exception {
        final Saml10SuccessResponseView view = new Saml10SuccessResponseView();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final Map model = new HashMap();

        final Authentication authentication = new ImmutableAuthentication(new SimplePrincipal("test"));
        final List authentications = new ArrayList();
        authentications.add(authentication);
        final ImmutableAssertionImpl assertion = new ImmutableAssertionImpl(authentications, new SimpleService("test"), true);
        model.put("assertion", assertion);
        request.addParameter("SAMLArt", "AAIYG64MrQ2+793pMM8J0sRjXf6uG2h0dHBzOi8vbG9jYWxob3N0Ojg0NDM=");

        view.setIssuer("https://cas.rutgers.edu");
        view.render(model, request, response);
        final String content = response.getContentAsString();

        Assertion assertionResponse = validator.parseResponse(content);

        assertEquals("test", assertionResponse.getPrincipal().getId());
    }     */
}
