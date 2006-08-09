/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.client.util;

import junit.framework.TestCase;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Test Cases for <code>DelegatingFilter</code>
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class DelegatingFilterTests extends TestCase {

    private DelegatingFilter delegatingFilter;

    protected int filterExecuted = -1;

    protected void setUp() throws Exception {
        final Map delegators = new HashMap();

        delegators.put("1", new TestFilter(1));

        this.filterExecuted = -1;
        this.delegatingFilter = new DelegatingFilter("test", delegators, true, new TestFilter(0));
        this.delegatingFilter.init(null);
    }

    protected void tearDown() throws Exception {
        this.delegatingFilter.destroy();
    }

    public void testExactMatchFound() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("test", "1");

        this.delegatingFilter.doFilter(request, new MockHttpServletResponse(),
                null);

        assertEquals(1, this.filterExecuted);
    }

    public void testNoMatchFound() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("test", "0");

        this.delegatingFilter.doFilter(request, new MockHttpServletResponse(),
                null);

        assertEquals(0, this.filterExecuted);
    }

    public void testNoParam() throws Exception {
        this.delegatingFilter.doFilter(new MockHttpServletRequest(),
                new MockHttpServletResponse(), null);

        assertEquals(0, this.filterExecuted);
    }

    public void testRegularExpressionMatch() throws Exception {

        final Map delegators = new HashMap();

        delegators.put("1.*", new TestFilter(1));

        this.delegatingFilter = new DelegatingFilter("test", delegators, false, new TestFilter(0));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("test", "1");

        this.delegatingFilter.doFilter(request, new MockHttpServletResponse(),
                null);

        assertEquals(1, this.filterExecuted);
        request = new MockHttpServletRequest();
        request.addParameter("test", "15");

        this.delegatingFilter.doFilter(request, new MockHttpServletResponse(),
                null);

        assertEquals(1, this.filterExecuted);
        request = new MockHttpServletRequest();
        request.addParameter("test", "0");

        this.delegatingFilter.doFilter(request, new MockHttpServletResponse(),
                null);

        assertEquals(0, this.filterExecuted);
    }

    public void testForIllegalArgument() {
        Map map = new HashMap();
        map.put("test", new Object());

        try {
            this.delegatingFilter = new DelegatingFilter("test", map, false, new TestFilter(0));
            fail("Exception expected.");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    private final class TestFilter implements Filter {

        private final int i;

        public TestFilter(final int i) {
            this.i = i;
        }

        public void destroy() {
            // nothing to do here
        }

        public void doFilter(ServletRequest arg0, ServletResponse arg1,
                             FilterChain arg2) throws IOException, ServletException {
            DelegatingFilterTests.this.filterExecuted = this.i;

        }

        public void init(FilterConfig arg0) throws ServletException {
            // nothing to do
        }
    }
}
