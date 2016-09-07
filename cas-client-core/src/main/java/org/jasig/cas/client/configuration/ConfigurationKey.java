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

import org.jasig.cas.client.util.CommonUtils;

/**
 * Holder class to represent a particular configuration key and its optional default value.
 *
 * @author Scott Battaglia
 * @since 3.4.0
 */
public final class ConfigurationKey<E> {

    private final String name;

    private final E defaultValue;

    public ConfigurationKey(final String name) {
        this(name, null);
    }

    public ConfigurationKey(final String name, final E defaultValue) {
        CommonUtils.assertNotNull(name, "name must not be null.");
        this.name = name;
        this.defaultValue = defaultValue;
    }

    /**
     * The referencing name of the configuration key (i.e. what you would use to look it up in your configuration strategy)
     *
     * @return the name.  MUST NOT BE NULL.
     */
    public String getName() {
        return this.name;
    }


    /**
     * The (optional) default value to use when this configuration key is not set.  If a value is provided it should be used. A <code>null</code> value indicates that there is no default.
     *
     * @return the default value or null.
     */
    public E getDefaultValue() {
        return this.defaultValue;
    }
    
    @Override
    public String toString() {
        return getName();
    }
}
