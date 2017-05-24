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
        final Object result = ReflectUtils.newInstance("org.jasig.cas.client.validation.Cas10TicketValidator",
                new Object[] { "https://localhost/cas" });
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

    public void testGetField() {
        final TestBean bean = new TestBean();
        bean.setName("bob");
        assertEquals(bean.getName(), ReflectUtils.getField("name", bean));
    }

    public void testGetFieldSuperclass() {
        final TestSubBean bean = new TestSubBean();
        bean.setName("bob");
        assertEquals(bean.getName(), ReflectUtils.getField("name", bean));
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

    static class TestSubBean extends TestBean {
        private String state;

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }
    }
}
