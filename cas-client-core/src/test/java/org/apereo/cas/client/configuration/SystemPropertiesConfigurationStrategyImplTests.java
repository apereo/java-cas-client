/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apereo.cas.client.configuration;

import org.apereo.cas.client.util.AbstractCasFilter;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterConfig;

import static org.junit.Assert.*;

/**
 * Tests {@link SystemPropertiesConfigurationStrategyImpl}.
 *
 * @author Jerome Leleu
 * @since 3.4.0
 */
public class SystemPropertiesConfigurationStrategyImplTests {

    private static final String PARAMETER_NAME = "parameter";

    private SystemPropertiesConfigurationStrategyImpl impl;

    private MockFilterConfig filterConfig;

    @Before
    public void setUp() throws Exception {
        this.filterConfig = new MockFilterConfig();
        this.impl = new SystemPropertiesConfigurationStrategyImpl();
        this.impl.init(this.filterConfig, AbstractCasFilter.class);
    }

    @Test
    public void testNoSystemPropertyDefined() {
        final var key = ConfigurationKeys.SERVER_NAME;
        // no system property defined
        assertEquals(key.getDefaultValue(), impl.getString(key));
    }

    @Test
    public void testWithSystemProperty() {
        final var key = ConfigurationKeys.ARTIFACT_PARAMETER_NAME;
        System.setProperty(key.getName(), PARAMETER_NAME);
        assertEquals(PARAMETER_NAME, impl.getString(key));
    }
}
