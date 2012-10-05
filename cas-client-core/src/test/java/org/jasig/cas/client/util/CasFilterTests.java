package org.jasig.cas.client.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.AssertionImpl;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author Scott Battaglia, Bernd Eckenfels
 * @since 3.2.1
 */
public final class CasFilterTests {

    @Test
    public void serverName() {
        final String serverNameWithoutSlash = "http://www.cnn.com";
        final String serverNameWithSlash = "http://www.cnn.com/";

        final TestCasFilter testCasFilter = new TestCasFilter();
        testCasFilter.setServerName(serverNameWithoutSlash);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        request.setContextPath("/cas");
        request.setRequestURI("/cas/test");

        assertTrue(testCasFilter.constructServiceUrl(request, response).startsWith("http://www.cnn.com/cas/test"));

        testCasFilter.setServerName(serverNameWithSlash);
        assertTrue(testCasFilter.constructServiceUrl(request, response).startsWith("http://www.cnn.com/cas/test"));


    }

    private static class TestCasFilter extends AbstractCasFilter {
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            // nothing to do
        }
    }

    @Test
    public void retrieveAssertionNull() {
        final TestCasFilter testCasFilter = new TestCasFilter();
        final MockHttpServletRequest request = new MockHttpServletRequest();

        Object value = testCasFilter.retrieveAssertion(request);

        assertNull(value);
        assertNull(request.getSession(false));
    }

    @Test
    public void retrieveAssertionSession() {
        final TestCasFilter testCasFilter = new TestCasFilter();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final Assertion testAssertion = new AssertionImpl("testprincipal");
        final HttpSession session = request.getSession(true);
        assertNotNull("The MockHTTPServlerRequest does not work like a real session.", session);

        session.setAttribute(AbstractCasFilter.CONST_CAS_ASSERTION, testAssertion);

        final Object value = testCasFilter.retrieveAssertion(request);
        assertSame(testAssertion, value);
    }

    @Test
    public void retrieveAssertionRequestNoSession() {
        final TestCasFilter testCasFilter = new TestCasFilter();
        final HttpServletRequest request = new MockHttpServletRequest();
        final Assertion testAssertion = new AssertionImpl("testprincipal");

        request.setAttribute(AbstractCasFilter.CONST_CAS_ASSERTION, testAssertion);

        final Object value = testCasFilter.retrieveAssertion(request);

        assertSame(testAssertion, value);
        assertNull(request.getSession(false));
    }

    @Test
    public void retrieveAssertionRequestEmptySession() {
        final TestCasFilter testCasFilter = new TestCasFilter();
        final HttpServletRequest request = new MockHttpServletRequest();
        final Assertion testAssertion = new AssertionImpl("testprincipal");
        final HttpSession session = request.getSession(true);
        assertNotNull("The MockHTTPServlerRequest does not work like a real session.", session);

        request.setAttribute(AbstractCasFilter.CONST_CAS_ASSERTION, testAssertion);

        final Object value = testCasFilter.retrieveAssertion(request);
        assertSame(testAssertion, value);
        assertSame(session, request.getSession());
        // Do we want to allow/forbid/require that assertion is propagated to session
    }


    /** Make sure AbstractCasFilter#retrieveAssertion() prefers request */
    @Test
    public void retrieveAssertionRequestOrSession() {
        final TestCasFilter testCasFilter = new TestCasFilter();
        final HttpServletRequest request = new MockHttpServletRequest();
        final Assertion testAssertionSession = new AssertionImpl("testprincipalSession");
        final Assertion testAssertionRequest = new AssertionImpl("testprincipalRequest");
        final HttpSession session = request.getSession(true);
        assertNotNull("The MockHTTPServlerRequest does not work like a real session.", session);

        session.setAttribute(AbstractCasFilter.CONST_CAS_ASSERTION, testAssertionSession);
        request.setAttribute(AbstractCasFilter.CONST_CAS_ASSERTION, testAssertionRequest);

        final Object value = testCasFilter.retrieveAssertion(request);
        assertSame(testAssertionRequest, value);
    }

    /** Make sure AbstractCasFilter#retrieveAssertion() ignores casting problems. */
    @Test
    public void retrieveAssertionIllegalObjectsIgnored() {
        final TestCasFilter testCasFilter = new TestCasFilter();
        final HttpServletRequest request = new MockHttpServletRequest();
        final HttpSession session = request.getSession(true);
        assertNotNull("The MockHTTPServlerRequest does not work like a real session.", session);

        session.setAttribute(AbstractCasFilter.CONST_CAS_ASSERTION, "wrong session object");
        request.setAttribute(AbstractCasFilter.CONST_CAS_ASSERTION, "wrong request object");

        final Object value = testCasFilter.retrieveAssertion(request);
        assertNull(value);
    }
}
