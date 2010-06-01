/*
 * Copyright 2010 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.util;

import junit.framework.TestCase;

/**
 * Unit test for {@link ReflectUtils} class.
 *
 * @author Marvin S. Addison
 * @version $Revision$
 * @since 3.1.11
 *
 */
public class ReflectUtilsTests extends TestCase {
    /**
     * Test method for {@link org.jasig.cas.client.util.ReflectUtils#newInstance(java.lang.String, java.lang.Object[])}.
     */
    public void testNewInstanceStringObjectArray() {
        final Object result = ReflectUtils.newInstance(
                "org.jasig.cas.client.validation.Cas10TicketValidator",
                new Object[] {"https://localhost/cas"} );
        assertNotNull(result);
    }

    /**
     * Test method for {@link org.jasig.cas.client.util.ReflectUtils#setProperty(java.lang.String, java.lang.Object, java.lang.Object)}.
     */
    public void testSetPropertyStringObjectObject() {
        final TestBean bean = new TestBean();
       
        ReflectUtils.setProperty("count", new Integer(30000), bean);
        assertEquals(30000, bean.getCount());
        
        ReflectUtils.setProperty("name", "bob", bean);
        assertEquals("bob", bean.getName());
        
        ReflectUtils.setProperty("flag", Boolean.TRUE, bean);
        assertTrue(bean.isFlag());
    }

    static class TestBean {
        private int count;
        private boolean flag;
        private String name;

        /**
         * @return the count
         */
        public int getCount() {
            return count;
        }

        /**
         * @param count the count to set
         */
        public void setCount(int count) {
            this.count = count;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @param name the name to set
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * @return the flag
         */
        public boolean isFlag() {
            return flag;
        }

        /**
         * @param flag the flag to set
         */
        public void setFlag(boolean flag) {
            this.flag = flag;
        }

    }
}
