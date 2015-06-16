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
package org.jasig.cas.client.configuration;

import org.jasig.cas.client.util.AbstractCasFilter;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockServletContext;

import static org.junit.Assert.*;

public final class WebXmlConfigurationStrategyImplTests {

    private WebXmlConfigurationStrategyImpl impl;

    private MockFilterConfig filterConfig;

    @Before
    public void setUp() throws Exception {
        this.filterConfig = new MockFilterConfig();
        this.impl = new WebXmlConfigurationStrategyImpl();
        this.impl.init(this.filterConfig, AbstractCasFilter.class);
    }


    @Test
    public void noKeyFoundGetDefaultForBoolean() {
        final ConfigurationKey<Boolean> key = ConfigurationKeys.ACCEPT_ANY_PROXY;
        assertEquals(key.getDefaultValue(), this.impl.getBoolean(key));
    }


    @Test
    public void noKeyFoundGetDefaultForString() {
        final ConfigurationKey<String> key = ConfigurationKeys.ARTIFACT_PARAMETER_NAME;
        assertEquals(key.getDefaultValue(), this.impl.getString(key));
    }


    @Test
    public void noKeyFoundGetDefaultForLong() {
        final ConfigurationKey<Long> key = ConfigurationKeys.TOLERANCE;
        assertEquals(key.getDefaultValue().longValue(), this.impl.getLong(key));
    }


    @Test
    public void noKeyFoundGetDefaultForInt() {
        final ConfigurationKey<Integer> key = ConfigurationKeys.MILLIS_BETWEEN_CLEAN_UPS;
        assertEquals(key.getDefaultValue().intValue(), this.impl.getInt(key));
    }

    @Test
    public void filterConfigValueForBoolean() {
        final ConfigurationKey<Boolean> key = ConfigurationKeys.ACCEPT_ANY_PROXY;
        final Boolean value = Boolean.TRUE;
        this.filterConfig.addInitParameter(key.getName(), value.toString());
        assertEquals(value, this.impl.getBoolean(key));
    }


    @Test
    public void filterConfigValueForString() {
        final ConfigurationKey<String> key = ConfigurationKeys.ARTIFACT_PARAMETER_NAME;
        final String value = "foobar";
        this.filterConfig.addInitParameter(key.getName(), value);
        assertEquals(value, this.impl.getString(key));
    }


    @Test
    public void filterConfigValueForLong() {
        final ConfigurationKey<Long> key = ConfigurationKeys.TOLERANCE;
        final long value = 1500;
        this.filterConfig.addInitParameter(key.getName(), Long.toString(value));
        assertEquals(value, this.impl.getLong(key));
    }


    @Test
    public void filterConfigValueForInt() {
        final ConfigurationKey<Integer> key = ConfigurationKeys.MILLIS_BETWEEN_CLEAN_UPS;
        final int value = 1500;
        this.filterConfig.addInitParameter(key.getName(), Integer.toString(value));
        assertEquals(value, this.impl.getInt(key));
    }


    @Test
    public void servletConfigValueForBoolean() {
        final ConfigurationKey<Boolean> key = ConfigurationKeys.ACCEPT_ANY_PROXY;
        final Boolean value = Boolean.TRUE;
        final MockServletContext context = (MockServletContext) this.filterConfig.getServletContext();
        context.addInitParameter(key.getName(), value.toString());
        assertEquals(value, this.impl.getBoolean(key));
    }


    @Test
    public void servletConfigValueForString() {
        final ConfigurationKey<String> key = ConfigurationKeys.ARTIFACT_PARAMETER_NAME;
        final String value = "foobar";
        final MockServletContext context = (MockServletContext) this.filterConfig.getServletContext();
        context.addInitParameter(key.getName(), value);
        assertEquals(value, this.impl.getString(key));
    }


    @Test
    public void servletConfigValueForLong() {
        final ConfigurationKey<Long> key = ConfigurationKeys.TOLERANCE;
        final long value = 1500;
        final MockServletContext context = (MockServletContext) this.filterConfig.getServletContext();
        context.addInitParameter(key.getName(), Long.toString(value));
        assertEquals(value, this.impl.getLong(key));
    }


    @Test
    public void servletConfigValueForInt() {
        final ConfigurationKey<Integer> key = ConfigurationKeys.MILLIS_BETWEEN_CLEAN_UPS;
        final int value = 1500;
        final MockServletContext context = (MockServletContext) this.filterConfig.getServletContext();
        context.addInitParameter(key.getName(), Integer.toString(value));
        assertEquals(value, this.impl.getInt(key));
    }

}
