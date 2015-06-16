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

import javax.servlet.Filter;
import javax.servlet.FilterConfig;

/**
 * Replicates the original behavior by checking the {@link org.jasig.cas.client.configuration.WebXmlConfigurationStrategyImpl} first, and then
 * the {@link org.jasig.cas.client.configuration.JndiConfigurationStrategyImpl} before using the <code>defaultValue</code>.
 *
 * @author Scott Battaglia
 * @since 3.4.0
 */
public final class LegacyConfigurationStrategyImpl extends BaseConfigurationStrategy {

    private final WebXmlConfigurationStrategyImpl webXmlConfigurationStrategy = new WebXmlConfigurationStrategyImpl();

    private final JndiConfigurationStrategyImpl jndiConfigurationStrategy = new JndiConfigurationStrategyImpl();

    public void init(FilterConfig filterConfig, Class<? extends Filter> filterClazz) {
        this.webXmlConfigurationStrategy.init(filterConfig, filterClazz);
        this.jndiConfigurationStrategy.init(filterConfig, filterClazz);
    }

    protected String get(final ConfigurationKey key) {
        final String value1 = this.webXmlConfigurationStrategy.get(key);

        if (CommonUtils.isNotBlank(value1)) {
            return value1;
        }

        return this.jndiConfigurationStrategy.get(key);
    }
}
