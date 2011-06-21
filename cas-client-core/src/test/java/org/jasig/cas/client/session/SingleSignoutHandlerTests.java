package org.jasig.cas.client.session;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.Assert.*;

/**
 * @author Matt Brown <matt.brown@citrix.com>
 * @version $Revision$ $Date$
 * @since 3.2.1
 */
public final class SingleSignoutHandlerTests {

    private SingleSignOutHandler handler;
    private MockHttpServletRequest request;
    private final static String logoutParameterName = "logoutRequest";

    @Before
    public void setUp() throws Exception {
        handler = new SingleSignOutHandler();
        handler.setLogoutParameterName(logoutParameterName);
        request = new MockHttpServletRequest();
    }

    @Test
    public void isLogoutRequest() throws Exception {
        request.setParameter(logoutParameterName, "true");
        request.setMethod("POST");

        assertTrue(handler.isLogoutRequest(request));
    }

    /**
     * Tests that a multipart request is not considered logoutRequest. Verifies issue CASC-147.
     *
     * @throws Exception
     */
    @Test
    public void isLogoutRequestMultipart() throws Exception {
        request.setParameter(logoutParameterName, "true");
        request.setMethod("POST");
        request.setContentType("multipart/form-data");

        assertFalse(handler.isLogoutRequest(request));
    }

}
