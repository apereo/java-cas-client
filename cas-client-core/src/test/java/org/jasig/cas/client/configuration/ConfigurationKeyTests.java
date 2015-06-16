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

import org.junit.Test;

import static org.junit.Assert.*;

public final class ConfigurationKeyTests {


    @Test
    public void gettersWithNoDefaultValue() {
        final String name = "name";
        final ConfigurationKey<Boolean> configurationKey = new ConfigurationKey<Boolean>(name);
        assertEquals(name, configurationKey.getName());
        assertNull(configurationKey.getDefaultValue());
    }


    @Test
    public void gettersWithDefaultValue() {
        final String name = "name";
        final Boolean defaultValue = Boolean.TRUE;
        final ConfigurationKey<Boolean> configurationKey = new ConfigurationKey<Boolean>(name, defaultValue);
        assertEquals(name, configurationKey.getName());
        assertEquals(defaultValue, configurationKey.getDefaultValue());
    }
}
